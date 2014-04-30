package net.sf.librefundraiser.templates;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import net.sf.librefundraiser.templates.Template.TemplateDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class TemplateManager {
	private final Gson gson;
	private final Map<String, Template[]> templates;
	public TemplateManager(String type) throws IOException {
		gson = new GsonBuilder().registerTypeAdapter(Template.class, new TemplateDeserializer()).create();
		InputStream jsonFile = TemplateManager.class.getResourceAsStream("/net/sf/librefundraiser/templates/" + type + ".json");
		Map<String, Template[]> templates = gson.fromJson(new InputStreamReader(jsonFile, "UTF-8"), new TypeToken<Map<String, Template[]>>(){}.getType());
		jsonFile.close();
		this.templates = templates;
	}
	
	public Template[] getTemplates(String type, String field) {
		return this.templates.get(type + "_" + field);
	}
}
