package net.sf.librefundraiser.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.io.Donor;
import net.sf.librefundraiser.io.Gift;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class SQLite {
	private static final int latestDbVersion = 2;
	private final File dbFile;
	public static final DateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private Connection connection = null;
	private final Lock lock = new ReentrantLock();

	public String getDbPath() {
		return dbFile.getPath();
	}

	public SQLite() {
		long unixTime = System.currentTimeMillis() / 1000L;
		String filename = System.getProperty("java.io.tmpdir") + "/temp" + unixTime + ".db";
		dbFile = new File(filename);
	}

	public static final String[] donorFields = new String[] { "ACCOUNT", "TYPE", "FIRSTNAME", "LASTNAME", "SPOUSEFRST",
		"SPOUSELAST", "SALUTATION", "HOMEPHONE", "WORKPHONE", "FAX", "CATEGORY1", "CATEGORY2", "CONTACT",
		"MAILNAME", "ADDRESS1", "ADDRESS2", "CITY", "STATE", "ZIP", "COUNTRY", "ENTRYDATE", "CHANGEDATE", "NOTES",
		"LASTGIVEDT", "LASTAMT", "ALLTIME", "YEARTODT", "FIRSTGIFT", "LARGEST", "FILTER", "EMAIL", "LASTENTDT",
		"LASTENTAMT", "EMAIL2", "WEB" };
	public static final String[] giftFields = new String[] { "ACCOUNT", "AMOUNT", "DATEGIVEN", "LETTER", "DT_ENTRY",
		"SOURCE", "NOTE", "TEMPTOTAL", "RECNUM" };
	public static final String[] dbInfoFields = new String[] { "KEY", "VALUE" };

	public static String generateTableCreateSQL(String[] fields, String primaryKey) {
		StringBuilder output = new StringBuilder();
		String sep = ", ";
		for (String f : fields) {
			output.append(f).append(sep);
		}
		output.append("PRIMARY KEY (" + primaryKey + ")");
		return output.toString();
	}

	public SQLite(String filename) throws NewerDbVersionException, SQLException {
		dbFile = new File(filename);
		Connection conn = this.getConnection();
		Statement stmt = conn.createStatement();

		String donorTableSQL = generateTableCreateSQL(donorFields, "ACCOUNT");
		String giftTableSQL = generateTableCreateSQL(giftFields, "RECNUM");
		String dbInfoTableSQL = generateTableCreateSQL(dbInfoFields, "KEY");
		stmt.executeUpdate("create table if not exists donors (" + donorTableSQL + ");");
		stmt.executeUpdate("create table if not exists gifts (" + giftTableSQL + ");");
		stmt.executeUpdate("create table if not exists dbinfo (" + dbInfoTableSQL + ");");
		stmt.close();
		if (this.getDbVersion() != latestDbVersion) {
			reconcileDbVersion();
		}
		conn = this.getConnection();
		stmt = conn.createStatement();
		stmt.executeUpdate("delete from dbinfo where KEY='version';");
		stmt.executeUpdate("insert into dbinfo(KEY,VALUE) values ('version','" + latestDbVersion + "');");
	}
	
	public FileLFD toFileLFD() {
		Donor[] donors = this.getDonors();
		Map<String, String> info = this.getDbInfo();
		try {
			this.connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.dbFile.delete();
		try {
			FileLFD fileLFD = new FileLFD(this.dbFile, false);
			fileLFD.saveDonors(donors);
			fileLFD.putDbInfo(info);
			return fileLFD;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void reconcileDbVersion() throws NewerDbVersionException, SQLException {
		int dbVersion = this.getDbVersion();
		if (dbVersion > latestDbVersion) {
			throw new NewerDbVersionException();
		}
		if (dbVersion < latestDbVersion) {
			if (dbVersion == 1) {
				Statement stmt = this.getConnection().createStatement();
				stmt.executeUpdate("alter table donors add column LASTENTDT;");
				stmt.executeUpdate("alter table donors add column LASTENTAMT;");
				stmt.close();
			}
		} else {
			return;
		}
	}

	public Connection getConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				return connection;
			}
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
			return connection;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Donor[] getDonors(String query) {
		lock.lock();
		Gson gson = new Gson();
		Connection conn = this.getConnection();
		ArrayDeque<String> columns = new ArrayDeque<>();
		Donor[] output = null;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("PRAGMA table_info(`donors`)");
			while (rs.next()) {
				String str = rs.getString("name");
				columns.add(str);
			}
			rs.close();
			rs = stmt.executeQuery("select * from donors " + query);
			ArrayDeque<Donor> donors = new ArrayDeque<>();
			while (rs.next()) {
				Donor donor = new Donor(rs.getInt("account"));
				for (String column : columns) {
					String value = "";
					try {
						value = formatDate(rs.getString(column));
					} catch (SQLException e1) {
					}
					donor.putData(column, value != null ? value : "");
				}
				donors.add(donor);
			}
			/*fetch gifts*/ {
				for (Donor donor : donors) {
					ArrayDeque<String> giftColumns = new ArrayDeque<>();
					ResultSet rsGifts = stmt.executeQuery("PRAGMA table_info(`gifts`)");
					while (rsGifts.next()) {
						giftColumns.add(rsGifts.getString("name"));
					}
					rsGifts = stmt.executeQuery("select * from gifts where ACCOUNT=\"" + donor.getAccountNum() + "\" order by DATEGIVEN desc");
					ArrayList<Gift> gifts = new ArrayList<>();
					while (rsGifts.next()) {
						JsonObject gift = new JsonObject();
						for (String column : giftColumns) {
							String value = "";
							try {
								value = formatDate(rsGifts.getString(column));
							} catch (SQLException e1) {
							}
							gift.add(column, new JsonPrimitive(value != null ? value : ""));
						}
						//using gson allows us to avoid using reflection ourselves
						gifts.add(gson.fromJson(gift, Gift.class));
					}
					rsGifts.close();
					donor.addGifts(gifts);
				}
			}
			rs.close();

			output = donors.toArray(new Donor[0]);
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else
				e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		lock.unlock();
		return output;
	}



	public Donor[] getDonors() {
		return getDonors("");
	}

	public int getMaxAccount() {
		lock.lock();
		Connection conn = this.getConnection();
		int output = 0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select max(ACCOUNT) as max_account from donors");
			while (rs.next()) {
				output = rs.getInt("max_account");
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else
				e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		lock.unlock();
		return output;
	}

	private final ArrayList<Integer> reserved = new ArrayList<>();
	public int getUniqueRecNum() {
		lock.lock();
		Connection conn = this.getConnection();
		int output = 0;
		ArrayList<Integer> recnums = new ArrayList<>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select RECNUM from gifts");
			while (rs.next()) {
				recnums.add(rs.getInt("RECNUM"));
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else
				e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (Integer i : recnums) {
			if (i > output) output = i;
		}
		output++;
		lock.unlock();
		while (reserved.contains(output)) output++;
		reserved.add(output);
		return output;
	}


	public String[] getPreviousValues(String column, String table) {
		lock.lock();
		Connection conn = this.getConnection();
		ArrayDeque<String> results = new ArrayDeque<>();
		try {
			Statement stmt = conn.createStatement();
			String query = "select distinct " + column + " from " + table + " order by " + column + " asc";
			if (column.equals("zip"))
				query = "select distinct substr(" + column + ",1,5) as " + column + " from " + table + " order by " + column + " asc";
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String value = rs.getString(column);
				if (!value.trim().equals(""))
					results.add(value);
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else
				e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		lock.unlock();
		return results.toArray(new String[] {});
	}

	public void deleteDonor(int id) {
		deleteDonors(new int[] {id});
	}

	public void deleteDonors(int[] ids) {
		lock.lock();
		Connection conn = this.getConnection();
		try {
			Statement stmt = conn.createStatement();
			conn.setAutoCommit(false);
			for (int id : ids) {
				String account = String.format("%06d", id);
				stmt.addBatch("delete from donors where account=\"" + account + "\";");
				stmt.addBatch("delete from gifts where account=\"" + account + "\";");
			}
			conn.setAutoCommit(true);
			stmt.executeBatch();
		} catch (SQLException e) {
			System.err.println("Unable to delete donor. ");
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		lock.unlock();
	}

	public void deleteGift(int id) {
		String recnum = String.format("%06d", id);
		lock.lock();
		Connection conn = this.getConnection();
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("delete from gifts where recnum=\"" + recnum + "\";");
		} catch (SQLException e) {
			System.err.println("Unable to delete gift. ");
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		lock.unlock();
	}


	public String getDbName() {
		String name = getDbInfo("name");
		return name == null ? "" : name;
	}

	public void setDbName(String name) {
		lock.lock();
		Connection conn = this.getConnection();
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("delete from dbinfo where KEY='name';");
			stmt.executeUpdate("insert into dbinfo(KEY,VALUE) values ('name','" + name + "');");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		lock.unlock();

	}

	public int getDbVersion() {
		try {
			return Integer.parseInt(getDbInfo("version"));
		} catch (Exception e) {
			return latestDbVersion;
		}
	}

	public String getDbInfo(String key) {
		lock.lock();
		Connection conn = this.getConnection();
		ArrayDeque<String> results = new ArrayDeque<>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select value from dbinfo where key='" + key + "'");
			while (rs.next()) {
				results.add(rs.getString("value"));
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query " + key + ".");
			} else
				e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		lock.unlock();
		return results.isEmpty() ? null : results.getFirst();
	}
	
	public Map<String, String> getDbInfo() {
		lock.lock();
		Connection conn = this.getConnection();
		Map<String, String> dbInfo = new HashMap<>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from dbinfo");
			while (rs.next()) {
				dbInfo.put(rs.getString("key"), rs.getString("value"));
			}
			rs.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		lock.unlock();
		return dbInfo;
	}

	public static String formatDate(String date) {
		try {
			return Main.getDateFormat().format(dbDateFormat.parse(date));
		} catch (Exception e) {
		}
		return date;
	}

	public static String unFormatDate(String date) {
		try {
			return dbDateFormat.format(Main.getDateFormat().parse(date));
		} catch (Exception e) {
		}
		return date;
	}

}
