package net.sf.librefundraiser.gui;

import java.util.ArrayDeque;
import java.util.StringTokenizer;

import net.sf.librefundraiser.Donor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class LinkEditForm extends Group {
	private ArrayDeque<String> links;
	private Composite compositeLinks;
	private Text txtNewAddress;
	private ToolBar btnNewLink;
	private Composite compositeLinkForm;
	private final Donor donor;
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
		tltmAddALink.setImage(SWTResourceManager.getImage(DonorEditForm.class, "/net/sf/librefundraiser/icons/add-link.png"));
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
				((DonorEditForm)getParent().getParent().getParent()).setEdited(true);
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
		for (String l : links) {
			i++;
			Link link = new Link(compositeLinks, SWT.NONE);
			link.setText(i + ".  <a href=\""+l+"\">"+l+"</a>");
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
