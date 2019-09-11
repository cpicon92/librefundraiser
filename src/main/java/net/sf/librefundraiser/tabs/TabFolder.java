package net.sf.librefundraiser.tabs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.sf.librefundraiser.Util;

public class TabFolder extends Composite {

	private Composite cmpTabs, cmpControlArea;
	private final List<TabFolderListener> tabFolderListeners = new ArrayList<>();
	private final List<SelectionListener> selectionListeners = new ArrayList<>();
	private TabItem currentSelection = null;
	private int maxTabWidth = 200;
	private ArrayDeque<TabItem> selectionOrder = new ArrayDeque<>();

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */

	public TabFolder(Composite parent, int style) {
		super(parent, style);
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		cmpTabs = new Composite(this, SWT.NONE);
		final Color gradBottom = cmpTabs.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		final Color colorShadow = Util.changeColorBrightness(cmpTabs.getDisplay(), gradBottom, -20);
//		final Color colorTabLines = compositeTabs.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
		final Color colorTabLines = Util.changeColorBrightness(cmpTabs.getDisplay(), gradBottom, -50);
		cmpTabs.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Rectangle gcSize = cmpTabs.getClientArea();
				e.gc.setForeground(gradBottom);
				e.gc.setBackground(colorShadow);
				e.gc.fillGradientRectangle(0, 0, gcSize.width, gcSize.height-1, true);
				e.gc.setForeground(colorTabLines);
				e.gc.drawLine(0, gcSize.height-1, gcSize.width, gcSize.height-1);
			}
		});
		cmpTabs.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				distributeTabs();
			}
		});
		cmpTabs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_compositeTabs = new GridLayout(1, true);
		gl_compositeTabs.horizontalSpacing = 2;
		gl_compositeTabs.marginWidth = 0;
		gl_compositeTabs.marginHeight = 0;
		cmpTabs.setLayout(gl_compositeTabs);

		cmpControlArea = new Composite(this, SWT.NONE);
		cmpControlArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cmpControlArea.setLayout(new StackLayout());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	void addTab(TabItem i) {
		i.setParent(cmpTabs);
		i.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		((GridLayout)cmpTabs.getLayout()).numColumns = cmpTabs.getChildren().length;
		distributeTabs();
	}

	void suckUpChildren() {
		for (Control c : this.getChildren()) {
			if (c != cmpTabs && c != cmpControlArea) {
				c.setParent(cmpControlArea);
//				c.setVisible(true);
			}
		}
		this.layout(new Control[]{cmpControlArea}, SWT.CHANGED);
		this.layout(true, true);
	}

	public void setSelection(TabItem item) {
		if (tabCount() == 0 || item.isDisposed() || !Arrays.asList(cmpTabs.getChildren()).contains(item)) {
			throw new IllegalArgumentException("Non-child tab item cannot be set as selection");
		}
		selectionOrder.remove(item);
		selectionOrder.push(item);
		currentSelection = item;
		for (SelectionListener l : selectionListeners) {
			l.widgetSelected(null);
		}
		try {
			((StackLayout)cmpControlArea.getLayout()).topControl = item.getControl();
			item.getControl().setVisible(true);
			for (Control c : cmpControlArea.getChildren()) {
				if (c != item.getControl()) c.setVisible(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Control c : cmpTabs.getChildren()) {
			try {
				TabItem i = (TabItem) c;
				i.setSelected(i == item);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void closeTab(TabItem item) {
		if (!item.isClosable()) return;
		for (TabFolderListener l : tabFolderListeners) {
			TabFolderEvent e = new TabFolderEvent(item);
			l.close(e);
			if (!e.doit) return;
		}
		selectionOrder.remove(item);
		item.getControl().dispose();
		item.dispose();
		TabItem lastSelection = null;
		while (!selectionOrder.isEmpty()) {
			lastSelection = selectionOrder.pop();
			if (lastSelection != item && !lastSelection.isDisposed()) break;
		}
		if (lastSelection != null) {
			this.setSelection(lastSelection);
		} else {
			//this should only happen when there are no more tabs
			this.currentSelection = null; 
			for (SelectionListener l : selectionListeners) {
				l.widgetSelected(null);
			}
		}
		((GridLayout)cmpTabs.getLayout()).numColumns = cmpTabs.getChildren().length;
		distributeTabs();
		this.layout(new Control[]{cmpControlArea}, SWT.CHANGED);
		this.layout(true, true);
	}
	
	public void closeAllTabs(TabItem[] exceptThese) {
		closeAllTabs(Arrays.asList(exceptThese));
	}
	
	public void closeAllTabs(List<TabItem> exceptThese) {
		if (tabCount() == 0) return;
		for (Control c : cmpTabs.getChildren()) {
			try {
				TabItem i = (TabItem) c;
				if (!exceptThese.contains(i)) closeTab(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void closeToRight(TabItem index) {
		if (tabCount() == 0) return;
		boolean passed = false;
		for (Control c : cmpTabs.getChildren()) {
			//TODO figure out what try-catches like this are for
			try {
				TabItem i = (TabItem) c;
				if (passed) closeTab(i);
				if (i.equals(index)) passed = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addTabFolderListener(TabFolderListener l) {
		this.tabFolderListeners.add(l);
	}
	
	public void addSelectionListener(SelectionListener l) {
		this.selectionListeners.add(l);
	}
	
	public TabItem getSelection() {
		return currentSelection;
	}
	
	public TabItem[] getTabs() {
		ArrayList<TabItem> items = new ArrayList<>();
		for (Control c : cmpTabs.getChildren()) {
			try {
				items.add((TabItem) c);
			} catch (Exception e) {
				
			}
		}
		return items.toArray(new TabItem[]{});
	}
	
	public int tabCount() {
		return cmpTabs.getChildren().length;
	}

	public int getMaxTabWidth() {
		return maxTabWidth;
	}

	public void setMaxTabWidth(int maxTabWidth) {
		this.maxTabWidth = maxTabWidth;
	}
	
	private void distributeTabs() {
		Point size = cmpTabs.getSize();
		int maxWidth = maxTabWidth * cmpTabs.getChildren().length;
		GridLayout gridLayout = (GridLayout) cmpTabs.getLayout();
		if (size.x > maxWidth) {
			gridLayout.marginRight = size.x - maxWidth;
		} else {
			gridLayout.marginRight = 0;
		}
	}
	
	protected void onNewTab(TabItem item) {
		boolean doit = true;
		for (TabFolderListener l : tabFolderListeners) {
			TabFolderEvent e = new TabFolderEvent(item);
			l.open(e);
			if (!e.doit) doit = false;
		}
		if (!doit) {
			item.getControl().dispose();
			item.dispose();
			TabItem lastSelection = null;
			while (!selectionOrder.isEmpty()) {
				lastSelection = selectionOrder.pop();
				if (lastSelection != item && !lastSelection.isDisposed()) break;
			}
			if (lastSelection != null) {
				this.setSelection(lastSelection);
			}
			((GridLayout)cmpTabs.getLayout()).numColumns = cmpTabs.getChildren().length;
			distributeTabs();
			this.layout(new Control[]{cmpControlArea}, SWT.CHANGED);
			this.layout(true, true);
		}
	}
}
