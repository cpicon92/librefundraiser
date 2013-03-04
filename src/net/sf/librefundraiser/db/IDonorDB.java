package net.sf.librefundraiser.db;

import java.sql.Connection;
import java.util.HashMap;

import net.sf.librefundraiser.Donor;

public interface IDonorDB {

	Connection getConnection();

	HashMap<String, String> quickSearch(String query);

	Donor[] getDonors(String query, boolean fetchGifts);

	void saveDonor(Donor donor);
	
	void saveDonors(Donor[] donors);
	
	void deleteDonor(int account);
	
	void deleteGift(int refnum);

	Donor[] getDonors();
	
	void refreshGifts(Donor donor);

	int getMaxAccount();

	int getMaxRecNum();

	double getTotalGifts(Donor donor);

	double getYTD(Donor donor);

	double getLargestGift(Donor donor);

	String getLastGiftDate(Donor donor);

	String getFirstGiftDate(Donor donor);

	double getLastGift(Donor donor);

	String[] getPreviousValues(String column, String table);
	
	String getDbName();
	
	void setDbName(String name);
	
	int getDbVersion();
	
	String getDbPath();

	String getLastEntryDate(Donor donor);

	double getLastEntryAmount(Donor donor);

}