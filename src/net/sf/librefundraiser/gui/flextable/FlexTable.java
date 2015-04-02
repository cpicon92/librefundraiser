package net.sf.librefundraiser.gui.flextable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

public class FlexTable<T> extends Composite {
	
	private int[] columnWidths = new int[0];
	private Rectangle[] positions = new Rectangle[0];
	private FlexTableDataProvider<T> dataProvider;
	private static final int pad = 20, rowHeight = 12;
	private final boolean border;
	private boolean dirty = true;
	private int scrollX = 0, scrollY = 0,
	selectedRow = -1; 

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public FlexTable(Composite parent, int style) {
		super(parent, style & ~SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		border = (style & SWT.BORDER) != 0;
		System.out.println(border ? "has" : "no" + " border");
		this.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				calibrateScrollBars();
			}
		});
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				for (int i = 0; i < positions.length; i++) {
					if (positions[i] != null && positions[i].contains(e.x + scrollX, e.y + scrollY)) {
						int row = i / dataProvider.columnCount(), column = i % dataProvider.columnCount();
						System.out.println("row " + row + ", column " + column + ": " + dataProvider.get(row, column));
						break;
					}
				}
			}
			@Override
			public void mouseUp(MouseEvent e) {
				for (int i = 0; i < positions.length; i++) {
					if (positions[i] != null && positions[i].contains(e.x + scrollX, e.y + scrollY)) {
						int row = i / dataProvider.columnCount();
						selectedRow = row;
						redraw();
						break;
					}
				}
			}
		});
		final ScrollBar hScroll = this.getHorizontalBar(), 
		vScroll = this.getVerticalBar();
		vScroll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scrollY = (int) ((vScroll.getSelection() / (vScroll.getMaximum() - (double) vScroll.getThumb())) * vScroll.getMaximum()); 
				redraw();
			}
		});
		hScroll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scrollX = (int) ((hScroll.getSelection() / (hScroll.getMaximum() - (double) hScroll.getThumb())) * hScroll.getMaximum()); 
				redraw();
			}
		});
		this.addPaintListener(new PaintListener() {
			final Color colorOddRows = changeColorBrightness(getDisplay(), getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND), 70),
			colorEvenRows = getDisplay().getSystemColor(SWT.COLOR_WHITE),
			colorSelectedRow = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION),
			colorBg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND),
			colorLines = changeColorBrightness(getDisplay(), colorOddRows, -50),
			colorSelectedText = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT),
			colorText = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
			final Font arial = new Font(getDisplay(), new FontData("Arial", 10, SWT.NORMAL)),
			arialBold = new Font(getDisplay(), new FontData("Arial", 10, SWT.BOLD));
			@Override
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = getClientArea();
				int caX = clientArea.x, caY = clientArea.y, caW = clientArea.width, caH = clientArea.height;
				int caX1 = caX, caY1 = caY, caX2 = caX + caW, caY2 = caY + caH;
				GC g = e.gc;
				g.setBackground(colorBg);
				g.fillRectangle(clientArea);
				g.setFont(arialBold);
				if (dirty) {
					packColumns(g);
					calibrateScrollBars();
					dirty = false;
				}
				int x = pad, y = 0;
				for (int i = 0; i < dataProvider.size(); i++) {
					for (int col = 0; col < dataProvider.columnCount(); col++) {
					if (col == 0) {
						x = pad;
						y += rowHeight + pad;
						if (i == selectedRow) {
							g.setForeground(colorSelectedText);
							g.setBackground(colorSelectedRow);
						} else {
							g.setBackground(i % 2 == 0 ? colorOddRows : colorEvenRows);
							g.setForeground(colorText);
						}
						g.fillRectangle(0, y - scrollY, caW, rowHeight + pad);
						g.setFont(arialBold);
						g.setBackground(colorLines);
						g.fillRectangle(0, y + rowHeight + pad - 1 - scrollY, caW, 1);
					} else {
						g.setFont(arial);
					}
					positions[i] = new Rectangle(x, y, columnWidths[col], rowHeight + pad);
					if (y - scrollY > caH) break;
					if (x - scrollX > caW) continue;
					g.drawString(String.valueOf(dataProvider.get(i, col)), x - scrollX, y + rowHeight / 2 + 1 - scrollY, true);
					x += columnWidths[col];
				}
				}
				x = pad;
				y = 0;
				g.setBackground(colorEvenRows);
				g.setForeground(colorText);
				g.fillRectangle(0, y, getClientArea().width, rowHeight + pad);
				g.setBackground(colorLines);
				g.fillRectangle(0, y + rowHeight + pad - 2, getClientArea().width, 2);
				g.setFont(arialBold);
				String[] tableHeaders = dataProvider.getHeaders();
				for (int i = 0; i < tableHeaders.length; i++) {
					g.drawString(String.valueOf(tableHeaders[i]), x - scrollX, y + rowHeight / 2 + 1, true);
					x += columnWidths[i];
				}
			}
		});
	}
	
	private void calibrateScrollBars() {
		int w = 0, h = dataProvider.size() * (rowHeight + pad);
		for (int cw : columnWidths) {
			w += cw;
		}
		if (w < 1 || h < 1) return;
		ScrollBar hScroll = this.getHorizontalBar(), vScroll = this.getVerticalBar();
		int vScrollSize = vScroll.getSize().y;
		if (h < vScrollSize) {
			vScroll.setEnabled(false);
		} else {
			vScroll.setEnabled(true);
			vScroll.setSelection(0);
			vScroll.setMinimum(0);
			vScroll.setMaximum(h);
			vScroll.setThumb(vScrollSize);
			vScroll.setIncrement((rowHeight + pad * 2));
			vScroll.setPageIncrement(vScrollSize);
//			vScroll.setValues(0, 0, h, 64, 1, 20 + pad * 2);
		}
		int hScrollSize = hScroll.getSize().x;
		if (w < hScrollSize) {
			hScroll.setEnabled(false);
		} else {
			hScroll.setEnabled(true);
			hScroll.setSelection(0);
			hScroll.setMinimum(0);
			hScroll.setMaximum(w);
			hScroll.setThumb(hScrollSize);
			hScroll.setIncrement(w / dataProvider.columnCount());
			vScroll.setPageIncrement(hScrollSize);
//			vScroll.setValues(0, 0, h, 64, 1, 20 + pad * 2);
		}
	}
	
	public void packColumns(GC g) {
		String[] tableHeaders = dataProvider.getHeaders();
		columnWidths = new int[dataProvider.columnCount()];
		for (int i = 0; i < columnWidths.length; i++) {
			columnWidths[i] += g.stringExtent(String.valueOf(tableHeaders[i])).x + pad;
		}
		for (int i = 0; i < dataProvider.size(); i++) {
			for (int col = 0; col < dataProvider.columnCount(); col++) {
				int w = g.stringExtent(dataProvider.get(i, col)).x + pad;
				if (columnWidths[col] < w) {
					columnWidths[col] = w;
				}
			}
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public static Color changeColorBrightness(Display display, Color color, int increment) {
		return new Color(
			display,
			incrementValue(color.getRed(), increment), 
			incrementValue(color.getGreen(), increment), 
			incrementValue(color.getBlue(), increment)
		);
	}
	
	private static int incrementValue(int value, int increment) {
		return Math.max(Math.min((value + increment), 255), 0);
	}

	public FlexTableDataProvider<T> getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(FlexTableDataProvider<T> dataProvider) {
		this.dataProvider = dataProvider;
		positions = new Rectangle[dataProvider.size() * dataProvider.columnCount()];
		this.dirty = true;
	}
}
