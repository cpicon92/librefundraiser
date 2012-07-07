package net.sf.librefundraiser.db;

import java.sql.Connection;
import java.util.HashMap;

import net.sf.librefundraiser.Donor;

public interface DonorDB {

	void deleteDonor(int account);

	void deleteGift(int refnum);

	Connection getConnection();

	Donor[] getDonors();

	Donor[] getDonors(String query, boolean fetchGifts);

	String getFirstGiftDate(Donor donor);

	double getLargestGift(Donor donor);

	double getLastGift(Donor donor);

	String getLastGiftDate(Donor donor);

	int getMaxAccount();

	int getMaxRecNum();

	String[] getPreviousValues(String column, String table);

	double getTotalGifts(Donor donor);

	double getYTD(Donor donor);

	HashMap<String, String> quickSearch(String query);

	void refreshGifts(Donor donor);

	void saveDonor(Donor donor);

}