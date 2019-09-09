package net.sf.librefundraiser.gui.flextable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
	private static final int pad = 20, rowHeight = 12, summaryRowHeight = 48, summaryPad = 5;
//	private final boolean border;
	private boolean dirty = true, headerVisible = true, multiple, shiftPressed, summaryMode;
	private int scrollX = 0, scrollY = 0, firstSelectedRow = -1, lastSelectedRow = -1; 
	private final Queue<FlexTableSelectionListener<T>> selectionListeners = new ConcurrentLinkedDeque<>();

	public FlexTable(Composite parent, int style) {
		super(parent, style | SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		this.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				calibrateScrollBars();
			}
		});
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.SHIFT) {
					FlexTable.this.shiftPressed = false;
				}
				//TODO add real keyboard interaction (arrow keys, enter key, etc)
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.SHIFT) {
					FlexTable.this.shiftPressed = true;
				}
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
				if (event.row < 0) return;
				event.target = dataProvider.get(event.row);
				for (FlexTableSelectionListener<T> l : selectionListeners) {
					l.widgetDefaultSelected(event);
				}
			}
			@Override
			public void mouseUp(MouseEvent e) {
				if (e.y < rowHeight + pad && !summaryMode) {
					//header has been clicked, sort by column
					for (int i = 0, x = 0; i < columnWidths.length; i++) {
						x += columnWidths[i];
						if (x > e.x + scrollX) {
							if (FlexTable.this.dataProvider.sort(i)) {
								redraw();
							}
							break;
						}
					}
				} else {
					//row has been clicked, alert selection listeners
					FlexTableSelectionEvent<T> event = new FlexTableSelectionEvent<>();
					for (int i = 0; i < positions.length; i++) {
						if (positions[i] != null && positions[i].contains(e.x + scrollX, e.y + scrollY)) {
							if (!summaryMode) {
								event.row = i / dataProvider.columnCount();
								event.column = i % dataProvider.columnCount();
							} else {
								event.row = i;
								event.column = 0;
							}
							break;
						}
					}
					event.target = event.row >= 0 && event.row < dataProvider.size() ? dataProvider.get(event.row) : null;
					for (FlexTableSelectionListener<T> l : selectionListeners) {
						l.widgetSelected(event);
					}
					if (event.doit) {
						//TODO implement multiple selection with gaps, i.e. ctrl key
						if (e.button != 3 || event.row > lastSelectedRow || event.row < firstSelectedRow) {
							if (!multiple || !shiftPressed) {
								firstSelectedRow = lastSelectedRow = event.row;
							} else {
								if (event.row < firstSelectedRow) {
									firstSelectedRow = event.row;
								} else {
									lastSelectedRow = event.row;
								}
							}
							redraw();
						}
					}
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
				if (summaryMode) {
					int x = pad, y = scrollY - scrollY % summaryRowHeight;
					for (int i = scrollY / summaryRowHeight; i < dataProvider.size(); i++) {
						if (i >= firstSelectedRow && i <= lastSelectedRow) {
							g.setForeground(colorSelectedText);
							g.setBackground(colorSelectedRow);
						} else {
							g.setBackground(i % 2 == 0 ? colorOddRows : colorEvenRows);
							g.setForeground(colorText);
						}
						g.fillRectangle(0, y - scrollY, caW, summaryRowHeight);
						g.setBackground(colorLines);
						g.fillRectangle(0, y + summaryRowHeight - 1 - scrollY, caW, 1);
						String[] fieldData = {dataProvider.get(i, 0), dataProvider.get(i, 1), dataProvider.get(i, 2), dataProvider.get(i, 3)};
						g.setFont(getHighlightField() == 0 ? arialBold : arial);
						g.drawString(fieldData[0], x, y - scrollY + summaryPad, true);
						g.setFont(getHighlightField() == 1 ? arialBold : arial);
						int sw = g.stringExtent(fieldData[1]).x;
						g.drawString(fieldData[1], x + caW - sw - pad * 2, y - scrollY + summaryPad, true);
						g.setFont(getHighlightField() == 2 ? arialBold : arial);
						g.drawString(fieldData[2], x, y + summaryRowHeight - 20 - scrollY - summaryPad, true);
						sw = g.stringExtent(fieldData[3]).x;
						g.setFont(getHighlightField() == 3 ? arialBold : arial);
						g.drawString(fieldData[3], x + caW - sw - pad * 2, y + summaryRowHeight - 20 - scrollY - summaryPad, true);
						if (positions.length != dataProvider.size()) {
							positions = new Rectangle[dataProvider.size()];
						}
						positions[i] = new Rectangle(x, y, caW, summaryRowHeight);
						if (y - scrollY > caH) break;
						y += summaryRowHeight;
					}
				} else {
					int x = pad, y = scrollY - scrollY % (rowHeight + pad);
					for (int i = scrollY / (rowHeight + pad); i < dataProvider.size(); i++) {
						for (int col = 0; col < dataProvider.columnCount(); col++) {
							if (col == 0) {
								x = pad;
								y += rowHeight + pad;
								if (i >= firstSelectedRow && i <= lastSelectedRow) {
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
							if (col == getHighlightField()) {
								g.setFont(arialBold);
								g.setBackground(colorSelectedRow);
								g.setAlpha(60);
								g.fillRectangle(x - scrollX - pad / 2, y - scrollY, columnWidths[col], rowHeight + pad);
								g.setAlpha(255);
							} else {
								g.setFont(arial);
							}
							if (positions.length != dataProvider.size() * dataProvider.columnCount()) {
								positions = new Rectangle[dataProvider.size() * dataProvider.columnCount()];
							}
							positions[i * dataProvider.columnCount() + col] = new Rectangle(x, y, columnWidths[col], rowHeight + pad);
							if (y - scrollY > caH) break;
							if (x - scrollX > caW) continue;
							g.drawString(String.valueOf(dataProvider.get(i, col)), x - scrollX, y + rowHeight / 2 + 1 - scrollY, true);
							x += columnWidths[col];
						}
					}
					if (headerVisible) {
						x = pad;
						g.setBackground(colorEvenRows);
						g.fillRectangle(0, 0, getClientArea().width, rowHeight + pad);
						g.setBackground(colorHeaderText);
						g.fillRectangle(0, rowHeight + pad - 2, getClientArea().width, 2);
						g.setFont(arialBold);
						g.setForeground(colorHeaderText);
						String[] tableHeaders = dataProvider.getHeaders();
						for (int i = 0; i < tableHeaders.length; i++) {
							if (i == getHighlightField()) {
								g.setBackground(colorSelectedRow);
								g.setAlpha(60);
								g.fillRectangle(x - scrollX - pad / 2, 0, columnWidths[i], rowHeight + pad);
								g.setAlpha(255);
								g.setBackground(colorHeaderText);
								int b = rowHeight + pad - 2, t = b - 6, center = columnWidths[i] / 2 - pad / 2 - scrollX;
								if (dataProvider.getSortAsc()) {
									g.fillPolygon(new int[] {center + x, t, center + x + 6, b, center + x - 6, b});
								} else {
									g.fillPolygon(new int[] {center + x - 6, t + 8, center + x + 6, t + 8, center + x, b + 8});
								}
							}
							g.drawString(String.valueOf(tableHeaders[i]), x - scrollX, rowHeight / 2 + 1, true);
							x += columnWidths[i];
						}
					}
				}
			}
		});
	}
	
	private void calibrateScrollBars() {
		int w, h;
		if (!summaryMode) {
			w = 0;
			//leave room for header
			h = (dataProvider.size() + 1) * (rowHeight + pad);
			for (int cw : columnWidths) {
				w += cw;
			}
		} else {
			w = this.getClientArea().width;
			h = dataProvider.size() * summaryRowHeight;
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
		if (this.dataProvider != null) {
			this.dataProvider.setSummaryMode(summaryMode);
		}
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
	
	public T getFirstSelection() {
		if (firstSelectedRow == -1) return null;
		return dataProvider.get(firstSelectedRow);
	}
	
	public List<T> getSelection() {
		List<T> selection = new ArrayList<>();
		for (int i = firstSelectedRow; i <= lastSelectedRow; i++) {
			selection.add(dataProvider.get(i));
		}
		return selection;
	}
	
	public void refresh() {
		new Thread("FlexTable Refresh"){
			public void run() {
				dataProvider.refresh();
				getDisplay().asyncExec(() -> {
					if (FlexTable.this.isDisposed()) return;
					FlexTable.this.dirty = true;
					FlexTable.this.redraw();
				});
			}
		}.start();
	}

	public void setHeaderVisible(boolean headerVisible) {
		this.headerVisible = headerVisible;
	}
	
	public boolean isHeaderVisible() {
		return this.headerVisible;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public int getSelectionCount() {
		if (firstSelectedRow < 0 || lastSelectedRow < 0) return 0;
		return lastSelectedRow - firstSelectedRow + 1;
	}

	public boolean isSummaryMode() {
		return summaryMode;
	}

	public void setSummaryMode(boolean summaryMode) {
		if (summaryMode != this.summaryMode) {
			this.getHorizontalBar().setVisible(!summaryMode);
			if (this.dataProvider != null) {
				this.dataProvider.setSummaryMode(summaryMode);
			}
			//make sure that table stays centered on same entry
			int oldScroll = this.getVerticalBar().getSelection();
			this.summaryMode = summaryMode;
			calibrateScrollBars();
			if (summaryMode) {
				int i = oldScroll / (rowHeight + pad);
				this.getVerticalBar().setSelection(i * summaryRowHeight);
			} else {
				int i = oldScroll / summaryRowHeight;
				this.getVerticalBar().setSelection(i * (rowHeight + pad));
			}
			this.scrollY = this.getVerticalBar().getSelection();
		}
	}

	private int getHighlightField() {
		return this.dataProvider.getSortField();
	}
	
	public void setFilter(String filter) {
		this.dataProvider.setFilter(filter);
	}
	
	public String getFilter() {
		return this.dataProvider.getFilter();
	}
}
