package net.sf.librefundraiser.db;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.util.Iterator;
import java.util.List;

import net.sf.librefundraiser.Main;
import nl.knaw.dans.common.dbflib.Database;
import nl.knaw.dans.common.dbflib.DbfLibException;
import nl.knaw.dans.common.dbflib.Field;
import nl.knaw.dans.common.dbflib.IfNonExistent;
import nl.knaw.dans.common.dbflib.Record;
import nl.knaw.dans.common.dbflib.Table;
import nl.knaw.dans.common.dbflib.Type;
import nl.knaw.dans.common.dbflib.Version;

public class FileDBASE {
	final Database database;

	public FileDBASE(String URL) {
		database = new Database(new File(URL), Version.DBASE_3, "ISO-8859-1");
	}

	public boolean loadTable(String sourceTableName, String destTableName) {
		final Table table = database.getTable(sourceTableName);
		try {
			table.open(IfNonExistent.ERROR);
			final Format dateFormat = SQLite.dbDateFormat;
			SQLite iDonorDB = Main.getDonorDB();
			Connection conn = iDonorDB.getConnection();
			final List<Field> fields = table.getFields();
			Statement stat = conn.createStatement();
			String fieldNames = "";
			String fieldValues = "";
			for (Field f : fields) {
				fieldNames += f.getName() + ", ";
				fieldValues += "?, ";
			}
			fieldNames = fieldNames.substring(0,fieldNames.length()-2);
			fieldValues = fieldValues.substring(0,fieldValues.length()-2);
			if (sourceTableName.equals("Gifts.dbf")) {
				fieldNames += ", RECNUM, PRIMARY KEY (RECNUM)";
				fieldValues += ", ?";
			}
			if (sourceTableName.equals("Master.dbf")) {
				fieldNames += ", LASTENTDT, LASTENTAMT, EMAIL2, WEB, PRIMARY KEY (ACCOUNT)";
				fieldValues += ", ?, ?, ?, ?";
			}
			stat.executeUpdate("drop table if exists "+destTableName+";");
			stat.executeUpdate("create table "+destTableName+" ("+fieldNames+");");
			//TODO cleanup frbw import and import custom fields properly
			PreparedStatement prep = conn.prepareStatement("insert into "+destTableName+" values ("+fieldValues+");");
			final Iterator<Record> recordIterator = table.recordIterator();
			for (int recNum = 1; recordIterator.hasNext(); recNum++) {
				final Record record = recordIterator.next();
				String email2 = "";
				int currentField = 1;
				for (final Field field : fields) { 
					try {
						Object value = record.getTypedValue(field.getName());
						if (field.getType().equals(Type.DATE) && value != null) value = dateFormat.format(value);
						String rawValue = (value != null ? value.toString() : "").trim();
						//if the value contains a null byte, it's probably not valid...
						boolean valid = !rawValue.contains(new String(new char[]{(char)0}));
						String fieldData = valid?rawValue:"";
						if (field.getName().equals("EMAIL")) {
							try {
								String[] emails = fieldData.split("(;|,) *",2);
								fieldData = emails[0];
								email2 = emails[1];
							} catch (Exception e) {
							}
						}
						prep.setString(currentField, fieldData);
					} catch (Exception e) {
//						e.printStackTrace();
					}
					currentField++;
				}
				if (sourceTableName.equals("Master.dbf")) {
					prep.setString(fields.size()+1, email2);
				}
				if (sourceTableName.equals("Gifts.dbf")) {
					prep.setString(fields.size()+1, String.format("%06d", recNum));
				}
				prep.addBatch();
			}
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.setAutoCommit(true);
			conn.close();

		} catch (IOException e) {
			System.err.println("Trouble reading table or table not found");
			e.printStackTrace();
			return false;
		} catch (DbfLibException e) {
			System.err.println("Problem getting raw value");
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				table.close();
			} catch (IOException ex) {
				System.err.println("Unable to close the table");
				return false;
			} catch (NullPointerException ex) {
				return false;
			}
		}
		return true;
	}
}
