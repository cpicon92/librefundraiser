package net.sf.librefundraiser.tabs;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class TabFolder extends Composite {

	private Composite compositeTabs;
	private Composite compositeControlArea;
	private final ArrayList<TabFolderListener> listeners = new ArrayList<TabFolderListener>();
	private final ArrayList<SelectionAdapter> selectionAdapters = new ArrayList<SelectionAdapter>();
	private TabItem currentSelection = null;
	private int maxTabWidth = 200;

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

		compositeTabs = new Composite(this, SWT.NONE);
		final Color gradBottom = compositeTabs.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		int c = 20;
		final Color colorShadow = new Color(compositeTabs.getDisplay(), gradBottom.getRed() - c, gradBottom.getGreen() - c, gradBottom.getBlue() - c);
		compositeTabs.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle gcSize = compositeTabs.getClientArea();
				e.gc.setForeground(gradBottom);
				e.gc.setBackground(colorShadow);
				e.gc.fillGradientRectangle(0, 0, gcSize.width, gcSize.height-1, true);
				e.gc.setForeground(new Color(compositeTabs.getDisplay(), 127, 127, 127));
				e.gc.drawLine(0, gcSize.height-1, gcSize.width, gcSize.height-1);
			}
		});
		compositeTabs.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				distributeTabs();
			}
		});
		compositeTabs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_compositeTabs = new GridLayout(1, true);
		gl_compositeTabs.horizontalSpacing = 2;
		gl_compositeTabs.marginWidth = 0;
		gl_compositeTabs.marginHeight = 0;
		compositeTabs.setLayout(gl_compositeTabs);

		compositeControlArea = new Composite(this, SWT.NONE);
		compositeControlArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeControlArea.setLayout(new StackLayout());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	void createItem(TabItem i) {
		i.setParent(compositeTabs);
		i.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		((GridLayout)compositeTabs.getLayout()).numColumns = compositeTabs.getChildren().length;
		distributeTabs();
	}

	void suckUpChildren() {
		for (Control c : this.getChildren()) {
			if (c != compositeTabs && c != compositeControlArea) {
				c.setParent(compositeControlArea);
//				c.setVisible(true);
			}
		}
		this.changed(new Control[]{compositeControlArea});
		this.layout(true, true);
	}

	public void setSelection(TabItem item) {
		currentSelection = item;
		for (SelectionAdapter a : selectionAdapters) {
			a.widgetSelected(null);
		}
		try {
			((StackLayout)compositeControlArea.getLayout()).topControl = item.getControl();
			item.getControl().setVisible(true);
			for (Control c : compositeControlArea.getChildren()) {
				if (c != item.getControl()) c.setVisible(false);
			}
		} catch (Exception e) {

		}
		for (Control c : compositeTabs.getChildren()) {
			try {
				TabItem i = (TabItem) c;
				i.setSelected(i == item);
			} catch (Exception e) {

			}
		}
	}
	
	public void closeTab(TabItem item) {
		for (TabFolderListener l : listeners) {
			TabFolderEvent e = new TabFolderEvent(item);
			l.close(e);
			if (!e.doit) return;
		}
		item.getControl().dispose();
		item.dispose();
		Control[] children = compositeTabs.getChildren();
		if (children.length > 0) { 
			Control lastChild = children[children.length - 1];
			try {
				setSelection((TabItem) lastChild);
			} catch (Exception e) {
				
			}
		}
		((GridLayout)compositeTabs.getLayout()).numColumns = compositeTabs.getChildren().length;
		distributeTabs();
		this.changed(new Control[]{compositeControlArea});
		this.layout(true, true);
	}
	
	public void addTabFolderListener(TabFolderListener l) {
		this.listeners.add(l);
	}
	
	public void addSelectionListener(SelectionAdapter a) {
		this.selectionAdapters.add(a);
	}
	
	public TabItem getSelection() {
		return currentSelection;
	}
	
	public TabItem[] getItems() {
		ArrayList<TabItem> items = new ArrayList<TabItem>();
		for (Control c : compositeTabs.getChildren()) {
			try {
				items.add((TabItem) c);
			} catch (Exception e) {
				
			}
		}
		return items.toArray(new TabItem[]{});
	}

	public int getMaxTabWidth() {
		return maxTabWidth;
	}

	public void setMaxTabWidth(int maxTabWidth) {
		this.maxTabWidth = maxTabWidth;
	}
	
	private void distributeTabs() {
		Point size = compositeTabs.getSize();
		int maxWidth = maxTabWidth * compositeTabs.getChildren().length;
		GridLayout gridLayout = (GridLayout) compositeTabs.getLayout();
		if (size.x > maxWidth) {
			gridLayout.marginRight = size.x - maxWidth;
		} else {
			gridLayout.marginRight = 0;
		}
	}
}
