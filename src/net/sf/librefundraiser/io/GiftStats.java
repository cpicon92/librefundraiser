package net.sf.librefundraiser.io;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

public class GiftStats {
	private Money allTime, yearToDt, largest, lastAmt, lastEntAmt;
	private Date lastGiveDt, firstGift, lastEntDt;

	protected GiftStats(Map<Integer, Gift> gifts) {
		Money allTime = new Money(0), 
		yearToDt = new Money(0), 
		largest = new Money(0), 
		lastEntAmt = new Money(0), 
		lastAmt = new Money(0);
		
		// needed for ytd
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		Date newYear = cal.getTime();

		for (Gift g : gifts.values()) {
			Money amount = g.getAmount();
			Date dateGiven = g.getDategiven();
			Date dtEntry = g.getDt_entry();
			
			//TODO add += method to Money class
			allTime = allTime.add(amount);
			if (dateGiven.compareTo(newYear) > 0) yearToDt = yearToDt.add(amount);
			if (amount.compareTo(largest) > 0) largest = amount;
			if (this.lastGiveDt == null || dateGiven.compareTo(this.lastGiveDt) > 0) {
				this.lastGiveDt = dateGiven;
				lastAmt = amount;
			}
			if (this.firstGift == null || dateGiven.compareTo(this.firstGift) < 0) this.firstGift = dateGiven;
			if (this.lastEntDt == null || dtEntry.compareTo(this.lastEntDt) > 0) {
				this.lastEntDt = dtEntry;
				lastEntAmt = amount;
			}
		}
		
		this.allTime = allTime;
		this.yearToDt = yearToDt;
		this.largest = largest;
		this.lastEntAmt = lastEntAmt;
		this.lastAmt = lastAmt;

	}

	public Date getLastGiveDt() {
		return lastGiveDt;
	}

	public Date getFirstGift() {
		return firstGift;
	}

	public Date getLastEntDt() {
		return lastEntDt;
	}

	public Money getAllTime() {
		return allTime;
	}

	public Money getYearToDt() {
		return yearToDt;
	}

	public Money getLargest() {
		return largest;
	}

	public Money getLastAmt() {
		return lastAmt;
	}

	public Money getLastEntAmt() {
		return lastEntAmt;
	}
}
