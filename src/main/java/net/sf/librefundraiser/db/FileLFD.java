package net.sf.librefundraiser.db;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.io.Donor;
import net.sf.librefundraiser.io.DonorData;
import net.sf.librefundraiser.io.Gift;
import net.sf.librefundraiser.io.Money;

public class FileLFD {
	private static final int latestDbVersion = 2;
	private final File dbFile;
	private List<Donor> donors = new ArrayList<>();
	private Map<String, String> info = new HashMap<>();
	private List<CustomField> customFields = new ArrayList<>();

	public String getDbPath() {
		return dbFile.getPath();
	}

	public static final String[] donorFields = {"type", "changedate", "address1", "address2", "contact", "city", "state", "country", "homephone", "workphone", "fax", "zip", "category", "source", "firstname", "lastname", "spousefrst", "spouselast", "mailname", "email", "email2", "web", "obsolete", "salutation", "notes"};
//	public static final String[] giftFields = { "ACCOUNT", "AMOUNT", "DATEGIVEN", "LETTER", "DT_ENTRY",
//		"SOURCE", "NOTE", "TEMPTOTAL", "RECNUM" };
//	public static final String[] dbInfoFields = { "KEY", "VALUE" };

	
	public FileLFD(String filename) throws IOException {
		this(new File(filename));
	}

	public FileLFD(File dbFile) throws IOException {
		this(dbFile, dbFile.exists());
	}	
	public FileLFD(File dbFile, boolean read) throws IOException {
		this.dbFile = dbFile;
		if (read) this.readAll();
	}	
	
	public FileLFD() {
		long unixTime = System.currentTimeMillis() / 1000L;
		String filename = System.getProperty("java.io.tmpdir") + "/temp" + unixTime + ".db";
		dbFile = new File(filename);
	}

