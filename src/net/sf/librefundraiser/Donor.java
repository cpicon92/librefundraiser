package net.sf.librefundraiser;
import java.util.HashMap;
import java.util.Set;


public class Donor {
	public static class Gift extends HashMap<String,String> implements Comparable<Gift> {
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
		@Override
		public int compareTo(Gift o) {
			String myDate = this.getIc("DATEGIVEN");
			String otherDate = o.getIc("DATEGIVEN");
			if (myDate == null && otherDate == null) return 0;
			if (myDate == null) return -1;
			if (otherDate == null ) return 1;
			return myDate.compareTo(otherDate);
		}
	}
	private final HashMap<String,String> data;
	private final HashMap<Integer,Gift> gifts;
	private int id;
	public Donor(int id) {
		data = new HashMap<String,String>();
		gifts = new HashMap<Integer,Gift>();
		this.id = id;
		this.putData("account", String.format("%06d",id));
		this.putData("type", "I"); //default to individual
	}
	public String getData(String key) {
		String output = data.get(key.toLowerCase()!=null?key.toLowerCase():"");
		return output!=null?output:"";
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
	public void clearGifts() {
		gifts.clear();
	}
	public void updateStats() {
		this.putData("alltime", ""+Main.getDonorDB().getTotalGifts(this));
		this.putData("yeartodt", ""+Main.getDonorDB().getYTD(this));
		this.putData("largest", ""+Main.getDonorDB().getLargestGift(this));
		this.putData("lastgivedt", Main.getDonorDB().getLastGiftDate(this));
		this.putData("firstgift", Main.getDonorDB().getFirstGiftDate(this));
		this.putData("lastamt", ""+Main.getDonorDB().getLastGift(this));
		this.putData("lastentdt", ""+Main.getDonorDB().getLastEntryDate(this));
		this.putData("lastentamt", ""+Main.getDonorDB().getLastEntryAmount(this));
	}
}
