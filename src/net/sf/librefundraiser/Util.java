package net.sf.librefundraiser;

import java.io.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 9/29/13
 * Time: 9:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class Util {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    //	private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
    private final static Properties settings = new Properties();
    private static NumberFormat currency = null;
    public static boolean fileExists (String path) {
        boolean realFile = false;
        try {
            File file = new File(path);
            if (file.exists()) realFile = true;
        } catch (Exception e) {
        }
        return realFile;
    }
    public static boolean fileCreationPossible(String path) throws IOException {
        if (fileExists(path)) {
            throw new IOException(String.format("The file \"%s\" already exists.", path));
        }
        boolean canCreate = true;
        try {
            File file = new File(path);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(" ");
            writer.close();
            if (file.exists()) canCreate = true;
            file.delete();
        } catch (Exception e) {
        }
        return canCreate;
    }
    public static DateFormat getDateFormat() {
        return dateFormat;
    }
    public static void loadSettings() {
        String path = System.getenv("AppData");
        if (path == null) {
            path = System.getProperty("user.home")+"/.librefundraiser/settings.xml";
        } else {
            path = path + "\\LibreFundraiser\\settings.xml";
        }
        try {
            settings.loadFromXML(new BufferedInputStream(new FileInputStream(path)));
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }
    public static void saveSettings() {
        String path = System.getenv("AppData");
        if (path == null) {
            new File(System.getProperty("user.home")+"/.librefundraiser").mkdirs();
            path = System.getProperty("user.home")+"/.librefundraiser/settings.xml";
        } else {
            new File(path + "\\LibreFundraiser").mkdirs();
            path = path + "\\LibreFundraiser\\settings.xml";
        }
        try {
            settings.storeToXML(new FileOutputStream(path),"LibreFundraiser");
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }
    public static void addSetting(String key, String value) {
        settings.setProperty(key, value);
        new Thread(new Runnable() {
            public void run() {
                saveSettings();
            }
        }).start();
    }
    public static String getSetting(String key) {
        return settings.getProperty(key);
    }
    public static String toMoney(double amount) {
        if (currency == null) currency = NumberFormat.getCurrencyInstance();
        return currency.format(amount);
    }
    public static String toMoney(String amount) {
        if (amount.trim().equals("")) amount = "0.00";
        try {
            return toMoney(Double.parseDouble(amount));
        } catch (Exception e) {
            System.err.println("Value \""+amount+"\" could not be parsed as money.");
            return amount;
        }
    }
    public static double fromMoney(String amount) {
        if (currency == null) currency = NumberFormat.getCurrencyInstance();
        try {
            return currency.parse(amount).doubleValue();
        } catch (ParseException e) {}
        return 0;
    }
}
