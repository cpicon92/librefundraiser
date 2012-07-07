package net.sf.librefundraiser;

import java.util.HashMap;
import java.util.Set;

public class Donor {
	public static class Gift extends HashMap<String, String> {
		private static final long serialVersionUID = -9169351258332556336L;
		public final int recnum;

		public Gift(int recnum) {
			this.recnum = recnum;
		}

		public String getIc(String key) {
			return this.get(key.toLowerCase());
		}

		public void putIc(String key, String val) {
			this.put(key.toLowerCase(), val);
		}
	}

	private final HashMap<String, String> data;
	private final HashMap<Integer, Gift> gifts;
	private int id;

	public Donor(int id) {
		data = new HashMap<String, String>();
		gifts = new HashMap<Integer, Gift>();
		this.id = id;
		this.putData("account", String.format("%06d", id));
		this.putData("type", "I"); // default to individual
	}

	public void addGift(Gift gift) {
		gifts.put(gift.recnum, gift);
	}

	public void clearGifts() {
		gifts.clear();
	}

	public String getData(String key) {
		String output = data.get(key.toLowerCase() != null ? key.toLowerCase()
				: "");
		return output != null ? output : "";
	}

	public HashMap<Integer, Gift> getGifts() {
		return gifts;
	}

	public int getId() {
		return id;
	}

	public String[] getKeys() {
		Set<String> keys = data.keySet();
		return keys.toArray(new String[0]);
	}

	public void putData(String key, String value) {
		data.put(key.toLowerCase(), value);
	}
}
