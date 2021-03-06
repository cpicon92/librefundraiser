package net.sf.librefundraiser.io;

import java.util.Date;

public class Gift implements Comparable<Gift> {
	public final int recnum;
	private String source = "", note = "";
	private int account;
	private boolean letter;
	private Date dt_entry = new Date(), dategiven = new Date();
	private Money amount = new Money(0);
	
	public Gift(int recnum) {
		this.recnum = recnum;
	}

	@Override
	public int compareTo(Gift o) {
		if (this.dategiven == null && o.dategiven == null) return 0;
		if (this.dategiven == null) return 1;
		if (o.dategiven == null ) return -1;
		return -1 * this.dategiven.compareTo(o.dategiven);
	}

	public String getSource() {
		if (source == null) return "";
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
		if (note == null) return "";
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
	public String getRecNum() {
		return String.format("%06d", recnum);
	}

	@Override
	public String toString() {
		return "Gift [recnum=" + recnum + ", source=" + source + ", note=" + note + ", account=" + account + ", letter="
				+ letter + ", dt_entry=" + dt_entry + ", dategiven=" + dategiven + ", amount=" + amount + "]";
	}
	
	public Gift copy() {
		return this.copy(recnum);
	}
	
	public Gift copy(int recnum) {
		Gift copy = new Gift(recnum);
		copy.account = account;
		copy.amount = amount;
		copy.dategiven = dategiven;
		copy.dt_entry = dt_entry;
		copy.letter = letter;
		copy.note = note;
		copy.source = source;
		return copy;
	}
}