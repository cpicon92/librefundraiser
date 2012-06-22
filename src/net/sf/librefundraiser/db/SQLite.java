package net.sf.librefundraiser.db;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import net.sf.librefundraiser.Donor;
import net.sf.librefundraiser.Donor.Gift;
import net.sf.librefundraiser.Main;

public class SQLite {
	private final File dbFile;
	public SQLite() {
		long unixTime = System.currentTimeMillis() / 1000L;
		String filename = System.getProperty("java.io.tmpdir")+"/temp"+unixTime+".db";
		dbFile = new File(filename);
	}
	public SQLite(String filename) {
		dbFile = new File(filename);
		Connection conn = this.getConnection();
		try {
			Statement stmt = conn.createStatement();
			String donorFields = "ACCOUNT, TYPE, LASTNAME, FIRSTNAME, " +
					"SPOUSELAST, SPOUSEFRST, SALUTATION, HOMEPHONE, " +
					"WORKPHONE, FAX, CATEGORY1, CATEGORY2, MAILNAME, " +
					"ADDRESS1, ADDRESS2, CITY, STATE, ZIP, COUNTRY, " +
					"EMAIL, EMAIL2, WEB, CHANGEDATE, LASTGIVEDT, LASTAMT, " +
					"ALLTIME, YEARTODT, FIRSTGIFT, LARGEST, NOTES, " +
					"PRIMARY KEY (ACCOUNT)";
			stmt.executeUpdate("create table if not exists donors ("+donorFields+");");
			String giftFields = "ACCOUNT, AMOUNT, DATEGIVEN, LETTER, DT_ENTRY, " +
					"SOURCE, NOTE, RECNUM, PRIMARY KEY (RECNUM)";
			stmt.executeUpdate("create table if not exists gifts ("+giftFields+");");
		} catch (SQLException e) {
			System.err.println("Unable to create new database. ");
			e.printStackTrace();
		}
		
	}
	public Connection getConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:"+dbFile.getPath());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	public HashMap<String,String> quickSearch(String query) {
		Connection conn = this.getConnection();
		HashMap<String,String> output = new HashMap<String,String>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from donors where account like \"%"+query+"%\" or firstname like \"%"+query+"%\" or lastname like \"%"+query+"%\"");
			while(rs.next()) {
				String firstname = rs.getString("firstname");
				String lastname = rs.getString("lastname");
				String account = rs.getString("account");
				String tabTitle = lastname+(!(lastname.equals("")||firstname.equals(""))?", ":"")+firstname;
				if (tabTitle.equals("")) tabTitle = account;
				output.put(account,tabTitle);
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
	public Donor[] getDonors(String query, boolean fetchGifts) {
		Connection conn = this.getConnection();
		ArrayDeque<String> columns = new ArrayDeque<String>();
		Donor[] output = null;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("PRAGMA table_info(`donors`)");
			while(rs.next()){
				String str = rs.getString("name");
				columns.add(str);
			}
			rs.close();
			rs = stmt.executeQuery("select * from donors "+query);
			ArrayDeque<Donor> donors = new ArrayDeque<Donor>();
			while(rs.next()) {
				Donor donor = new Donor(rs.getInt("account"));
				for (String column : columns) {
					String value = "";
					try {
						value = rs.getString(column);
					} catch (SQLException e1) {}
					donor.putData(column, value!=null?value:"");
				}
				donors.add(donor);
			}
			if (fetchGifts) {
				for (Donor donor : donors) {
					ArrayDeque<String> giftColumns = new ArrayDeque<String>();
					ResultSet rsGifts = stmt.executeQuery("PRAGMA table_info(`gifts`)");
					while (rsGifts.next()) {
						giftColumns.add(rsGifts.getString("name"));
					}
					rsGifts = stmt.executeQuery("select * from gifts where ACCOUNT=\""+donor.getData("account")+"\"");
					while (rsGifts.next()) {
						Donor.Gift gift = new Donor.Gift(Integer.parseInt(rsGifts.getString("recnum")));
						for (String column : giftColumns) {
							String value = "";
							try {
								value = rsGifts.getString(column);
							} catch (SQLException e1) {}
							gift.putIc(column, value!=null?value:"");
						}
						donor.addGift(gift);
					}
					rsGifts.close();
				}
			}
			rs.close();

			output = donors.toArray(new Donor[0]);
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
	public void saveDonor(Donor donor) {
		Connection conn = this.getConnection();
		try {
			ArrayDeque<String> columns = new ArrayDeque<String>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("PRAGMA table_info(`donors`)");
			while(rs.next()){
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
			fieldNames = fieldNames.substring(0,fieldNames.length()-2);
			fieldValues = fieldValues.substring(0,fieldValues.length()-2);
			PreparedStatement prep = conn.prepareStatement("replace into donors values ("+fieldValues+");");
			int currentField = 1;
			for (final String field : columns) {
				prep.setString(currentField, donor.getData(field));
				currentField++;
			}
			prep.addBatch();
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			ArrayDeque<String> columns = new ArrayDeque<String>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("PRAGMA table_info(`gifts`)");
			while(rs.next()){
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
			fieldNames = fieldNames.substring(0,fieldNames.length()-2);
			fieldValues = fieldValues.substring(0,fieldValues.length()-2);
			PreparedStatement prep = conn.prepareStatement("replace into gifts values ("+fieldValues+");");
			for (Gift g : donor.getGifts().values()) {
				int currentField = 1;
				for (final String field : columns) {
					prep.setString(currentField, g.getIc(field));
					currentField++;
				}
				prep.addBatch();
			}
			conn.setAutoCommit(false);
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
	}
	public Donor[] getDonors() {
		return getDonors("",false);
	}
	public int getMaxAccount() {
		Connection conn = this.getConnection();
		int output = 0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select max(ACCOUNT) as max_account from donors");
			while(rs.next()){
				output = rs.getInt("max_account");
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public int getMaxRecNum() {
		Connection conn = this.getConnection();
		int output = 0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select max(RECNUM) as max_recnum from gifts");
			while(rs.next()){
				output = rs.getInt("max_recnum");
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public double getTotalGifts(Donor donor) {
		Connection conn = this.getConnection();
		double output = 0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select total(AMOUNT) as total_amount from gifts where ACCOUNT=\""+donor.getData("account")+"\"");
			while(rs.next()){
				output = rs.getDouble("total_amount");
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public double getYTD (Donor donor) {
		Connection conn = this.getConnection();
		double output = 0;
		try {
			Statement stmt = conn.createStatement();
			GregorianCalendar cal = new GregorianCalendar();
			cal.set(Calendar.DATE, 1);
			cal.set(Calendar.MONTH, Calendar.JANUARY);
			ResultSet rs = stmt
					.executeQuery("select total(AMOUNT) as total_amount from gifts where ACCOUNT=\""
							+ donor.getData("account")
							+ "\" and DATEGIVEN>=Datetime('"
							+ Main.getDateFormat().format(cal.getTime()) + "')");
			while(rs.next()){
				output = rs.getDouble("total_amount");
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public double getLargestGift(Donor donor) {
		Connection conn = this.getConnection();
		double output = 0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select AMOUNT from gifts where ACCOUNT=\""+donor.getData("account")+"\"");
			while(rs.next()){
				if (rs.getDouble("AMOUNT") > output) output = rs.getDouble("AMOUNT");
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public String getLastGiftDate(Donor donor) {
		Connection conn = this.getConnection();
		String output = "";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("select max(DATEGIVEN) as last_gift_date from gifts where ACCOUNT=\""
							+ donor.getData("account")
							+ "\"");
			while(rs.next()){
				output = rs.getString("last_gift_date");
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public String getFirstGiftDate(Donor donor) {
		Connection conn = this.getConnection();
		String output = "";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("select min(DATEGIVEN) as first_gift_date from gifts where ACCOUNT=\""
							+ donor.getData("account")
							+ "\"");
			while(rs.next()){
				output = rs.getString("first_gift_date");
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public double getLastGift (Donor donor) {
		Connection conn = this.getConnection();
		double output = 0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("select AMOUNT, max(DATEGIVEN) from gifts where ACCOUNT=\""
							+ donor.getData("account")
							+ "\"");
			while(rs.next()){
				output = rs.getDouble("AMOUNT");
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public String[] getPreviousValues(String column, String table) {
		Connection conn = this.getConnection();
		ArrayDeque<String> results = new ArrayDeque<String>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select distinct "+column+" from "+table);
			while(rs.next()){
				results.add(rs.getString(column));
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) {
				System.err.println("Unable to query donor list.");
			} else e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return results.toArray(new String[]{});
	}
}
