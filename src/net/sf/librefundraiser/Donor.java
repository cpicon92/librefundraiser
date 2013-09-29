package net.sf.librefundraiser;

import java.util.*;


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
		public double getIcAsDouble(String key) {
			double output = 0;
			try {
				output = Double.parseDouble(this.getIc(key));
			} catch (Exception e) {
				System.err.printf("Attempt to fetch value \"%s\" from gift %d in donor %s as double failed.\n", key, recnum, this.getIc("account"));
			}
			return output;
		}
		public Date getIcAsDate(String key) {
			Date output = null;
			try {
				output = Main.getDateFormat().parse(this.getIc(key));
			} catch (Exception e) {
				System.err.printf("Attempt to fetch value \"%s\" from gift %d in donor %s as double failed.\n", key, recnum, this.getIc("account"));
			}
			return output;
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
	private final int id;
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
	public void putData(String key, double value) {
		data.put(key.toLowerCase(), "" + value);
	}
	public void putData(String key, Date value) {
		data.put(key.toLowerCase(), value == null ? "" : Main.getDateFormat().format(value));
	}
	public void addGift(Gift gift) {
		this.gifts.put(gift.recnum, gift);
		this.recalculateGiftStats();
	}
	public void addGifts(Collection<Gift> gifts) {
		for (Gift g : gifts) {
			this.gifts.put(g.recnum, g);
		}
		this.recalculateGiftStats();
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
	public void recalculateGiftStats() {
		double allTime = 0;
		double yearToDt = 0;
		double largest = 0;
		Date lastGiveDt = null;
		Date firstGift = null;
		double lastAmt = 0;
		Date lastEntDt = null;
		double lastEntAmt = 0;
		
		// needed for ytd
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		Date newYear = cal.getTime();
		
		for (Gift g : this.getGifts().values()) {
			double amount = g.getIcAsDouble("amount");
			Date dateGiven = g.getIcAsDate("dateGiven");
			Date dtEntry = g.getIcAsDate("dt_entry");
			
			allTime += amount;
			if (dateGiven.compareTo(newYear) > 0) yearToDt += amount;
			if (amount > largest) largest = amount;
			if (lastGiveDt == null || dateGiven.compareTo(lastGiveDt) > 0) {
				lastGiveDt = dateGiven;
				lastAmt = amount;
			}
			if (firstGift == null || dateGiven.compareTo(firstGift) < 0) firstGift = dateGiven;
			if (lastEntDt == null || dtEntry.compareTo(lastEntDt) > 0) {
				lastEntDt = dtEntry;
				lastEntAmt = amount;
			}
		}

		this.putData("allTime", allTime);
		this.putData("yearToDt", yearToDt);
		this.putData("largest", largest);
		this.putData("lastGiveDt", lastGiveDt);
		this.putData("firstGift", firstGift);
		this.putData("lastAmt", lastAmt);
		this.putData("lastEntDt", lastEntDt);
		this.putData("lastEntAmt", lastEntAmt);
	}
}
