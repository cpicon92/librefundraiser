package net.sf.librefundraiser.db;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.io.Donor;
import net.sf.librefundraiser.io.Gift;
import nl.knaw.dans.common.dbflib.CorruptedTableException;
import nl.knaw.dans.common.dbflib.Database;
import nl.knaw.dans.common.dbflib.Field;
import nl.knaw.dans.common.dbflib.IfNonExistent;
import nl.knaw.dans.common.dbflib.Record;
import nl.knaw.dans.common.dbflib.Table;
import nl.knaw.dans.common.dbflib.Type;
import nl.knaw.dans.common.dbflib.Version;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class FileDBASE {
	final Database database;

	public FileDBASE(String URL) {
		database = new Database(new File(URL), Version.DBASE_3, "ISO-8859-1");
	}

	public Donor[] importFRBW() throws CorruptedTableException, IOException {
		Gson gson = new Gson();
		final HashMap<Integer, Donor> importedDonors = new HashMap<>();
		final Table donorTable = database.getTable("Master.dbf");
		final Table giftTable = database.getTable("Gifts.dbf");
		final Format dateFormat = Main.getDateFormat();
		donorTable.open(IfNonExistent.ERROR);
		final List<Field> fields = donorTable.getFields();
		final Iterator<Record> recordIterator = donorTable.recordIterator();
		recordLoop: while (recordIterator.hasNext()) {
			final Record record = recordIterator.next();
			String email2 = "";
			Donor donor = null;
			for (final Field field : fields) { 
				try {
					String fieldName = field.getName();
					Object value = record.getTypedValue(fieldName);
					if (field.getType().equals(Type.DATE) && value != null) value = dateFormat.format(value);
					String rawValue = (value != null ? value.toString() : "").trim();
					//if the value contains a null byte, it's probably not valid...
					//for some reason FRBW puts null bytes in the notes field
					//resulting in weird import unless we do this
					boolean valid = !rawValue.contains(new String(new char[]{(char)0}));
					String fieldData = valid ? rawValue : "";
					if (field.getName().equals("EMAIL")) {
						try {
							String[] emails = fieldData.split("(;|,) *",2);
							fieldData = emails[0];
							email2 = emails[1];
						} catch (Exception e) {
						}
					}
					if (fieldName.equalsIgnoreCase("account")) {
						try {
							donor = new Donor(Integer.parseInt(fieldData));
						} catch (Exception e) {
							System.err.println("Could not import donor " + fieldData);
							continue recordLoop;
						}
					}
					if (donor != null) {
						//TODO when fixing this note that Donor Source and Category are called custom1 and custom2 in frbw
						donor.putData(fieldName, fieldData);
					}
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
			donor.data.setEmail2(email2);
			importedDonors.put(donor.getId(), donor);
		}
		donorTable.close();
		System.out.println(importedDonors);
		giftTable.open(IfNonExistent.ERROR);
		final List<Field> giftFields = giftTable.getFields();
		final Iterator<Record> giftRecordIterator = giftTable.recordIterator();
		for (int recNum = 1; giftRecordIterator.hasNext(); recNum++) {
			final Record record = giftRecordIterator.next();
			JsonObject gift = new JsonObject();
			gift.add("recnum", new JsonPrimitive(recNum));
			for (final Field field : giftFields) { 
				try {
					String fieldName = field.getName();
					Object value = record.getTypedValue(fieldName);
					if (field.getType().equals(Type.DATE) && value != null) value = dateFormat.format(value);
					String rawValue = (value != null ? value.toString() : "").trim();
					boolean valid = !rawValue.contains(new String(new char[]{(char)0}));
					String fieldData = valid ? rawValue : "";
					gift.add(fieldName.toLowerCase(), new JsonPrimitive(fieldData));
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
			System.out.println(gift.toString());
			//use gson to avoid having to use reflection to instantiate gift
			Gift g;
			try {
				g = gson.fromJson(gift, Gift.class);
			} catch (Exception e) {
				System.err.println("Error importing " + gift);
				e.printStackTrace();
				continue;
			}
			Donor account = importedDonors.get(g.getAccount());
			if (account != null) account.addGift(g);
			else System.err.println("Orphaned gift! " + g.getAccount());
		}
		giftTable.close();
		return importedDonors.values().toArray(new Donor[]{});
	}
}
