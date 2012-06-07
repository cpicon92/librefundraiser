package net.sf.librefundraiser.db;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import net.sf.librefundraiser.LibreFundraiser;
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
			final Format dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SQLite sqLite = LibreFundraiser.getLocalDB();
			Connection conn = sqLite.getConnection();
			final List<Field> fields = table.getFields();
			Statement stat = conn.createStatement();
			String fieldNames = "";
			String fieldTypes = "";
			for (Field f : fields) {
				fieldNames += f.getName() + ", ";
				fieldTypes += "?, ";
			}
			fieldNames = fieldNames.substring(0,fieldNames.length()-2);
			fieldTypes = fieldTypes.substring(0,fieldTypes.length()-2);
			stat.executeUpdate("create table "+destTableName+" ("+fieldNames+");");
			PreparedStatement prep = conn.prepareStatement("insert into "+destTableName+" values ("+fieldTypes+");");
			final Iterator<Record> recordIterator = table.recordIterator();
			while (recordIterator.hasNext()) {
				final Record record = recordIterator.next();
				int currentField = 1;
				for (final Field field : fields) {
					Object value = record.getTypedValue(field.getName());
					if (field.getType().equals(Type.DATE) && value != null) value = dateFormat.format(value);
					String rawValue = value != null ? value.toString() : "";
					prep.setString(currentField, rawValue.trim());
					currentField++;
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
