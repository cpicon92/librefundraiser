package net.sf.librefundraiser.io;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.librefundraiser.Main;

public class DonorData {
	private Type type = Type.INDIVIDUAL;
	private Date changedate = new Date();
	private boolean obsolete = false;
	private String 	spousefrst = "", 
	state = "", 
	lastname = "", 
	address1 = "",
	address2 = "", 
	contact = "", 
	city = "", 
	homephone = "",
	workphone = "", 
	zip = "", 
	fax = "", 
	spouselast = "", 
	web = "", 
	firstname = "", 
	mailname = "", 
	email2 = "", 
	country = "",	
	email = "", 
	salutation = "", 
	category = "",
	source = "",
	notes = "";

	private Map<String, Object> custom = new HashMap<>();

	public static enum Type {
		INDIVIDUAL, BUSINESS, NONPROFIT, OTHER;
		@Override
		public String toString() {
			return super.toString().substring(0,1);
		}
		public String getName() {
			String n = super.toString();
			return n.substring(0,1) + n.substring(1).toLowerCase();
		}
		public static Type forName(String n) {
			try {
				return Type.valueOf(n);
			} catch (IllegalArgumentException e) {
				return Type.OTHER;
			}
		}
	}
	
	public String getData(String key) {
		for (Method m : this.getClass().getMethods()) {
			if (m.getName().equalsIgnoreCase("get" + key) && m.getParameterTypes().length == 0) {
				try {
					return String.valueOf(m.invoke(this));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public void putData(String key, String value) {
		try {
			Field f = this.getClass().getDeclaredField(key);
			f.setAccessible(true);
			if (f.getType().equals(DonorData.Type.class)) {
				if (value.equalsIgnoreCase("i")) value = "INDIVIDUAL";
				else if (value.equalsIgnoreCase("b")) value = "BUSINESS";
				else if (value.equalsIgnoreCase("n") || value.equalsIgnoreCase("np")) value = "NONPROFIT";
				f.set(this, Type.forName(value.toUpperCase()));
			} else if (f.getType().equals(String.class)) {
				f.set(this, value);
			} else {
				System.err.println("Field " + key + " is not a string");
			}
		} catch (NoSuchFieldException e) {
			//TODO support non-text custom fields
			if (Main.getDonorDB().customFieldExists(key)) {
				this.custom.put(key, value);
			} else {
				System.err.println("No donor field " + key);
			}
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Date getChangedate() {
		return changedate;
	}

	public void setChangedate(Date changedate) {
		this.changedate = changedate;
	}

	public String getSpousefrst() {
		return spousefrst;
	}

	public void setSpousefrst(String spousefrst) {
		this.spousefrst = spousefrst;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getHomephone() {
		return homephone;
	}

	public void setHomephone(String homephone) {
		this.homephone = homephone;
	}

	public String getWorkphone() {
		return workphone;
	}

	public void setWorkphone(String workphone) {
		this.workphone = workphone;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getSpouselast() {
		return spouselast;
	}

	public void setSpouselast(String spouselast) {
		this.spouselast = spouselast;
	}

	public String getWeb() {
		return web;
	}

	public void setWeb(String web) {
		this.web = web;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getMailname() {
		return mailname;
	}

	public void setMailname(String mailname) {
		this.mailname = mailname;
	}

	public String getEmail2() {
		return email2;
	}

	public void setEmail2(String email2) {
		this.email2 = email2;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSalutation() {
		return salutation;
	}

	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}

	public String getNotes() {
		//dbase import sometimes is full of NUL bytes...
		return notes.replace("\u0000", "");
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public boolean isObsolete() {
		return obsolete;
	}

	public void setObsolete(boolean active) {
		this.obsolete = active;
	}

	public void putCustom(String key, Object value) {
		custom.put(key, value);
	}
	
	public Object getCustom(String key) {
		return custom.get(key);
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
