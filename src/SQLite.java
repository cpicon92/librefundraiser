import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;

public class SQLite {
	private final File dbFile;
	public SQLite() {
		long unixTime = System.currentTimeMillis() / 1000L;
		String filename = System.getProperty("java.io.tmpdir")+"temp"+unixTime+".db";
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
	public Donor[] getDonors(String query) {
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
					donor.putData(column, value);
				}
				donors.add(donor);
			}
			for (Donor donor : donors) {
				ArrayDeque<String> giftColumns = new ArrayDeque<String>();
				ResultSet rsGifts = stmt.executeQuery("PRAGMA table_info(`gifts`)");
				while (rsGifts.next()) {
					giftColumns.add(rsGifts.getString("name"));
				}
				rsGifts = stmt.executeQuery("select * from gifts where ACCOUNT=\""+donor.getData("account")+"\"");
				for (int i = 0; rsGifts.next(); i++) {
					Donor.Gift gift = new Donor.Gift(i);
					gift.putIc("recnum", String.format("%06d", i));
					for (String column : giftColumns) {
						String value = "";
						try {
							value = rsGifts.getString(column);
						} catch (SQLException e1) {}
						gift.putIc(column, value);
					}
					donor.addGift(gift);
				}
				rsGifts.close();
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
	public Donor[] getDonors() {
		return getDonors("");
	}
}
