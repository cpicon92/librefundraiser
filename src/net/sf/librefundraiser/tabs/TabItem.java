package net.sf.librefundraiser.tabs;

import net.sf.librefundraiser.ResourceManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.wb.swt.SWTResourceManager;

public class TabItem extends Composite {

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	private Image image;
	private String text;
	private Label lblTabText;
	private Control control;
	private TabFolder parent;
	private final TabItem thisTabItem = this;
	private Label lblImage;
	
	public TabItem(final TabFolder parent, int style) {
		super(parent, SWT.BORDER);
		MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				parent.setSelection(thisTabItem);
			}
		};
		addMouseListener(mouseAdapter);
		this.setBackgroundMode(SWT.INHERIT_FORCE);
		this.parent = parent;
		parent.createItem(this);
		setLayout(new GridLayout(3, false));
		
		lblImage = new Label(this, SWT.NONE);
		lblImage.addMouseListener(mouseAdapter);
		
		lblTabText = new Label(this, SWT.NONE);
		lblTabText.addMouseListener(mouseAdapter);
		lblTabText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		
		final Label lblCloseButton = new Label(this, SWT.NONE);
		lblCloseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				close();
			}
		});
		lblCloseButton.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				lblCloseButton.setImage(ResourceManager.getIcon("tabclose_hover.png"));
			}
			@Override
			public void mouseExit(MouseEvent e) {
				lblCloseButton.setImage(ResourceManager.getIcon("tabclose.png"));
			}
		});
		lblCloseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		lblCloseButton.setImage(ResourceManager.getIcon("tabclose.png"));
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
		lblImage.setImage(image);
		
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		lblTabText.setText(text);
		this.text = text;
		this.changed(new Control[] {lblTabText});
		this.layout(true, true);
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
		if (selected) {
			this.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		} else {
			this.setBackground(null);
		}
	}
	
	public void close() {
		parent.closeTab(this); 
	}
}
