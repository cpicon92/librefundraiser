package net.sf.librefundraiser.db;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import net.sf.librefundraiser.Donor;
import net.sf.librefundraiser.ProgressListener;

public interface IDatabase {

	public static final DateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	String getDbPath();

	public static final String[] donorFields = new String[] { "ACCOUNT", "TYPE", "FIRSTNAME", "LASTNAME", "SPOUSEFRST",
			"SPOUSELAST", "SALUTATION", "HOMEPHONE", "WORKPHONE", "FAX", "CATEGORY1", "CATEGORY2", "CONTACT",
			"MAILNAME", "ADDRESS1", "ADDRESS2", "CITY", "STATE", "ZIP", "COUNTRY", "ENTRYDATE", "CHANGEDATE", "NOTES",
			"LASTGIVEDT", "LASTAMT", "ALLTIME", "YEARTODT", "FIRSTGIFT", "LARGEST", "FILTER", "EMAIL", "LASTENTDT",
			"LASTENTAMT", "EMAIL2", "WEB" };
	public static final String[] giftFields = new String[] { "ACCOUNT", "AMOUNT", "DATEGIVEN", "LETTER", "DT_ENTRY",
			"SOURCE", "NOTE", "TEMPTOTAL", "RECNUM" };
	public static final String[] dbInfoFields = new String[] { "KEY", "VALUE" };

	Connection getConnection();

	HashMap<String, String> quickSearch(String query);

	Donor[] getDonors(String query);

	void saveDonor(Donor donor);

	void saveDonors(Donor[] donors);

	Donor[] getDonors();

	int getMaxAccount();

	int getUniqueRecNum();

	String[] getPreviousValues(String column, String table);

	void deleteDonor(int id);

	void deleteDonors(int[] ids);

	void deleteGift(int id);

	void refreshGifts(Donor donor);

	String getDbName();

	void setDbName(String name);

	int getDbVersion();

	String getDbInfo(String key);

	void updateAllStats(Donor[] toUpdate, ProgressListener pl);

}