package net.sf.librefundraiser.db;

import java.sql.Connection;
import java.util.HashMap;

import net.sf.librefundraiser.Donor;

public interface DonorDB {

	public abstract Connection getConnection();

	public abstract HashMap<String, String> quickSearch(String query);

	public abstract Donor[] getDonors(String query, boolean fetchGifts);

	public abstract void saveDonor(Donor donor);

	public abstract Donor[] getDonors();

	public abstract int getMaxAccount();

	public abstract int getMaxRecNum();

	public abstract double getTotalGifts(Donor donor);

	public abstract double getYTD(Donor donor);

	public abstract double getLargestGift(Donor donor);

	public abstract String getLastGiftDate(Donor donor);

	public abstract String getFirstGiftDate(Donor donor);

	public abstract double getLastGift(Donor donor);

	public abstract String[] getPreviousValues(String column, String table);

}