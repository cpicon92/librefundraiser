package net.sf.librefundraiser.io;

import java.util.Comparator;
import java.util.Currency;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

public class Money implements Comparable<Money> {
	private final Currency currency;
	private final int amount, fractionDigits;

	private static final SortedMap<Currency, Locale> currencyLocaleMap;

	static {
		currencyLocaleMap = new TreeMap<>(new Comparator<Currency>() {
			@Override
			public int compare(Currency c1, Currency c2){
				return c1.getCurrencyCode().compareTo(c2.getCurrencyCode());
			}
		});
		for (Locale locale : Locale.getAvailableLocales()) {
			try {
				Currency currency = Currency.getInstance(locale);
				currencyLocaleMap.put(currency, locale);
			}catch (Exception e){
			}
		}
	}

	public Money(String formatted, String currency) {
		this.currency = Currency.getInstance(currency);
		String[] parts = formatted.split("\\.", 2);
		try {
			if (parts.length == 0) {
				throw new MoneyParsingException("Input string is empty");
			} else if (parts.length == 1) {
				this.fractionDigits = 0;
				this.amount = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
			} else {
				int characteristic = Integer.parseInt(parts[0].replaceAll("[^0-9]", "")), 
						mantissa = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
				this.fractionDigits = parts[1].replaceAll("[^0-9]", "").length();
				this.amount = (int) (characteristic * Math.pow(10, fractionDigits) + mantissa);
			}
		} catch (NumberFormatException e) {
			throw new MoneyParsingException("Input string is invalid", e);
		}

	}

	public Money(String formatted) {
		this(formatted, "USD");
	}

	public Money(int amount, String currency, int fractionDigits) {
		this.amount = amount;
		this.currency = Currency.getInstance(currency);
		this.fractionDigits = fractionDigits;
	}

	public Money(int amount, String currency) {
		this(amount, currency, Currency.getInstance(currency).getDefaultFractionDigits());
	}

	public Money(int amount) {
		this(amount, "USD");
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder(getSymbol(this.currency));
		sb.append(amount / (int) Math.pow(10, fractionDigits));
		if (fractionDigits > 0) {
			sb.append(".");
			sb.append(String.format("%0" + fractionDigits + "d", amount % (int) Math.pow(10, fractionDigits)));
		}
		return sb.toString();
	}

	public static String getSymbol(String currencyCode) {
		Currency currency = Currency.getInstance(currencyCode);
		return currency.getSymbol(currencyLocaleMap.get(currency));
	}
	
	public static String getSymbol(Currency c) {
		return getSymbol(c.getCurrencyCode());
	}

	@Override
	public int compareTo(Money o) {
		return Integer.compare(this.amount, o.amount);
	}

}
