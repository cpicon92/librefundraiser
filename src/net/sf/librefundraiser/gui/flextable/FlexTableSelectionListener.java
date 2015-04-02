package net.sf.librefundraiser.gui.flextable;


public interface FlexTableSelectionListener<T> {
	void widgetSelected(FlexTableSelectionEvent<T> e);
	void widgetDefaultSelected(FlexTableSelectionEvent<T> e);
}
