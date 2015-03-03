package net.sf.librefundraiser.io;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;

import net.sf.librefundraiser.Main;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class Gift extends HashMap<String,String> implements Comparable<Gift> {
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
	//TODO: fix this mess
	public static class GiftDeserializer implements JsonDeserializer<Gift> {
		@Override
		public Gift deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
			JsonObject jsonGift = json.getAsJsonObject();
			Gift gift = new Gift(jsonGift.get("recnum").getAsInt());
			for (java.util.Map.Entry<String, JsonElement> e : jsonGift.entrySet()) {
				gift.put(e.getKey(), e.getValue().getAsString());
			}
			return gift;
		}
	}
}