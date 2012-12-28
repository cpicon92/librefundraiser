package net.sf.librefundraiser.tabs;

import net.sf.librefundraiser.ResourceManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class TabItem extends Composite {

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	private Image image;
	private String text;
	private Control control;
	private TabFolder parent;
	private final TabItem thisTabItem = this;
	private boolean selected = false;
	private Rectangle closeButtonArea = null;
	private boolean closeHover = false;
	
	public TabItem(final TabFolder parent, int style) {
		super(parent, SWT.NONE);
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				boolean newCloseHover = closeButtonArea.contains(e.x, e.y);
				if (closeHover != newCloseHover) {
					closeHover = newCloseHover;
					redraw();
				}
			}
		});
		//TODO prevent overflows in colour mixing
		final Color gradBottom = thisTabItem.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		int b = 25;
		final Color gradTop = new Color(thisTabItem.getDisplay(), gradBottom.getRed() + b, gradBottom.getGreen() + b, gradBottom.getBlue() + b);
		final Color colorTabLines = new Color(thisTabItem.getDisplay(), 127, 127, 127);
		final Color colorBlack = new Color(thisTabItem.getDisplay(), 0, 0, 0);
		int c = 20;
		final Color colorShadow = new Color(thisTabItem.getDisplay(), gradBottom.getRed() - c, gradBottom.getGreen() - c, gradBottom.getBlue() - c);
		int d = 10;
		final Color grayTab = new Color(thisTabItem.getDisplay(), colorShadow.getRed() + d, colorShadow.getGreen() + d, colorShadow.getBlue() + d);
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle gcSize = thisTabItem.getClientArea();
				e.gc.setForeground(gradBottom);
				e.gc.setBackground(colorShadow);
				e.gc.fillGradientRectangle(0, 0, gcSize.width, gcSize.height-1, true);
				e.gc.setForeground(colorTabLines);
				e.gc.setAntialias(SWT.ON);
				if (selected) {
					e.gc.setBackgroundPattern(new Pattern(thisTabItem.getDisplay(), 0, 0, 0, gcSize.height, gradTop, gradBottom));
				} else {
					e.gc.setBackground(grayTab);
					e.gc.drawLine(0, gcSize.height-1, gcSize.width, gcSize.height-1);
				}
				e.gc.fillPolygon(generateTabShape(gcSize.width, gcSize.height));
				e.gc.drawPolyline(generateTabShape(gcSize.width, gcSize.height));
				e.gc.setForeground(colorBlack);
				int textPosition = 14;
				if (image != null) {
					e.gc.drawImage(image, textPosition, gcSize.height/2 - image.getBounds().height/2);
					textPosition += image.getBounds().width + 6;
				}
				e.gc.drawText(text, textPosition, 5, true);
				closeButtonArea = new Rectangle(gcSize.width - 22, gcSize.height/2 - 6, 12, 12);
				String icon = closeHover?"tabclose_hover.png":"tabclose.png";
				e.gc.drawImage(ResourceManager.getIcon(icon), closeButtonArea.x, closeButtonArea.y);
			}
		});
		MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (closeHover) {
					thisTabItem.parent.closeTab(thisTabItem);
				} else {
					parent.setSelection(thisTabItem);
				}
			}
		};
		addMouseListener(mouseAdapter);
		this.parent = parent;
		parent.createItem(this);
		setLayout(new GridLayout(1, false));
		new Label(this, SWT.NONE);
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
		parent.setSelection(thisTabItem);
	}
	
	protected void setSelected(boolean selected) {
		this.selected = selected;
		this.redraw();
	}
	
	private static int[] generateTabShape(int w, int h) {
		int[] tabShape = new int[] {0,h-1, 1,h-2, 2,h-3, 3,h-4, 4,h-8, 4,5, 5,3, 6,2, 7,1, 10,0,
				w-10,0, w-7,1, w-6,2, w-5,3, w-4,5, w-4,h-8, w-3,h-4, w-2,h-3, w-1,h-2, w,h-1};
		return tabShape;
	}
}
