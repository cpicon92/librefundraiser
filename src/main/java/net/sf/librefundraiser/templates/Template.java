package net.sf.librefundraiser.templates;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class Template {
	private final String template;
	private final List<String> fields;
	private final Pattern pattern = Pattern.compile("<[^>]*>");
	public Template(String template) {
		this.template = template;
		Matcher matcher = pattern.matcher(template);
		List<String> fields = new ArrayList<>();
		while (matcher.find()) {
			fields.add(matcher.group());
		}
		this.fields = fields;
	}
	@Override
	public String toString() {
		return "Template [template=" + template + ", fields=" + fields + "]";
	}
	public static class TemplateDeserializer implements JsonDeserializer<Template> {
		@Override
		public Template deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
			String jsonTemplate = json.getAsString();
			return new Template(jsonTemplate);
		}
	}
	public int getFieldCount() {
		return fields.size();
	}
	public String process(String... fieldValues) {
		return null;
	}
}
