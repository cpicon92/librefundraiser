package net.sf.librefundraiser.tabs;

import net.sf.librefundraiser.ResourceManager;
import net.sf.librefundraiser.Util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class TabItem extends Composite {

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	private Image image;
	private String text = "";
	private Control control;
	private TabFolder parent;
	private boolean selected = false;
	private Rectangle closeButtonArea = null;
	private boolean closeHover = false;
	private boolean closable = true;

	public TabItem(final TabFolder parent, int style) {
		super(parent, SWT.DOUBLE_BUFFERED);
		addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				checkCloseButton(e);
			}
		});
		final Color gradBottom = TabItem.this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		final Color gradTop = Util.changeColorBrightness(this.getDisplay(), gradBottom, 25);
		final Color colorSelectedHighlight = TabItem.this.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
		final Color colorTabLines = Util.changeColorBrightness(TabItem.this.getDisplay(), gradBottom, -50);		
//		final Color colorTabLinesInactive = TabItem.this.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
		final Color colorBlack = new Color(TabItem.this.getDisplay(), 0, 0, 0);
		final Color colorShadow = Util.changeColorBrightness(this.getDisplay(), gradBottom, -20);
		final Color grayTab = Util.changeColorBrightness(this.getDisplay(), colorShadow, 10);
		final boolean[] repainting = new boolean[] {false};
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				if (repainting[0]) return;
				repainting[0] = true;
				Rectangle gcSize = TabItem.this.getClientArea();
				e.gc.setForeground(gradBottom);
				e.gc.setBackground(colorShadow);
				e.gc.fillGradientRectangle(0, 0, gcSize.width, gcSize.height-1, true);
				e.gc.setForeground(colorTabLines);
				e.gc.setAntialias(SWT.ON);
				if (selected) {
					e.gc.setBackgroundPattern(new Pattern(TabItem.this.getDisplay(), 0, 0, 0, gcSize.height, gradTop, gradBottom));
				} else {
					e.gc.setBackground(grayTab);
					e.gc.drawLine(0, gcSize.height-1, gcSize.width, gcSize.height-1);
				}
				e.gc.setForeground(colorTabLines);
				e.gc.fillPolygon(generateTabShape(gcSize.width, gcSize.height));
				if (selected) {
					e.gc.setBackground(colorSelectedHighlight);
				} else {
					e.gc.setBackground(colorTabLines);
				}
				e.gc.setClipping(0, 0, gcSize.width, 3);
				e.gc.fillPolygon(generateTabShape(gcSize.width, gcSize.height));
				e.gc.setClipping(0, 0, gcSize.width, gcSize.height);
				e.gc.drawPolyline(generateTabShape(gcSize.width, gcSize.height));
				e.gc.setForeground(colorBlack);
				if (closable) {
					closeButtonArea = new Rectangle(gcSize.width - 22, gcSize.height/2 - 4, 12, 12);
				} else {
					closeButtonArea = new Rectangle(gcSize.width - 6, gcSize.height/2 - 6, 0, 0);
				}
				int textPosition = 14;
				if (image != null) {
					e.gc.drawImage(image, textPosition, gcSize.height/2 - image.getBounds().height/2 + 2);
					textPosition += image.getBounds().width + 6;
				}
				int maxTextWidth = gcSize.width - textPosition - (gcSize.width - closeButtonArea.x);

				e.gc.drawText(shortenText(e.gc, text, maxTextWidth), textPosition, 7, true);
				if (closable) {
					String icon = closeHover?"tabclose_hover.png":"tabclose.png";
					e.gc.drawImage(ResourceManager.getIcon(icon), closeButtonArea.x, closeButtonArea.y);
				}
				repainting[0] = false;
			}
		});
		MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				checkCloseButton(e);
				if (e.button == 3) return;
				if (e.button == 2) {
					TabItem.this.parent.closeTab(TabItem.this);
					return;
				}
				if (closeHover) {
					TabItem.this.parent.closeTab(TabItem.this);
				} else {
					parent.setSelection(TabItem.this);
				}
			}
		};
		addMouseListener(mouseAdapter);
		this.parent = parent;
		parent.addTab(this);
		setLayout(new GridLayout(1, false));
		new Label(this, SWT.NONE);

		Menu menu = new Menu(this);
		setMenu(menu);

		final MenuItem mntmCloseTab = new MenuItem(menu, SWT.NONE);
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				if (!TabItem.this.isClosable()) mntmCloseTab.setEnabled(false);
			}
		});
		mntmCloseTab.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TabItem.this.parent.closeTab(TabItem.this);
			}
		});
		mntmCloseTab.setText("Close tab");

		MenuItem mntmCloseOtherTabs = new MenuItem(menu, SWT.NONE);
		mntmCloseOtherTabs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TabItem.this.parent.closeAllTabs(new TabItem[] {TabItem.this});
			}
		});
		mntmCloseOtherTabs.setText("Close other tabs");

		MenuItem mntmCloseTabsTo = new MenuItem(menu, SWT.NONE);
		mntmCloseTabsTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TabItem.this.parent.closeToRight(TabItem.this);
			}
		});
		mntmCloseTabsTo.setText("Close tabs to the right");
		parent.onNewTab(this);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
		redraw();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		redraw();
	}

	public Control getControl() {
		return control;
	}

	public void setControl(Control control) {
		this.control = control;
		parent.suckUpChildren();
		//		parent.setSelection(TabItem.this);
	}

	protected void setSelected(boolean selected) {
		this.selected = selected;
		this.redraw();
	}

	private static int[] generateTabShape(int w, int h) {
		int[] tabShape = new int[] {0,h-1, 1,h-2, 2,h-3, 3,h-4, 4,h-8, 4,6, 5,3, 6,2, 7,1, 10,0,
				w-10,0, w-7,1, w-6,2, w-5,3, w-4,6, w-4,h-8, w-3,h-4, w-2,h-3, w-1,h-2, w,h-1};
		return tabShape;
	}
	private static String shortenText(GC gc, String text, int maxWidth) {
		try {
			int width = gc.stringExtent(text).x;
			while (width > maxWidth) {
				//				System.out.println(width + " > " + maxWidth);
				text = text.substring(0, text.length() - 4) + "...";
				if (text.equals("...")) text = "";
				width = gc.stringExtent(text).x;
			}
		} catch (Exception e){}
		return text;
	}

	public boolean isClosable() {
		return closable;
	}

	public void setClosable(boolean closable) {
		this.closable = closable;
	}

	private void checkCloseButton(MouseEvent e) {
		checkCloseButton(e.x, e.y);
	}

	/**
	 * Checks the close button every two seconds (recursive)
	 * @param x mouse position
	 * @param y mouse position
	 */
	private void checkCloseButton(int x, int y) {
		try { 
			boolean newCloseHover = closeButtonArea.contains(x, y);
			if (closeHover != newCloseHover) {
				closeHover = newCloseHover;
				redraw();
			}
			if (closeHover) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
						}
						try {
							//TODO fix bug that causes app to crash when tabs are closed (seems to be starting here)
							//2019/08/31 added try catch.. should fix this
							getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									try {
										Point mousePos = toControl(getDisplay().getCursorLocation());
										checkCloseButton(mousePos.x, mousePos.y);
									} catch (RuntimeException e) {}
								}
							});
						} catch (Exception e) {}
					}
				}).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
