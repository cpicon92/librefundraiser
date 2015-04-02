package net.sf.librefundraiser.gui.flextable;

import org.eclipse.swt.events.SelectionListener;

public interface FlexTableSelectionListener<T> extends SelectionListener {
	void widgetSelected(FlexTableSelectionEvent<T> e);
}
