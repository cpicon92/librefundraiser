package net.sf.librefundraiser.gui;

import net.sf.librefundraiser.Donor;
import net.sf.librefundraiser.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.StringTokenizer;

public class LinkEditForm extends Group {
	private ArrayDeque<String> links;
	private Composite compositeLinks;
	private Text txtNewAddress;
	private ToolBar btnNewLink;
	private Composite compositeLinkForm;
	private final Donor donor;
	private static SelectionAdapter linkAdapter = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			try {
				Program.launch(new URL(e.text).toString());
			} catch (MalformedURLException e1) {
			}
		}
	};
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public LinkEditForm(Composite parent, int style, Donor donor) {
		super(parent, style);
		this.donor = donor;
		links = new ArrayDeque<String>();
		StringTokenizer token = new StringTokenizer(donor.getData("web"),"\n");
		while (token.hasMoreElements()) {
			links.add((String)token.nextElement());
		}
		
		this.setLayout(new GridLayout(1, false));
		this.setText("Web");
		this.setBounds(0, 0, 70, 82);
		this.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		compositeLinks = new Composite(this, SWT.NONE);
		compositeLinks.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeLinks.setLayout(new GridLayout(1, false));
		
		displayLinks();
		
		final Composite compositeNewLink = new Composite(this, SWT.NONE);
		compositeNewLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeNewLink.setLayout(new StackLayout());
		
		btnNewLink = new ToolBar(compositeNewLink, SWT.FLAT | SWT.RIGHT);
		
		ToolItem tltmAddALink = new ToolItem(btnNewLink, SWT.NONE);
		tltmAddALink.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				txtNewAddress.setText("http://");
				StackLayout sl = (StackLayout)compositeNewLink.getLayout();
				btnNewLink.setVisible(false);
				compositeLinkForm.setVisible(true);
				sl.topControl = compositeLinkForm;
				txtNewAddress.setFocus();
				txtNewAddress.setSelection(txtNewAddress.getCharCount());
			}
		});
		tltmAddALink.setImage(ResourceManager.getIcon("add-link.png"));
		tltmAddALink.setText("Add a link...");
		
		compositeLinkForm = new Composite(compositeNewLink, SWT.NONE);
		compositeLinkForm.setLayout(new GridLayout(2, false));
		
		txtNewAddress = new Text(compositeLinkForm, SWT.BORDER);
		txtNewAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtNewAddress.setBounds(0, 0, 75, 27);
		
		Button btnAdd = new Button(compositeLinkForm, SWT.NONE);
		btnAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				links.add(txtNewAddress.getText());
				((DonorEditForm)getParent().getParent().getParent().getParent()).setEdited(true);
				StackLayout sl = (StackLayout)compositeNewLink.getLayout();
				btnNewLink.setVisible(true);
				compositeLinkForm.setVisible(false);
				sl.topControl = btnNewLink;
				displayLinks();
				layout();
			}
		});
		btnAdd.setText("Add");
		
		StackLayout sl = (StackLayout)compositeNewLink.getLayout();
		btnNewLink.setVisible(true);
		compositeLinkForm.setVisible(false);
		sl.topControl = btnNewLink;

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	private void displayLinks() {
		for(Control c : compositeLinks.getChildren()) {
			c.dispose();
		}
		int i = 0;
		for (final String l : links) {
			i++;
			Composite cLink = new Composite(compositeLinks, SWT.NONE);
			cLink.setLayout(new RowLayout());
			Link link = new Link(cLink, SWT.NONE);
			link.setText(i + ".  <a href=\""+l+"\">"+l+"</a>");
			link.addSelectionListener(linkAdapter);
			final Label deleteButton = new Label(cLink, SWT.NONE);
			deleteButton.setVisible(false);
			deleteButton.setToolTipText("Delete this link");
			deleteButton.setImage(ResourceManager.getIcon("delete-link.png"));
			deleteButton.addMouseListener(new MouseListener() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
				}
				@Override
				public void mouseDown(MouseEvent e) {
				}
				@Override
				public void mouseUp(MouseEvent e) {
					links.remove(l);
					((DonorEditForm)getParent().getParent().getParent().getParent()).setEdited(true);
					displayLinks();
					layout();
				}
			});
			deleteButton.addMouseTrackListener(new MouseTrackListener() {
				@Override
				public void mouseEnter(MouseEvent e) {
					deleteButton.setImage(ResourceManager.getIcon("delete-link_hover.png"));		
				}

				@Override
				public void mouseExit(MouseEvent e) {
					deleteButton.setImage(ResourceManager.getIcon("delete-link.png"));
				}

				@Override
				public void mouseHover(MouseEvent e) {
				}
			});
			MouseTrackListener displayDelete = new MouseTrackListener() {
				@Override
				public void mouseEnter(MouseEvent e) {
					deleteButton.setVisible(true);
				}

				@Override
				public void mouseExit(MouseEvent e) {
					deleteButton.setVisible(false);
				}

				@Override
				public void mouseHover(MouseEvent e) {
				}
			};
			cLink.addMouseTrackListener(displayDelete);
			deleteButton.addMouseTrackListener(displayDelete);
			link.addMouseTrackListener(displayDelete);
		}
	}
	
	public void saveLinks() {
		String output = "";
		for (String l : links) {
			output += l + "\n";
		}
		donor.putData("web", output);
	}
	
}
