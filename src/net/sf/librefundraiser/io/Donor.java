package net.sf.librefundraiser.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Donor {
	public final DonorData data = new DonorData();
	private final Map<Integer,Gift> gifts = new HashMap<>();
	public final int id;
	
	public Donor(int id) {
		this.id = id;
	}
	
	@Deprecated
	public String getData(String key) {
		key = key.toLowerCase();
		if (key.equals("account")) {
			return String.format("%06d",id);
		}
		
		for (Method m : GiftStats.class.getMethods()) {
			if (m.getName().toLowerCase().equals("get" + key)) {
				try {
					return String.valueOf(m.invoke(this.getGiftStats()));
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		
		String output = data.getData(key.toLowerCase()!=null?key.toLowerCase():"");
		return output != null ? output : "";
	}
	
	public GiftStats getGiftStats() {
		return new GiftStats(this.getGifts());
	}

	public void putData(String key, String value) {
		data.putData(key.toLowerCase(), value);
	}
	
	public void addGift(Gift gift) {
		this.gifts.put(gift.recnum, gift);
	}
	
	public void addGifts(Collection<Gift> gifts) {
		for (Gift g : gifts) {
			this.gifts.put(g.recnum, g);
		}
	}
	
	public Map<Integer, Gift> getGifts() {
		return gifts;
	}
	
	public String[] getKeys() {
		return this.data.getKeys();
	}
	
	public int getId() {
		return id;
	}
	
	public void clearGifts() {
		gifts.clear();
	}
	
	/**
	 * Check if the value of any field contains the filter string. 
	 * Used for the DonorListFilter
	 * @param filter string to compare against
	 * @return true if any field contains the filter, false otherwise
	 */
	public boolean match(String filter) {
		//TODO get rid of this and other methods that are just references
		return this.data.match(filter);
	}


}
