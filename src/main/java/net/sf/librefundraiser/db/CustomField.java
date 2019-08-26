package net.sf.librefundraiser.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomField {
	public String name = "";
	public int decDigits = 0;
	public Type type;
	public List<String> choices;
	
	public enum Type {
		TEXT, NUMBER, CHOICE, BINARY;
		public String getName() {
			String n = super.toString();
			return n.substring(0,1) + n.substring(1).toLowerCase();
		}
	}

	public CustomField copy() {
		CustomField c = new CustomField();
		c.type = this.type;
		c.name = this.name;
		c.decDigits = this.decDigits;
		if (type == Type.CHOICE && choices != null) {
			List<String> choices = new ArrayList<>(this.choices.size());
			Collections.copy(choices, this.choices);
			c.choices = choices;
		}
		return c;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDecDigits() {
		if (type != Type.NUMBER) throw new RuntimeException("Non-number type cannot have digits");
		return decDigits;
	}

	public void setDecDigits(int decDigits) {
		if (type != Type.NUMBER) throw new IllegalStateException("Cannot set decimal digits on non-number type");
		this.decDigits = decDigits;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
		if (type == Type.CHOICE && this.type != Type.CHOICE) this.choices = new ArrayList<>();
		if (type != Type.CHOICE) this.choices = null;
	}

	public List<String> getChoices() {
		if (type != Type.CHOICE) return null;
		return choices;
	}
	
	
}
