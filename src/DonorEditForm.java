import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;


public class DonorEditForm extends Composite {
	private Text txtFirstName;
	private Text txtLastName;
	private Text txtHomePhone;
	private Text txtWorkPhone;
	private Text txtFax;
	private Text txtOptional;
	private Text txtCity;
	private Text txtAddress1;
	private Text txtZip;
	private Text txtAddress2;
	private Text txtCountry;
	private Text txtNotes;
	private Text txtTotalGiven;
	private Text txtMain;
	private Text txtOther;
	private Text txtYearToDate;
	private Text txtLargestGift;
	private Text txtFirstGiftDate;
	private Text txtLastGiftAmt;
	private Text txtLastGiftDate;
	private Text txtLastEdited;
	public final Donor donor;
	private boolean business = false;
	private Combo comboSalutation;
	private Combo comboCategory;
	private Combo comboDonorSource;
	private Combo comboMailingName;
	private Combo comboState;
	private Text txtSpouseLast;
	private Text txtSpouseFirst;
	private Button btnBusinessother;
	private Button btnIndividual;
	private Text txtContactLast;
	private Text txtContactFirst;
	private Text txtBusinessName;
	private Composite compositeIndvNames;
	private Composite compositeBusiNames;
	private Composite compositeNames;
	private Label lblOptional;
	private Label lblContact;
	private Composite compositeOptional;
	private DonorTab donorTab;
	private boolean edited = false;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public DonorEditForm(Composite parent, int style, DonorTab donorTab) {
		super(parent, style);
		this.donorTab = donorTab;
		this.donor = donorTab.getDonor();
		setLayout(new GridLayout(1, false));
		
		Composite compositeBasic = new Composite(this, SWT.NONE);
		compositeBasic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeBasic.setLayout(new GridLayout(2, false));
		
		Group grpType = new Group(compositeBasic, SWT.NONE);
		grpType.setText("Type");
		grpType.setBounds(0, 0, 70, 82);
		grpType.setLayout(new GridLayout(1, false));
		
		SelectionAdapter typeSelect = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean business = e.getSource().equals(btnBusinessother);
				setBusiness(business);
			}
		};
		
		btnIndividual = new Button(grpType, SWT.RADIO);
		btnIndividual.addSelectionListener(typeSelect);
		btnIndividual.setSelection(true);
		btnIndividual.setText("Individual");
		
		btnBusinessother = new Button(grpType, SWT.RADIO);
		btnBusinessother.setText("Business/Other");
		btnBusinessother.addSelectionListener(typeSelect);
		
		compositeNames = new Composite(compositeBasic, SWT.NONE);
		compositeNames.setLayout(new StackLayout());
		compositeNames.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		compositeIndvNames = new Composite(compositeNames, SWT.NONE);
		GridLayout gl_compositeIndvNames = new GridLayout(4, false);
		gl_compositeIndvNames.marginBottom = -5;
		gl_compositeIndvNames.marginTop = -5;
		gl_compositeIndvNames.marginRight = -5;
		gl_compositeIndvNames.marginLeft = -5;
		compositeIndvNames.setLayout(gl_compositeIndvNames);
		compositeIndvNames.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblFirstName = new Label(compositeIndvNames, SWT.NONE);
		lblFirstName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFirstName.setText("First Name");
		
		txtFirstName = new Text(compositeIndvNames, SWT.BORDER);
		txtFirstName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblLastName = new Label(compositeIndvNames, SWT.NONE);
		lblLastName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLastName.setText("Last Name");
		
		txtLastName = new Text(compositeIndvNames, SWT.BORDER);
		txtLastName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblSpouseFirst = new Label(compositeIndvNames, SWT.NONE);
		lblSpouseFirst.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSpouseFirst.setText("Spouse First");
		
		txtSpouseFirst = new Text(compositeIndvNames, SWT.BORDER);
		txtSpouseFirst.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblSpouseLast = new Label(compositeIndvNames, SWT.NONE);
		lblSpouseLast.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSpouseLast.setText("Spouse Last");
		
		txtSpouseLast = new Text(compositeIndvNames, SWT.BORDER);
		txtSpouseLast.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		compositeBusiNames = new Composite(compositeNames, SWT.NONE);
		GridLayout gl_compositeBusiNames = new GridLayout(4, false);
		gl_compositeBusiNames.marginTop = -5;
		gl_compositeBusiNames.marginRight = -5;
		gl_compositeBusiNames.marginLeft = -5;
		gl_compositeBusiNames.marginBottom = -5;
		compositeBusiNames.setLayout(gl_compositeBusiNames);
		
		Label lblBusinessName = new Label(compositeBusiNames, SWT.NONE);
		lblBusinessName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBusinessName.setText("Business Name");
		
		txtBusinessName = new Text(compositeBusiNames, SWT.BORDER);
		txtBusinessName.setText("Business Name");
		txtBusinessName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label lblContactFirst = new Label(compositeBusiNames, SWT.NONE);
		lblContactFirst.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblContactFirst.setText("Contact First");
		
		txtContactFirst = new Text(compositeBusiNames, SWT.BORDER);
		txtContactFirst.setText("Contact First");
		txtContactFirst.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblContactLast = new Label(compositeBusiNames, SWT.NONE);
		lblContactLast.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblContactLast.setText("Contact Last");
		
		txtContactLast = new Text(compositeBusiNames, SWT.BORDER);
		txtContactLast.setText("Contact Last");
		txtContactLast.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label sep = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite compositeMisc = new Composite(this, SWT.NONE);
		compositeMisc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeMisc.setLayout(new GridLayout(4, false));
		
		Label lblSalutation = new Label(compositeMisc, SWT.NONE);
		lblSalutation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSalutation.setBounds(0, 0, 55, 15);
		lblSalutation.setText("Salutation");
		
		comboSalutation = new Combo(compositeMisc, SWT.NONE);
		comboSalutation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboSalutation.setBounds(0, 0, 91, 23);
		
		Label lblHomePhone = new Label(compositeMisc, SWT.NONE);
		lblHomePhone.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblHomePhone.setBounds(0, 0, 55, 15);
		lblHomePhone.setText("Home Phone");
		
		txtHomePhone = new Text(compositeMisc, SWT.BORDER);
		txtHomePhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtHomePhone.setBounds(0, 0, 76, 21);
		
		Label lblCategory = new Label(compositeMisc, SWT.NONE);
		lblCategory.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCategory.setBounds(0, 0, 55, 15);
		lblCategory.setText("Category");
		
		comboCategory = new Combo(compositeMisc, SWT.NONE);
		comboCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboCategory.setBounds(0, 0, 91, 23);
		
		Label lblWorkPhone = new Label(compositeMisc, SWT.NONE);
		lblWorkPhone.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblWorkPhone.setBounds(0, 0, 55, 15);
		lblWorkPhone.setText("Work Phone");
		
		txtWorkPhone = new Text(compositeMisc, SWT.BORDER);
		txtWorkPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtWorkPhone.setBounds(0, 0, 76, 21);
		
		Label lblDonorSource = new Label(compositeMisc, SWT.NONE);
		lblDonorSource.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDonorSource.setBounds(0, 0, 55, 15);
		lblDonorSource.setText("Donor Source");
		
		comboDonorSource = new Combo(compositeMisc, SWT.NONE);
		comboDonorSource.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboDonorSource.setBounds(0, 0, 91, 23);
		
		
		Label lblFax = new Label(compositeMisc, SWT.NONE);
		lblFax.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFax.setBounds(0, 0, 55, 15);
		lblFax.setText("Fax");
		
		txtFax = new Text(compositeMisc, SWT.BORDER);
		txtFax.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtFax.setBounds(0, 0, 76, 21);
		
		TabFolder tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setSelection(0);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TabItem tbtmAddress = new TabItem(tabFolder, SWT.NONE);
		tbtmAddress.setText("Postal Address");
		
		Composite compositeAddress = new Composite(tabFolder, SWT.NONE);
		tbtmAddress.setControl(compositeAddress);
		compositeAddress.setLayout(new GridLayout(4, false));
		
		compositeOptional = new Composite(compositeAddress, SWT.NONE);
		compositeOptional.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		compositeOptional.setLayout(new StackLayout());
		
		lblOptional = new Label(compositeOptional, SWT.NONE);
		lblOptional.setAlignment(SWT.RIGHT);
		lblOptional.setText("(optional)");
		
		lblContact = new Label(compositeOptional, SWT.NONE);
		lblContact.setAlignment(SWT.RIGHT);
		lblContact.setText("Contact");
		
		txtOptional = new Text(compositeAddress, SWT.BORDER);
		txtOptional.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtOptional.setBounds(0, 0, 76, 21);
		
		Label lblCity = new Label(compositeAddress, SWT.NONE);
		lblCity.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCity.setBounds(0, 0, 55, 15);
		lblCity.setText("City");
		
		txtCity = new Text(compositeAddress, SWT.BORDER);
		txtCity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtCity.setBounds(0, 0, 76, 21);
		
		Label lblMailingName = new Label(compositeAddress, SWT.NONE);
		lblMailingName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMailingName.setBounds(0, 0, 55, 15);
		lblMailingName.setText("Mailing Name");
		
		comboMailingName = new Combo(compositeAddress, SWT.NONE);
		comboMailingName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboMailingName.setBounds(0, 0, 91, 23);
		
		Label lblState = new Label(compositeAddress, SWT.NONE);
		lblState.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblState.setBounds(0, 0, 55, 15);
		lblState.setText("State/Province");
		
		comboState = new Combo(compositeAddress, SWT.NONE);
		comboState.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboState.setBounds(0, 0, 91, 23);
		
		Label lblAddress1 = new Label(compositeAddress, SWT.NONE);
		lblAddress1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAddress1.setBounds(0, 0, 55, 15);
		lblAddress1.setText("Address 1");
		
		txtAddress1 = new Text(compositeAddress, SWT.BORDER);
		txtAddress1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtAddress1.setBounds(0, 0, 76, 21);
		
		Label lblZip = new Label(compositeAddress, SWT.NONE);
		lblZip.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblZip.setBounds(0, 0, 55, 15);
		lblZip.setText("Zip");
		
		txtZip = new Text(compositeAddress, SWT.BORDER);
		txtZip.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtZip.setBounds(0, 0, 76, 21);
		
		Label lblAddress2 = new Label(compositeAddress, SWT.NONE);
		lblAddress2.setToolTipText("This is the primary address the post office will deliver to. ");
		lblAddress2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAddress2.setBounds(0, 0, 55, 15);
		lblAddress2.setText("Address 2");
		
		txtAddress2 = new Text(compositeAddress, SWT.BORDER);
		txtAddress2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtAddress2.setBounds(0, 0, 76, 21);
		
		Label lblCountry = new Label(compositeAddress, SWT.NONE);
		lblCountry.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCountry.setBounds(0, 0, 55, 15);
		lblCountry.setText("Country");
		
		txtCountry = new Text(compositeAddress, SWT.BORDER);
		txtCountry.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtCountry.setBounds(0, 0, 76, 21);
		
		TabItem tbtmOther = new TabItem(tabFolder, SWT.NONE);
		tbtmOther.setText("Email/Web");
		
		Composite compositeOther = new Composite(tabFolder, SWT.NONE);
		tbtmOther.setControl(compositeOther);
		compositeOther.setLayout(new GridLayout(1, false));
		

		
		Group grpEmail = new Group(compositeOther, SWT.NONE);
		grpEmail.setLayout(new GridLayout(4, false));
		grpEmail.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpEmail.setText("Email");
		grpEmail.setBounds(0, 0, 70, 82);
		
		Label lblMain = new Label(grpEmail, SWT.NONE);
		lblMain.setBounds(0, 0, 55, 15);
		lblMain.setText("Main");
		
		txtMain = new Text(grpEmail, SWT.BORDER);
		txtMain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtMain.setBounds(0, 0, 76, 21);
		
		Label lblOther = new Label(grpEmail, SWT.NONE);
		lblOther.setBounds(0, 0, 55, 15);
		lblOther.setText("Other");
		
		txtOther = new Text(grpEmail, SWT.BORDER);
		txtOther.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtOther.setBounds(0, 0, 76, 21);
		
		Group grpWeb = new Group(compositeOther, SWT.NONE);
		grpWeb.setLayout(new GridLayout(1, false));
		grpWeb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpWeb.setText("Web");
		grpWeb.setBounds(0, 0, 70, 82);
		grpWeb.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		ToolBar toolBar = new ToolBar(grpWeb, SWT.FLAT | SWT.RIGHT);
		
		ToolItem tltmAddALink = new ToolItem(toolBar, SWT.NONE);
		tltmAddALink.setImage(SWTResourceManager.getImage(DonorEditForm.class, "/icons/add-link.png"));
		tltmAddALink.setText("Add a link...");
		
		TabItem tbtmNotes = new TabItem(tabFolder, SWT.NONE);
		tbtmNotes.setText("Notes");
		
		txtNotes = new Text(tabFolder, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		tbtmNotes.setControl(txtNotes);
		
		TabItem tbtmGifts = new TabItem(tabFolder, SWT.NONE);
		tbtmGifts.setText("Gifts");
		
		Composite compositeGifts = new Composite(tabFolder, SWT.NONE);
		tbtmGifts.setControl(compositeGifts);
		compositeGifts.setLayout(new GridLayout(1, false));
		compositeGifts.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		ToolBar tbrGifts = new ToolBar(compositeGifts, SWT.FLAT | SWT.RIGHT);
		tbrGifts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tbrGifts.setBounds(0, 0, 89, 23);
		
		ToolItem tltmAdd = new ToolItem(tbrGifts, SWT.NONE);
		tltmAdd.setImage(SWTResourceManager.getImage(DonorEditForm.class, "/icons/new-gift.png"));
		tltmAdd.setText("Add");
		
		ToolItem tltmSep = new ToolItem(tbrGifts, SWT.SEPARATOR);
		tltmSep.setText("sep");
		
		ToolItem tltmEdit = new ToolItem(tbrGifts, SWT.NONE);
		tltmEdit.setImage(SWTResourceManager.getImage(DonorEditForm.class, "/icons/edit-gift.png"));
		tltmEdit.setEnabled(false);
		tltmEdit.setText("Edit");
		
		ToolItem tltmDelete = new ToolItem(tbrGifts, SWT.NONE);
		tltmDelete.setImage(SWTResourceManager.getImage(DonorEditForm.class, "/icons/delete-gift.png"));
		tltmDelete.setEnabled(false);
		tltmDelete.setText("Delete");
		
		Composite compositeGiftTable = new GiftTable(compositeGifts, SWT.NONE, donor);
		compositeGiftTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TabItem tbtmDetails = new TabItem(tabFolder, SWT.NONE);
		tbtmDetails.setText("Details");
		
		Composite compositeDetails = new Composite(tabFolder, SWT.NONE);
		tbtmDetails.setControl(compositeDetails);
		compositeDetails.setLayout(new GridLayout(2, false));
		
		Group grpGifts = new Group(compositeDetails, SWT.NONE);
		grpGifts.setLayout(new GridLayout(4, false));
		grpGifts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpGifts.setText("Gifts");
		
		Label lblTotalGiven = new Label(grpGifts, SWT.NONE);
		lblTotalGiven.setBounds(0, 0, 55, 15);
		lblTotalGiven.setText("Total Given");
		
		txtTotalGiven = new Text(grpGifts, SWT.BORDER);
		txtTotalGiven.setEditable(false);
		txtTotalGiven.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtTotalGiven.setBounds(0, 0, 76, 21);
		
		Label lblLargestGift = new Label(grpGifts, SWT.NONE);
		lblLargestGift.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLargestGift.setText("Largest Gift");
		
		txtLargestGift = new Text(grpGifts, SWT.BORDER);
		txtLargestGift.setEditable(false);
		txtLargestGift.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblYearToDate = new Label(grpGifts, SWT.NONE);
		lblYearToDate.setText("Year to Date");
		
		txtYearToDate = new Text(grpGifts, SWT.BORDER);
		txtYearToDate.setEditable(false);
		txtYearToDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblFirstGiftDate = new Label(grpGifts, SWT.NONE);
		lblFirstGiftDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFirstGiftDate.setText("First Gift Date");
		
		txtFirstGiftDate = new Text(grpGifts, SWT.BORDER);
		txtFirstGiftDate.setEditable(false);
		txtFirstGiftDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblLastGiftAmt = new Label(grpGifts, SWT.NONE);
		lblLastGiftAmt.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLastGiftAmt.setText("Last Gift Amt");
		
		txtLastGiftAmt = new Text(grpGifts, SWT.BORDER);
		txtLastGiftAmt.setEditable(false);
		txtLastGiftAmt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblLastGiftDate = new Label(grpGifts, SWT.NONE);
		lblLastGiftDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLastGiftDate.setText("Last Gift Date");
		
		txtLastGiftDate = new Text(grpGifts, SWT.BORDER);
		txtLastGiftDate.setEditable(false);
		txtLastGiftDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group grpOther = new Group(compositeDetails, SWT.NONE);
		grpOther.setLayout(new GridLayout(2, false));
		grpOther.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpOther.setText("Other");
		
		Label lblLastEdited = new Label(grpOther, SWT.NONE);
		lblLastEdited.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLastEdited.setText("Last Edited");
		
		txtLastEdited = new Text(grpOther, SWT.BORDER);
		txtLastEdited.setEditable(false);
		txtLastEdited.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		this.fillOut();
		this.setBusiness(!donor.getData("type").equals("I"));
		this.setEdited(false);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	protected void fillOut() {
		txtFirstName.setText(donor.getData("firstname"));
		txtLastName.setText(donor.getData("lastname"));
		txtBusinessName.setText(donor.getData("lastname"));
		txtSpouseFirst.setText(donor.getData("spousefrst"));
		txtSpouseLast.setText(donor.getData("spouselast"));
		txtContactLast.setText(donor.getData("spouselast"));
		txtContactFirst.setText(donor.getData("spousefrst"));
		txtHomePhone.setText(donor.getData("homephone"));
		txtWorkPhone.setText(donor.getData("workphone"));
		txtFax.setText(donor.getData("fax"));
		txtOptional.setText(donor.getData("contact"));
		txtCity.setText(donor.getData("city"));
		txtAddress1.setText(donor.getData("address1"));
		txtZip.setText(donor.getData("zip"));
		txtAddress2.setText(donor.getData("address2"));
		txtCountry.setText(donor.getData("country"));
		txtNotes.setText(donor.getData("notes"));
		txtTotalGiven.setText(donor.getData("alltime"));
		txtMain.setText(donor.getData("email"));
		txtOther.setText("");
		txtYearToDate.setText(donor.getData("yeartodt"));
		txtLargestGift.setText(donor.getData("largest"));
		txtFirstGiftDate.setText(donor.getData("firstgift"));
		txtLastGiftAmt.setText(donor.getData("lastamt"));
		txtLastGiftDate.setText(donor.getData("lastgivedt"));
		txtLastEdited.setText(donor.getData("changedate"));
		comboSalutation.setText(donor.getData("salutation"));
		comboCategory.setText(donor.getData("category1"));
		comboDonorSource.setText(donor.getData("category2"));
		comboMailingName.setText(donor.getData("mailname"));
		comboState.setText(donor.getData("state"));
	}
	
	protected void setBusiness(boolean business) {
		if (this.business != business) {
			this.business = business;
			this.setEdited(true);
			if (business) {
				txtBusinessName.setText(txtLastName.getText());
				txtContactLast.setText(txtSpouseLast.getText());
				txtContactFirst.setText(txtSpouseFirst.getText());
			} else {
				txtLastName.setText(txtBusinessName.getText());
				txtSpouseFirst.setText(txtContactFirst.getText());
				txtSpouseLast.setText(txtContactLast.getText());
			}
		}
		btnIndividual.setSelection(!business);
		btnBusinessother.setSelection(business);
		compositeIndvNames.setVisible(!business);
		compositeBusiNames.setVisible(business);
		lblOptional.setVisible(!business);
		lblContact.setVisible(business);
		((StackLayout)compositeNames.getLayout()).topControl = business?compositeBusiNames:compositeIndvNames;
		((StackLayout)compositeOptional.getLayout()).topControl = business?lblContact:lblOptional;
		this.getShell().layout();
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
		String lastname = donor.getData("lastname");
		String firstname = donor.getData("firstname");
		String tabTitle = lastname+(!(lastname.equals("")||firstname.equals(""))?", ":"")+firstname;
		if (tabTitle.equals("")) tabTitle = donor.getData("account");
		donorTab.setText((edited?"*":"")+tabTitle);
		LibreFundraiser.getSaveButton().setEnabled(edited);
	}
}
