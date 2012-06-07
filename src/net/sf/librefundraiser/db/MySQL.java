package net.sf.librefundraiser.db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;

import net.sf.librefundraiser.Donor;


public class MySQL {
	private static Connection con = null;
	public static Donor[] getDonors() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		String url = "jdbc:mysql://192.168.0.1:3306/friends";
		if (con == null) {
			try {
				con = DriverManager.getConnection(url,"friends","12SouthAstor");
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		System.out.println("URL: " + url);
		System.out.println("Connection: " + con);
		ArrayDeque<String> columns = new ArrayDeque<String>();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("show columns from donors");
			while(rs.next()){
				String str = rs.getString("field");
				columns.add(str);
			}
			rs = stmt.executeQuery("select * from donors");
			ArrayDeque<Donor> donors = new ArrayDeque<Donor>();
			while(rs.next()) {
				Donor donor = new Donor(rs.getInt("account"));
				for (String column : columns) {
					String output = "";
					try {
						output = rs.getString(column);
					} catch (SQLException e1) {}
					donor.putData(column, output);
				}
				donors.add(donor);
			}
			return donors.toArray(new Donor[0]);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
