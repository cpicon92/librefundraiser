package net.sf.librefundraiser.gui.flextable;


public class FlexTableSelectionEvent<T> {
	public int row = -1, column = -1;
	public boolean doit = true;
	public T target;
}
