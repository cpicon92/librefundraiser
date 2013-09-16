package net.sf.librefundraiser.db;

import net.sf.librefundraiser.Donor;
import net.sf.librefundraiser.Donor.Gift;
import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.ProgressListener;
import net.sf.librefundraiser.gui.DonorList;

import java.io.File;
import java.sql.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SQLite implements IDatabase {
	private static final int latestDbVersion = 2;
	private final File dbFile;
	private Connection connection = null;
	private final Lock lock = new ReentrantLock();

	@Override
	public String getDbPath() {
		return dbFile.getPath();
	}

	public SQLite() {
		long unixTime = System.currentTimeMillis() / 1000L;
		String filename = System.getProperty("java.io.tmpdir") + "/temp" + unixTime + ".db";
		dbFile = new File(filename);
	}

	public static String generateTableCreateSQL(String[] fields, String primaryKey) {
		StringBuilder output = new StringBuilder();
		String sep = ", ";
		for (String f : fields) {
			output.append(f).append(sep);
		}
		output.append("PRIMARY KEY (" + primaryKey + ")");
		return output.toString();
	}

	public SQLite(String filename) throws NewerDbVersionException {
		dbFile = new File(filename);
		Connection conn = this.getConnection();
		try {
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
		} catch (SQLException e) {
			System.err.println("Unable to create new database. ");
			e.printStackTrace();
		}

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

	@Override
	public HashMap<String, String> quickSearch(String query) {
		lock.lock();
		Connection conn = this.getConnection();
		final String[] fields = new String[] { "account", "firstname", "lastname", "spousefrst", "spouselast" };
		HashMap<String, String> output = new HashMap<String, String>();
		try {
			StringBuilder statement = new StringBuilder("select * from donors where ");
			String sep = "";
			for (String f : fields) {
				statement.append(sep).append(f + " like ?");
				sep = " or ";
			}
			System.out.println(statement);
			PreparedStatement prep = conn.prepareStatement(statement.toString());
			for (int i = 1; i <= fields.length; i++) {
				prep.setString(i, "%" + query + "%");
			}
			ResultSet rs = prep.executeQuery();
			while (rs.next()) {
				String matchingFieldName = "";
				String matchingField = "";
				fieldSearch:for (String field : fields) {
					String result = rs.getString(field);
					if (result.toLowerCase().contains(query.toLowerCase())) {
						for (String[] c : DonorList.columns) {
							if (field.equals(c[1])) {
								matchingFieldName = c[0];
								matchingField = c[1];
								break fieldSearch;
							}
						}
					}
				}
				String firstname = rs.getString("firstname");
				String lastname = rs.getString("lastname");
				if (matchingField.equals("spousefrst") || matchingField.equals("spouselast")) {
					firstname = rs.getString("spousefrst");
					lastname = rs.getString("spouselast");
				}
				String account = rs.getString("account");
				String listEntry = lastname + (!(lastname.equals("") || firstname.equals("")) ? ", " : "") + firstname;
				if (listEntry.equals(""))
					listEntry = account;
				output.put(account, listEntry + " (" + matchingFieldName + ")");
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

    @Override
    public Donor[] getDonors(int[] ids) {
        return new Donor[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Donor[] getDonors(String[] ids) {
        return new Donor[0];  //To change body of implemented methods use File | Settings | File Templates.
    }


	public Donor[] getDonors(String query) {
		lock.lock();
		Connection conn = this.getConnection();
		ArrayDeque<String> columns = new ArrayDeque<String>();
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
			ArrayDeque<Donor> donors = new ArrayDeque<Donor>();
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
					ArrayDeque<String> giftColumns = new ArrayDeque<String>();
					ResultSet rsGifts = stmt.executeQuery("PRAGMA table_info(`gifts`)");
					while (rsGifts.next()) {
						giftColumns.add(rsGifts.getString("name"));
					}
					rsGifts = stmt.executeQuery("select * from gifts where ACCOUNT=\"" + donor.getData("account") + "\" order by DATEGIVEN desc");
					ArrayList<Gift> gifts = new ArrayList<Gift>();
					while (rsGifts.next()) {
						Gift gift = new Donor.Gift(Integer.parseInt(rsGifts.getString("recnum")));
						for (String column : giftColumns) {
							String value = "";
							try {
								value = formatDate(rsGifts.getString(column));
							} catch (SQLException e1) {
							}
							gift.putIc(column, value != null ? value : "");
						}
						gifts.add(gift);
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
		final Donor[] toSave = output;
		new Thread(new Runnable() {
			public void run() {
				saveDonors(toSave);
				System.out.println("saved");
			}
		}).start();
		return output;
	}

	@Override
	public void saveDonor(Donor donor) {
		saveDonors(new Donor[] { donor });
	}

	@Override
	public void saveDonors(Donor[] donors) {
		lock.lock();
		Connection conn = this.getConnection();
		try {
			ArrayDeque<String> columns = new ArrayDeque<String>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("PRAGMA table_info(`donors`)");
			while (rs.next()) {
				String str = rs.getString("name");
				columns.add(str);
			}
			rs.close();
			String fieldNames = "";
			String fieldValues = "";
			for (String f : columns) {
				fieldNames += f + ", ";
				fieldValues += "?, ";
			}
			fieldNames = fieldNames.substring(0, fieldNames.length() - 2);
			fieldValues = fieldValues.substring(0, fieldValues.length() - 2);

			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement("replace into donors values (" + fieldValues + ");");
			for (Donor donor : donors) {
				int currentField = 1;
				for (final String field : columns) {
					prep.setString(currentField, unFormatDate(donor.getData(field)));
					currentField++;
				}
				prep.addBatch();
			}
			prep.executeBatch();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			ArrayDeque<String> columns = new ArrayDeque<String>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("PRAGMA table_info(`gifts`)");
			while (rs.next()) {
				String str = rs.getString("name");
				columns.add(str);
			}
			rs.close();
			String fieldNames = "";
			String fieldValues = "";
			for (String f : columns) {
				fieldNames += f + ", ";
				fieldValues += "?, ";
			}
			fieldNames = fieldNames.substring(0, fieldNames.length() - 2);
			fieldValues = fieldValues.substring(0, fieldValues.length() - 2);
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement("replace into gifts values (" + fieldValues + ");");
			for (Donor donor : donors) {
				for (Gift g : donor.getGifts().values()) {
					int currentField = 1;
					for (final String field : columns) {
						if (!field.equalsIgnoreCase("recnum")) {
							prep.setString(currentField, unFormatDate(g.getIc(field)));
						} else {
							prep.setInt(currentField, Integer.parseInt(g.getIc(field)));
						}
						currentField++;
					}
					prep.addBatch();
				}
			}
			prep.executeBatch();
			conn.setAutoCommit(true);
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

	@Override
	public Donor[] getDonors() {
		return getDonors("");
	}

	@Override
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

	private final ArrayList<Integer> reserved = new ArrayList<Integer>();
	@Override
	public int getUniqueRecNum() {
		lock.lock();
		Connection conn = this.getConnection();
		int output = 0;
		ArrayList<Integer> recnums = new ArrayList<Integer>();
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


	@Override
	public String[] getPreviousValues(String column, String table) {
		lock.lock();
		Connection conn = this.getConnection();
		ArrayDeque<String> results = new ArrayDeque<String>();
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

	@Override
	public void deleteDonor(int id) {
		deleteDonors(new int[] {id});
	}

	@Override
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

	@Override
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

	@Override
	public void refreshGifts(Donor donor) {
		lock.lock();
		Connection conn = this.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ArrayDeque<String> giftColumns = new ArrayDeque<String>();
			ResultSet rsGifts = stmt.executeQuery("PRAGMA table_info(`gifts`)");
			while (rsGifts.next()) {
				giftColumns.add(rsGifts.getString("name"));
			}
			rsGifts = stmt.executeQuery("select * from gifts where ACCOUNT=\"" + donor.getData("account") + "\"");
			donor.clearGifts();
			while (rsGifts.next()) {
				Donor.Gift gift = new Donor.Gift(Integer.parseInt(rsGifts.getString("recnum")));
				for (String column : giftColumns) {
					String value = "";
					try {
						value = formatDate(rsGifts.getString(column));
					} catch (SQLException e1) {
					}
					gift.putIc(column, value != null ? value : "");
				}
				donor.addGift(gift);
			}
			rsGifts.close();
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

	@Override
	public String getDbName() {
		String name = getDbInfo("name");
		return name == null ? "" : name;
	}

	@Override
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

	@Override
	public int getDbVersion() {
		try {
			return Integer.parseInt(getDbInfo("version"));
		} catch (Exception e) {
			return latestDbVersion;
		}
	}

	@Override
	public String getDbInfo(String key) {
		lock.lock();
		Connection conn = this.getConnection();
		ArrayDeque<String> results = new ArrayDeque<String>();
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

	@Override
	public void updateAllStats(Donor[] toUpdate, ProgressListener pl) {
		//		if (pl != null) pl.setProgress(1);
		//		final Donor[] donors;
		//		if (toUpdate == null) {
		//			donors = getDonors("", true);
		//		} else {
		//			StringBuilder queryString = new StringBuilder();
		//			String sep = " where ";
		//			for (Donor d : toUpdate) {
		//				queryString.append(sep).append("ACCOUNT='" + d.getData("ACCOUNT") + "'");
		//				sep = " or ";
		//			}
		//			System.out.println(queryString.toString());
		//			donors = getDonors(queryString.toString(), true);
		//		}
		//		if (pl != null) pl.setMaxProgress(donors.length);
		//		int progress = 0;
		//		for (Donor d : donors) {
		//			if (pl != null) pl.setProgress(++progress);
		//			d.recalculateGiftStats();
		//		}
		//		
		//		this.saveDonors(donors);
		if (pl != null) pl.setProgress(-1);

	}

}
