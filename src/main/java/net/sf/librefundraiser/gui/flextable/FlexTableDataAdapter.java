package net.sf.librefundraiser.gui.flextable;

public class FlexTableDataAdapter<T> implements FlexTableDataProvider<T> {

	@Override
	public T get(int i) {
		return null;
	}

	@Override
	public String get(int i, int field) {
		return null;
	}

	@Override
	public String[] getHeaders() {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public int columnCount() {
		return 0;
	}

	@Override
	public void refresh() {
	}

	@Override
	public boolean sort(int field) {
		return false;
	}

	@Override
	public void setSummaryMode(boolean summaryMode) {
	}

	@Override
	public int getSortField() {
		return -1;
	}

	@Override
	public boolean getSortAsc() {
		return false;
	}

	@Override
	public String getFilter() {
		return null;
	}

	@Override
	public void setFilter(String filter) {		
	}

}
