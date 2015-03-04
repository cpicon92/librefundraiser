package net.sf.librefundraiser.io;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

public class DonorData {
	private Type type = Type.I;
	private Date changedate;
	private String 	spousefrst, 
	state, 
	lastname, 
	address1,
	address2, 
	contact, 
	city, 
	homephone,
	workphone, 
	zip, 
	fax, 
	spouselast, 
	web, 
	category1, 
	firstname, 
	category2, 
	mailname, 
	email2, 
	country,	
	email, 
	salutation, 
	notes;

	public static enum Type {
		I, B;
	}
	
	public String getData(String key) {
		for (Method m : this.getClass().getMethods()) {
			if (m.getName().toLowerCase().equals("get" + key.toLowerCase())) {
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
			Field f = this.getClass().getField(key);
			if (f.getType().equals(String.class)) {
				f.set(this, value);
			} else {
				System.err.println("Field " + key + " is not a string");
			}
		} catch (NoSuchFieldException e) {
//			System.err.println("No donor field " + key);
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

	public String getCategory1() {
		return category1;
	}

	public void setCategory1(String category1) {
		this.category1 = category1;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getCategory2() {
		return category2;
	}

	public void setCategory2(String category2) {
		this.category2 = category2;
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
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

}
