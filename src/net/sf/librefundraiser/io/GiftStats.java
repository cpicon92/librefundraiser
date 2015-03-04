package net.sf.librefundraiser.io;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

public class GiftStats {
	private double allTime, yearToDt, largest, lastAmt, lastEntAmt;
	private Date lastGiveDt, firstGift, lastEntDt;

	protected GiftStats(Map<Integer, Gift> gifts) {
		// needed for ytd
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		Date newYear = cal.getTime();

		for (Gift g : gifts.values()) {
			double amount = g.getIcAsDouble("amount");
			Date dateGiven = g.getIcAsDate("dateGiven");
			Date dtEntry = g.getIcAsDate("dt_entry");

			this.allTime += amount;
			if (dateGiven.compareTo(newYear) > 0) this.yearToDt += amount;
			if (amount > this.largest) this.largest = amount;
			if (this.lastGiveDt == null || dateGiven.compareTo(this.lastGiveDt) > 0) {
				this.lastGiveDt = dateGiven;
				this.lastAmt = amount;
			}
			if (this.firstGift == null || dateGiven.compareTo(this.firstGift) < 0) this.firstGift = dateGiven;
			if (this.lastEntDt == null || dtEntry.compareTo(this.lastEntDt) > 0) {
				this.lastEntDt = dtEntry;
				this.lastEntAmt = amount;
			}
		}

	}

	public double getAllTime() {
		return allTime;
	}

	public double getYearToDt() {
		return yearToDt;
	}

	public double getLargest() {
		return largest;
	}

	public double getLastAmt() {
		return lastAmt;
	}

	public double getLastEntAmt() {
		return lastEntAmt;
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
}