	private void readAll() {
		try {
			Gson gson = new GsonBuilder()
			.registerTypeAdapter(Money.class, new JsonDeserializer<Money>() {
				@Override
				public Money deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
					if (json.isJsonObject()) {
						JsonObject m = json.getAsJsonObject();
						String currency;
						if (m.get("currency").isJsonObject()) currency = m.get("currency").getAsJsonObject().get("currencyCode").getAsString();
						else currency = m.get("currency").getAsString();
						return new Money(
							m.get("amount").getAsInt(), 
							currency, 
							m.get("fractionDigits").getAsInt()
						);
					} else {
						//needed due to old file format storing money as string
						return new Money(json.getAsString());
					}
				}
			})
			.registerTypeAdapter(DonorData.Type.class, new JsonDeserializer<DonorData.Type>() {
				@Override
				public DonorData.Type deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
					String t = json.getAsString();
					if (t.equalsIgnoreCase("i")) return DonorData.Type.INDIVIDUAL;
					else if (t.equalsIgnoreCase("b")) return DonorData.Type.BUSINESS;
					else return DonorData.Type.valueOf(t);
				}
			})
			.setDateFormat(Main.getDateFormatString())
			.create();
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(dbFile));
			bis.mark(4);
			byte[] magic = new byte[4];
			bis.read(magic);
			if (!Arrays.equals(magic, new byte[] {(byte) 0x89, 'L','F','D'})) {
				bis.reset();
				System.err.println("LFD file missing magic number, may fail");
			}
			JsonReader reader = new JsonReader(new InputStreamReader(new XZInputStream(bis), "UTF-8"));
			List<Donor> donors = new ArrayList<>();
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("info")) {
					this.info = gson.fromJson(reader, Map.class);
				} else if (name.equals("customFields")) {
					this.customFields = gson.fromJson(reader, new TypeToken<List<CustomField>>(){}.getType());
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
		} catch (IOException e) {
			throw new DatabaseIOException(e);
		}
	}

	private void writeAll() {
		try {
			Gson gson = new GsonBuilder()
			.setDateFormat(Main.getDateFormatString())
			.registerTypeAdapter(String.class, new JsonSerializer<String>() {
				@Override
				public JsonElement serialize(String src, Type typeOfT, JsonSerializationContext ctx) {
					if (src == null || src.isEmpty()) return null;
					return new JsonPrimitive(src);
				}
			})
			.create();
			OutputStream os = new FileOutputStream(this.dbFile);
			os.write(new byte[] {(byte) 0x89, 'L','F','D'});
			JsonWriter writer = new JsonWriter(new OutputStreamWriter(new XZOutputStream(os, new LZMA2Options()), "UTF-8"));
			writer.beginObject();
			writer.name("info");
			gson.toJson(this.info, Map.class, writer);
			writer.name("customFields");
			gson.toJson(this.customFields, List.class, writer);
			writer.name("donors");
			writer.beginArray();
			for (Donor donor: this.donors) {
				gson.toJson(donor, Donor.class, writer);
			}
			writer.endArray();
			writer.endObject();
			writer.close();
		} catch (IOException e) {
			throw new DatabaseIOException(e);
		}
	}

	public Donor saveDonor(Donor donor) {
		List<Donor> out = saveDonors(new Donor[] {donor});
		return out.isEmpty() ? null : out.get(0);
	}

	public List<Donor> saveDonors(Donor[] donors) {
		List<Donor> out = new ArrayList<Donor>();
		int maxAcct = getMaxAccount(),
			maxRecNum = getMaxRecNum();
		insert: for (Donor donor : donors) {
			List<Gift> gifts = new ArrayList<>(donor.giftCount());
			for (Gift g : donor.getGifts()) {
				if (g.recnum < 0) g = g.copy(maxRecNum++);
				gifts.add(g);
			}
			if (donor.id >= 0) for (ListIterator<Donor> iter = this.donors.listIterator(); iter.hasNext();) {
				Donor existingDonor = iter.next();
				if (existingDonor.id == donor.id) {
					donor = new Donor(donor.id, donor.data, gifts);
					iter.set(donor);
					out.add(donor);
					continue insert;
				}
			}
			donor = new Donor(maxAcct, donor.data, gifts);
			this.donors.add(donor);
			out.add(donor);
		}
		this.writeAll();
		return out;
	}

	public List<Donor> getDonors() {
		return new ArrayList<>(this.donors);
	}
	
	public Donor getDonor(int id) {
		for (Donor d : donors) {
			if (d.id == id) return d;
		}
		return null;
	}

	private int getMaxAccount() {
		int output = 0;
		for (Donor d : donors) {
			int id = d.id;
			if (id > output) output = id;
		}
		return output + 1;
	}

	private int getMaxRecNum() {
		int output = 0;
		for (Donor d : donors) {
			for (Gift g : d.getGifts()) {
				if (g.recnum > output) output = g.recnum;
			}
		}
		return output + 1;
	}

	//TODO make sure that previous values are cached
	public List<String> getPreviousDonorValues(String field) {
		Map<String, Integer> previousValues = new HashMap<>();
		for (Donor d : this.donors) {
			String k = d.getData(field).trim();
			if (field.equalsIgnoreCase("zip")) k = k.length() > 5 ? k.substring(0, 5) : k;
			previousValues.put(k, previousValues.containsKey(k) ? previousValues.get(k) + 1 : 1);
		}
		List<Entry<String, Integer>> quantities = new ArrayList<>(previousValues.entrySet());
		Collections.sort(quantities, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
				return -1 * e1.getValue().compareTo(e2.getValue());
			}
		});
		List<String> out = new ArrayList<>(quantities.size());
		for (Entry<String, Integer> e : quantities) {
			if (!e.getKey().trim().isEmpty()) out.add(e.getKey());
		}
		return out;
	}
	
	public List<String> getPreviousGiftValues(String field) {
		HashSet<String> previousValues = new HashSet<>();
		Method getter = null;
		for (Method m : Gift.class.getMethods()) {
			if (m.getName().equalsIgnoreCase("get" + field)) {
				getter = m;
			}
		}
		if (getter == null) {
			return new ArrayList<>();
		}
		for (Donor d : this.donors) {
			for (Gift g : d.getGifts()) {
				try {
					previousValues.add(String.valueOf(getter.invoke(g)).trim());
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				}
			}
		}
		ArrayList<String> out = new ArrayList<>(previousValues);
		Collections.sort(out);
		return out;
	}

	public void deleteDonor(int id) {
		deleteDonors(new int[] {id});
	}

	public void deleteDonors(int[] ids) {
		for (Iterator<Donor> iter = this.donors.iterator(); iter.hasNext();) {
			Donor donor = iter.next();
			for (int id : ids) {
				if (donor.id == id) iter.remove();
			}
		}
		this.writeAll();
	}

	public String getDbName() {
		String name = getDbInfo("name");
		return name == null ? "" : name;
	}

	public void setDbName(String name) {
		this.putDbInfo("name", name);
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
	
	public void putDbInfo(String key, String value) {
		this.info.put(key, value);
		this.writeAll();
	}
	
	public void putDbInfo(Map<String, String> entries) {
		this.info.putAll(entries);
		this.writeAll();
	}
	
	/**
	 * @return a deep copy of the custom fields defined in this db
	 */
	public CustomField[] getCustomFields() {
		CustomField[] out = new CustomField[customFields.size()];
		int i = 0;
		for (CustomField f : customFields) {
			out[i++] = f.copy();
		}
		return out;
	}
	
	/**
	 * Overwrites custom fields for db with deep copy
	 * @param fields list of fields to copy
	 */
	public void setCustomFields(List<CustomField> fields) {
		this.customFields.clear();
		for (CustomField f : fields) {
			this.customFields.add(f.copy());
		}
		this.writeAll();
	}
	
	public boolean customFieldExists(String key) {
		for (CustomField f : this.customFields) {
			if (f.getName().equals(key)) return true;
		}
		return false;
	}

	public static String formatDate(String date) {
		try {
			return Main.getDateFormat().format(Main.getDateFormat().parse(date));
		} catch (Exception e) {
		}
		return date;
	}

	public static String unFormatDate(String date) {
		try {
			return Main.getDateFormat().format(Main.getDateFormat().parse(date));
		} catch (Exception e) {
		}
		return date;
	}
	
	public static class DatabaseIOException extends RuntimeException {
		private static final long serialVersionUID = -4322061981988425539L;
		public DatabaseIOException() {
			super();
		}
		public DatabaseIOException(String message) {
			super(message);
		}
		public DatabaseIOException(String message, Throwable cause) {
			super(message, cause);
		}
		public DatabaseIOException(Throwable cause) {
			super(cause);
		}
	}

}
