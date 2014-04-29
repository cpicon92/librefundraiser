package net.sf.librefundraiser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.librefundraiser.db.NewerDbVersionException;
import net.sf.librefundraiser.db.SQLite;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class FormatConverter {

	public static void main(String[] args) throws NewerDbVersionException, IOException {
		long startTime = System.currentTimeMillis();
		Gson gson = new Gson();
		OutputStream os = new FileOutputStream(new File("C:/Users/Kristian/Desktop/newformat.lfd"));
		SQLite db = new SQLite("C:/Users/Kristian/Desktop/friends.lfd");
		Donor[] donors = db.getDonors();
		System.out.printf("Got %d donors in %dms%n", donors.length, System.currentTimeMillis() - startTime);
		Map<String, String> dbInfo = db.getDbInfo();
		startTime = System.currentTimeMillis();
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, "UTF-8"));
		writer.beginObject();
		writer.name("info");
		gson.toJson(dbInfo, Map.class, writer);
		writer.name("donors");
        writer.beginArray();
        for (Donor donor: donors) {
            gson.toJson(donor, Donor.class, writer);
        }
        writer.endArray();
        writer.endObject();
		writer.close();
		System.out.printf("Write took %dms%n", System.currentTimeMillis() - startTime);
		startTime = System.currentTimeMillis();
        JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(new File("C:/Users/Kristian/Desktop/newformat.lfd")), "UTF-8"));
        List<Donor> messages = new ArrayList<Donor>();
        reader.beginObject();
        while (reader.hasNext()) {
        	String name = reader.nextName();
        	if (name.equals("info")) {
        		Map<String, String> readInfo = gson.fromJson(reader, Map.class);
        		System.out.println(readInfo);
        	} else if (name.equals("donors")) {
                reader.beginArray();
                while (reader.hasNext()) {
                    Donor message = gson.fromJson(reader, Donor.class);
                    messages.add(message);
                }
                reader.endArray();
        	} else {
        		reader.skipValue();
        	}
        }
        reader.endObject();
        reader.close();
		System.out.printf("Got back %d donors in %dms%n", messages.size(), System.currentTimeMillis() - startTime);
	}

}
