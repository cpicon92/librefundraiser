package net.sf.librefundraiser.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Donor {
	public final DonorData data;
	private final Map<Integer,Gift> gifts = new HashMap<>();
	public final int id; //TODO make id a long
	
	public Donor(int id) {
		this.id = id;
		data = new DonorData();
	}
	
	public Donor(int id, DonorData data, Map<Integer, Gift> gifts) {
		this.id = id;
		this.data = data;
		this.gifts.putAll(gifts);
	}
	
	public Donor(int id, DonorData data, Collection<Gift> gifts) {
		this.id = id;
		this.data = data;
		for (Gift g : gifts) {
			this.gifts.put(g.recnum, g);
		}
	}
	
	public Donor(int id, Donor donor) {
		this(id, donor.data, donor.gifts);
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
		return new GiftStats(this.gifts.values());
	}

	@Deprecated
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
	
	public int giftCount() {
		return gifts.size();
	}
	
	public List<Gift> getGifts() {
		List<Gift> copy = new ArrayList<>(gifts.size());
		for (Gift g : gifts.values()) {
			copy.add(g.copy());
		}
		return copy;
	}
	
	public Gift getGift(int recnum) {
		return this.gifts.get(recnum);
	}
	
	public void deleteGift(int recnum) {
		this.gifts.remove(recnum);
	}
	
	public void deleteGift(Gift g) {
		this.deleteGift(g.recnum);
	}
	
	public String getAccountNum() {
		return String.format("%06d", id);
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
		//TODO find some way to make this much faster
		filter = filter.toLowerCase();
		for (Method m : DonorData.class.getMethods()) {
			if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
				try {
					if (String.valueOf(m.invoke(this.data)).toLowerCase().contains(filter)) {
						return true;
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "Donor " + this.id + " " + this.data;
	}

}
