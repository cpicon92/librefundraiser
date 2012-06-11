package net.sf.librefundraiser.db;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.HashMap;

import net.sf.librefundraiser.Donor;

public class SQLite {
	private final File dbFile;
	public SQLite() {
		long unixTime = System.currentTimeMillis() / 1000L;
		String filename = System.getProperty("java.io.tmpdir")+"/temp"+unixTime+".db";
		dbFile = new File(filename);
	}
	public SQLite(String filename) {
		dbFile = new File(filename);
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
					for (int i = 0; rsGifts.next(); i++) {
						Donor.Gift gift = new Donor.Gift(i);
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
		ArrayDeque<String> columns = new ArrayDeque<String>();
		try {
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
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public Donor[] getDonors() {
		return getDonors("",false);
	}
}
