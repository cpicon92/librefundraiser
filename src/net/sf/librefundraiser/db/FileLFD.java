package net.sf.librefundraiser.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.librefundraiser.Donor;
import net.sf.librefundraiser.Donor.Gift;
import net.sf.librefundraiser.Donor.Gift.GiftDeserializer;
import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.ProgressListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class FileLFD {
	private static final int latestDbVersion = 2;
	private final File dbFile;
	public static final DateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private List<Donor> donors = new ArrayList<>();
	private Map<String, String> info = new HashMap<>();
//	private Connection connection = null;
//	private final Lock lock = new ReentrantLock();

	public String getDbPath() {
		return dbFile.getPath();
	}



	public static final String[] donorFields = new String[] { "ACCOUNT", "TYPE", "FIRSTNAME", "LASTNAME", "SPOUSEFRST",
		"SPOUSELAST", "SALUTATION", "HOMEPHONE", "WORKPHONE", "FAX", "CATEGORY1", "CATEGORY2", "CONTACT",
		"MAILNAME", "ADDRESS1", "ADDRESS2", "CITY", "STATE", "ZIP", "COUNTRY", "ENTRYDATE", "CHANGEDATE", "NOTES",
		"LASTGIVEDT", "LASTAMT", "ALLTIME", "YEARTODT", "FIRSTGIFT", "LARGEST", "FILTER", "EMAIL", "LASTENTDT",
		"LASTENTAMT", "EMAIL2", "WEB" };
	public static final String[] giftFields = new String[] { "ACCOUNT", "AMOUNT", "DATEGIVEN", "LETTER", "DT_ENTRY",
		"SOURCE", "NOTE", "TEMPTOTAL", "RECNUM" };
	public static final String[] dbInfoFields = new String[] { "KEY", "VALUE" };

	
	public FileLFD(String filename) throws IOException {
		this(new File(filename));
	}

	public FileLFD(File dbFile) throws IOException {
		this.dbFile = dbFile;
		this.readAll();
	}	
	
	public FileLFD() {
		long unixTime = System.currentTimeMillis() / 1000L;
		String filename = System.getProperty("java.io.tmpdir") + "/temp" + unixTime + ".db";
		dbFile = new File(filename);
	}
	
	private void readAll() throws IOException {
		Gson gson = new GsonBuilder()
	            .registerTypeAdapter(Gift.class, new GiftDeserializer())
	            .create();
        JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(dbFile), "UTF-8"));
        List<Donor> donors = new ArrayList<Donor>();
        reader.beginObject();
        while (reader.hasNext()) {
        	String name = reader.nextName();
        	if (name.equals("info")) {
        		this.info = gson.fromJson(reader, Map.class);
        	} else if (name.equals("donors")) {
                reader.beginArray();
                while (reader.hasNext()) {
                    Donor donor = gson.fromJson(reader, Donor.class);
                    donors.add(donor);
                }
                reader.endArray();
        	} else {
        		reader.skipValue();
        	}
        }
        reader.endObject();
        reader.close();
        this.donors = donors;
	}
	
	private void writeAll() {

	}

	public void saveDonor(Donor donor) {
	}

	public void saveDonors(Donor[] donors) {

	}

	public List<Donor> getDonors() {
		return new ArrayList<Donor>(this.donors);
	}
	
	public Donor getDonor(int id) {
		for (Donor d : donors) {
			if (d.getId() == id) return d;
		}
		return null;
	}

	public int getMaxAccount() {
		int output = 0;
		for (Donor d : donors) {
			int id = d.getId();
			if (id > output) output = id;
		}
		return output;
	}

	public int getUniqueRecNum() {
		return 0;
	}

	public List<String> getPreviousDonorValues(String field) {
		HashSet<String> previousValues = new HashSet<>();
		for (Donor d : this.donors) {
			previousValues.add(d.getData(field));
		}
		return new ArrayList<String>(previousValues);
	}
	
	public List<String> getPreviousGiftValues(String field) {
		HashSet<String> previousValues = new HashSet<>();
		for (Donor d : this.donors) {
			for (Gift g : d.getGifts().values()) {
				previousValues.add(g.getIc(field));
			}
		}
		return new ArrayList<String>(previousValues);
	}

	public void deleteDonor(int id) {
		deleteDonors(new int[] {id});
	}

	public void deleteDonors(int[] ids) {
		for (Iterator<Donor> iter = this.donors.iterator(); iter.hasNext();) {
			Donor donor = iter.next();
			for (int id : ids) {
				if (donor.getId() == id) iter.remove();
			}
		}
	}

	public void deleteGift(int recnum) {
		for (Donor donor : this.donors) {
			for (Iterator<Gift> iter = donor.getGifts().values().iterator(); iter.hasNext();) {
				Gift gift = iter.next();
				if (gift.recnum == recnum) iter.remove();
			}
		}
	}

	public void refreshGifts(Donor donor) {

	}

	public String getDbName() {
		String name = getDbInfo("name");
		return name == null ? "" : name;
	}

	public void setDbName(String name) {
		this.info.put("name", name);
	}

	public int getDbVersion() {
		try {
			return Integer.parseInt(getDbInfo("version"));
		} catch (Exception e) {
			return latestDbVersion;
		}
	}

	public String getDbInfo(String key) {
		return this.info.get(key);
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
