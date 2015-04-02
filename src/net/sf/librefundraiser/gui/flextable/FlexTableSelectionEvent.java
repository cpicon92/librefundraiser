package net.sf.librefundraiser.gui.flextable;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;

public class FlexTableSelectionEvent<T> extends SelectionEvent {
	
	private static final long serialVersionUID = -7341243391701993864L;
	
	public FlexTableSelectionEvent(Event e) {
		super(e);
	}

}
