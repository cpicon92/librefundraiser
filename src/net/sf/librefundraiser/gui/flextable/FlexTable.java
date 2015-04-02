package net.sf.librefundraiser.gui.flextable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

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
import org.eclipse.swt.widgets.ScrollBar;

public class FlexTable<T> extends Composite {
	
	private int[] columnWidths = new int[0];
	private Rectangle[] positions = new Rectangle[0];
	private FlexTableDataProvider<T> dataProvider;
	private static final int pad = 20, rowHeight = 12;
//	private final boolean border;
	private boolean dirty = true;
	private int scrollX = 0, scrollY = 0,
	selectedRow = -1, highlightColumn = 0; 
	private final Queue<FlexTableSelectionListener<T>> selectionListeners = new ConcurrentLinkedDeque<>();

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public FlexTable(Composite parent, int style) {
		super(parent, style /*& ~SWT.BORDER*/ | SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
//		border = (style & SWT.BORDER) != 0;
//		System.out.println((border ? "has" : "no") + " border");
		this.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				calibrateScrollBars();
			}
		});
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				FlexTableSelectionEvent<T> event = new FlexTableSelectionEvent<>();
				for (int i = 0; i < positions.length; i++) {
					if (positions[i] != null && positions[i].contains(e.x + scrollX, e.y + scrollY)) {
						event.row = i / dataProvider.columnCount();
						event.column = i % dataProvider.columnCount();
						break;
					}
				}
				event.target = dataProvider.get(event.row);
				for (FlexTableSelectionListener<T> l : selectionListeners) {
					l.widgetDefaultSelected(event);
				}
				
			}
			@Override
			public void mouseUp(MouseEvent e) {
				FlexTableSelectionEvent<T> event = new FlexTableSelectionEvent<>();
				for (int i = 0; i < positions.length; i++) {
					if (positions[i] != null && positions[i].contains(e.x + scrollX, e.y + scrollY)) {
						event.row = i / dataProvider.columnCount();
						event.column = i % dataProvider.columnCount();
						break;
					}
				}
				event.target = event.row >= 0 && event.row < dataProvider.size() ? dataProvider.get(event.row) : null;
				for (FlexTableSelectionListener<T> l : selectionListeners) {
					l.widgetSelected(event);
				}
				if (event.doit) {
					selectedRow = event.row;
					redraw();
				}
			}
		});
		final ScrollBar hScroll = this.getHorizontalBar(), 
		vScroll = this.getVerticalBar();
		vScroll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				scrollY = (int) ((vScroll.getSelection() / (vScroll.getMaximum() - (double) vScroll.getThumb())) * vScroll.getMaximum()); 
				scrollY = vScroll.getSelection(); 
				redraw();
			}
		});
		hScroll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				scrollX = (int) ((hScroll.getSelection() / (hScroll.getMaximum() - (double) hScroll.getThumb())) * hScroll.getMaximum()); 
				scrollX = hScroll.getSelection();
				redraw();
			}
		});
		this.addPaintListener(new PaintListener() {
			final Color colorEvenRows = getDisplay().getSystemColor(SWT.COLOR_WHITE),
			colorSelectedRow = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION),
			colorOddRows = changeSaturation(colorSelectedRow, 0.2f),
			colorBg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND),
			colorLines = changeBrightness(colorOddRows, -50),
			colorSelectedText = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT),
			colorText = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND),
			colorHeaderText = changeBrightness(colorSelectedRow, -160);
			final Font arial = new Font(getDisplay(), new FontData("Arial", 10, SWT.NORMAL)),
			arialBold = new Font(getDisplay(), new FontData("Arial", 10, SWT.BOLD));
			@Override
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = getClientArea();
				int caW = clientArea.width, caH = clientArea.height;
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
						g.setBackground(colorLines);
						g.fillRectangle(0, y + rowHeight + pad - 1 - scrollY, caW, 1);
					}
					if (col == highlightColumn) {
						g.setFont(arialBold);
					} else {
						g.setFont(arial);
					}
					positions[i * dataProvider.columnCount() + col] = new Rectangle(x, y, columnWidths[col], rowHeight + pad);
					if (y - scrollY > caH) break;
					if (x - scrollX > caW) continue;
					g.drawString(String.valueOf(dataProvider.get(i, col)), x - scrollX, y + rowHeight / 2 + 1 - scrollY, true);
					x += columnWidths[col];
				}
				}
				x = pad;
				y = 0;
				g.setBackground(colorEvenRows);
				g.fillRectangle(0, y, getClientArea().width, rowHeight + pad);
				g.setBackground(colorHeaderText);
				g.fillRectangle(0, y + rowHeight + pad - 2, getClientArea().width, 2);
				g.setFont(arialBold);
				g.setForeground(colorHeaderText);
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
			vScroll.setSelection(0);
			scrollY = 0;
		} else {
			vScroll.setEnabled(true);
			vScroll.setValues(vScroll.getSelection(), 0, h, vScrollSize, rowHeight + pad * 2, vScrollSize);
		}
		int hScrollSize = hScroll.getSize().x;
		if (w < hScrollSize) {
			hScroll.setEnabled(false);
			hScroll.setSelection(0);
			scrollX = 0;
		} else {
			hScroll.setEnabled(true);
			hScroll.setValues(hScroll.getSelection(), 0, w + pad, hScrollSize, w / dataProvider.columnCount(), hScrollSize);
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

	public static Color changeBrightness(Color color, int increment) {
		return new Color(
			color.getDevice(),
			incrementValue(color.getRed(), increment), 
			incrementValue(color.getGreen(), increment), 
			incrementValue(color.getBlue(), increment)
		);
	}
	
	public static Color changeSaturation(Color color, float saturation) {
		float[] hsb = java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		int argb = java.awt.Color.HSBtoRGB(hsb[0], saturation, hsb[2]);
		return new Color(color.getDevice(), (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, (argb) & 0xFF);
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
	
	public void addSelectionListener(FlexTableSelectionListener<T> listener) {
		this.selectionListeners.add(listener);
	}
	
	public void removeSelectionListener(FlexTableSelectionListener<T> listener) {
		this.selectionListeners.remove(listener);
	}
	
	public void removeAllSelectionListeners() {
		this.selectionListeners.clear();
	}
	
	public T getSelection() {
		return dataProvider.get(selectedRow);
	}
	
	public void refresh() {
		dataProvider.refresh();
		this.dirty = true;
		this.redraw();
	}

	public int getHighlightColumn() {
		return highlightColumn;
	}

	public void setHighlightColumn(int highlightColumn) {
		this.highlightColumn = highlightColumn;
	}
}
