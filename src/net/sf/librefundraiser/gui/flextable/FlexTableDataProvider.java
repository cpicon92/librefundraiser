package net.sf.librefundraiser.gui.flextable;


public interface FlexTableDataProvider<T> {
	T get(int i);
	String get(int i, int field);
	String[] getHeaders();
	int size();
	int columnCount();
	void refresh();
	boolean sort(int field);
	void setSummaryMode(boolean summaryMode);
	int getSortField();
	String getFilter();
	void setFilter(String filter);
}
