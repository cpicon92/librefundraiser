package net.sf.librefundraiser.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

public class Gift implements Comparable<Gift> {
	private static final long serialVersionUID = -9169351258332556336L;
	public final int recnum;
	private String source, note;
	private int account;
	private boolean letter;
	private Date dt_entry, dategiven;
	private Money amount;
	
	public Gift(int recnum) {
		this.recnum = recnum;
	}
	
	@Deprecated
	public String getIc(String key) {
		return this.get(key.toLowerCase());
	}
	
	private String get(String key) {
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

	@Override
	public int compareTo(Gift o) {
		if (this.dategiven == null && o.dategiven == null) return 0;
		if (this.dategiven == null) return -1;
		if (o.dategiven == null ) return 1;
		return this.dategiven.compareTo(o.dategiven);
	}

	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Date getDategiven() {
		return dategiven;
	}
	public void setDategiven(Date dategiven) {
		this.dategiven = dategiven;
	}
	public int getAccount() {
		return account;
	}
	public void setAccount(int account) {
		this.account = account;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public boolean isLetter() {
		return letter;
	}
	public void setLetter(boolean letter) {
		this.letter = letter;
	}
	public Date getDt_entry() {
		return dt_entry;
	}
	public void setDt_entry(Date dt_entry) {
		this.dt_entry = dt_entry;
	}
	public Money getAmount() {
		return amount;
	}
	public void setAmount(Money amount) {
		this.amount = amount;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getRecNum() {
		return String.format("%06d", recnum);
	}
}