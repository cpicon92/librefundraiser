package net.sf.librefundraiser;
import java.util.HashMap;
import java.util.Set;


public class Donor {
	public static class Gift extends HashMap<String,String> {
		private static final long serialVersionUID = -9169351258332556336L;
		public final int recnum;
		public Gift(int recnum) {
			this.recnum = recnum;
		}
		public void putIc(String key, String val) {
			this.put(key.toLowerCase(),val);
		}
		public String getIc(String key) {
			return this.get(key.toLowerCase());
		}
	}
	private final HashMap<String,String> data;
	private final HashMap<Integer,Gift> gifts;
	private int id;
	public Donor(int id) {
		data = new HashMap<String,String>();
		gifts = new HashMap<Integer,Gift>();
		this.id = id;
	}
	public String getData(String key) {
		return data.get(key.toLowerCase());
	}
	public void putData(String key, String value) {
		data.put(key.toLowerCase(), value);
	}
	public void addGift(Gift gift) {
		gifts.put(gift.recnum, gift);
	}
	public HashMap<Integer, Gift> getGifts() {
		return gifts;
	}
	public String[] getKeys() {
		Set<String> keys = data.keySet();
		return keys.toArray(new String[0]);
	}
	public int getId() {
		return id;
	}
}
