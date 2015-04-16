package net.sf.librefundraiser.gui;

import java.text.Format;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.ResourceManager;
import net.sf.librefundraiser.gui.flextable.FlexTable;
import net.sf.librefundraiser.gui.flextable.FlexTableDataProvider;
import net.sf.librefundraiser.gui.flextable.FlexTableSelectionAdapter;
import net.sf.librefundraiser.gui.flextable.FlexTableSelectionEvent;
import net.sf.librefundraiser.io.Donor;
import net.sf.librefundraiser.io.DonorData.Type;
import net.sf.librefundraiser.io.Gift;
import net.sf.librefundraiser.io.GiftStats;

import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class DonorEditForm extends Composite {
	private Text txtFirstName;
	private Text txtLastName;
	private Text txtHomePhone;
	private Text txtWorkPhone;
	private Text txtFax;
	private Text txtOptional;
	private Combo comboCity;
	private Text txtAddress1;
	private Combo comboZip;
	private Text txtAddress2;
	private Combo comboCountry;
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
	//ModifyListener sends false positives when comboboxes are dropped
	private VerifyListener verifyListener = new VerifyListener() {
		@Override
		public void verifyText(VerifyEvent e) {
			DonorEditForm.this.setEdited(true);
		}
	};
	private Text txtAccountID;
	private Composite compositeEditForm;
	private GridData gd_compositeEditForm;
	private ToolItem tltmAdd;
	private ToolItem tltmEdit;
	private ToolItem tltmDelete;
	private FlexTable<Gift> giftTable;
	private Composite compositeGifts;
	private LinkEditForm grpWeb;
	private TabFolder tabFolder;
	private GridLayout mainGrid;
	private Composite compositeDetails;
	private Composite compositeMain;
	private Composite compositeAddressPadder;
	private Composite compositeAddress;
	private Composite compositeBigAddress;
	private Label sep1;
	private TabItem tbtmCustom;
	private Composite compositeCustom;
	private Label lblCategory_1;
	private Label lblCategory_2;
	private Text txtCategory;
	private Text txtCategory_1;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public DonorEditForm(Composite parent, int style, DonorTab donorTab) {
		super(parent, style);
		this.donorTab = donorTab;
		this.donor = donorTab.getDonor();

		mainGrid = new GridLayout(2, true);
		mainGrid.marginHeight = 0;
		mainGrid.marginWidth = 0;
		setLayout(mainGrid);

		compositeMain = new Composite(this, SWT.NONE);
		compositeMain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 3));
		compositeMain.setLayout(new GridLayout(1, false));

		Composite compositeBasic = new Composite(compositeMain, SWT.NONE);
		GridData gd_compositeBasic = new GridData(SWT.CENTER, SWT.CENTER, true,
				false, 1, 1);
		gd_compositeBasic.widthHint = 800;
		compositeBasic.setLayoutData(gd_compositeBasic);
		compositeBasic.setLayout(new GridLayout(1, false));

		Group grpType = new Group(compositeBasic, SWT.NONE);
		grpType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		grpType.setText("Type");
		grpType.setBounds(0, 0, 70, 82);

		SelectionAdapter typeSelect = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean business = e.getSource().equals(btnBusinessother);
				setBusiness(business);
			}
		};
		grpType.setLayout(new RowLayout(SWT.HORIZONTAL));

		btnIndividual = new Button(grpType, SWT.RADIO);
		btnIndividual.addSelectionListener(typeSelect);
		btnIndividual.setSelection(true);
		btnIndividual.setText("Individual");

		btnBusinessother = new Button(grpType, SWT.RADIO);
		btnBusinessother.setText("Business/Other");
		btnBusinessother.addSelectionListener(typeSelect);

		compositeNames = new Composite(compositeBasic, SWT.NONE);
		compositeNames.setLayout(new StackLayout());
		compositeNames.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		compositeIndvNames = new Composite(compositeNames, SWT.NONE);
		GridLayout gl_compositeIndvNames = new GridLayout(4, false);
		gl_compositeIndvNames.marginBottom = -5;
		gl_compositeIndvNames.marginTop = -5;
		gl_compositeIndvNames.marginRight = -5;
		gl_compositeIndvNames.marginLeft = -5;
		compositeIndvNames.setLayout(gl_compositeIndvNames);
		compositeIndvNames.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));

		Label lblFirstName = new Label(compositeIndvNames, SWT.NONE);
		lblFirstName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblFirstName.setText("First Name");

		txtFirstName = new Text(compositeIndvNames, SWT.BORDER);
		txtFirstName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		txtFirstName.addVerifyListener(verifyListener);

		Label lblLastName = new Label(compositeIndvNames, SWT.NONE);
		lblLastName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblLastName.setText("Last Name");

		txtLastName = new Text(compositeIndvNames, SWT.BORDER);
		txtLastName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Label lblSpouseFirst = new Label(compositeIndvNames, SWT.NONE);
		lblSpouseFirst.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblSpouseFirst.setText("Spouse First");

		txtSpouseFirst = new Text(compositeIndvNames, SWT.BORDER);
		txtSpouseFirst.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Label lblSpouseLast = new Label(compositeIndvNames, SWT.NONE);
		lblSpouseLast.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblSpouseLast.setText("Spouse Last");

		txtSpouseLast = new Text(compositeIndvNames, SWT.BORDER);
		txtSpouseLast.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		compositeBusiNames = new Composite(compositeNames, SWT.NONE);
		GridLayout gl_compositeBusiNames = new GridLayout(4, false);
		gl_compositeBusiNames.marginTop = -5;
		gl_compositeBusiNames.marginRight = -5;
		gl_compositeBusiNames.marginLeft = -5;
		gl_compositeBusiNames.marginBottom = -5;
		compositeBusiNames.setLayout(gl_compositeBusiNames);

		Label lblBusinessName = new Label(compositeBusiNames, SWT.NONE);
		lblBusinessName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		lblBusinessName.setText("Business Name");

		txtBusinessName = new Text(compositeBusiNames, SWT.BORDER);
		txtBusinessName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));

		Label lblContactFirst = new Label(compositeBusiNames, SWT.NONE);
		lblContactFirst.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		lblContactFirst.setText("Contact First");

		txtContactFirst = new Text(compositeBusiNames, SWT.BORDER);
		txtContactFirst.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Label lblContactLast = new Label(compositeBusiNames, SWT.NONE);
		lblContactLast.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblContactLast.setText("Contact Last");

		txtContactLast = new Text(compositeBusiNames, SWT.BORDER);
		txtContactLast.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Label sep = new Label(compositeMain, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite compositeMisc = new Composite(compositeMain, SWT.NONE);
		GridData gd_compositeMisc = new GridData(SWT.CENTER, SWT.CENTER, true,
				false, 1, 1);
		gd_compositeMisc.widthHint = 800;
		compositeMisc.setLayoutData(gd_compositeMisc);
		compositeMisc.setLayout(new GridLayout(4, false));

		Label lblSalutation = new Label(compositeMisc, SWT.NONE);
		lblSalutation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblSalutation.setBounds(0, 0, 55, 15);
		lblSalutation.setText("Salutation");

		// TODO:implement proper autosuggest and field filling
		comboSalutation = new Combo(compositeMisc, SWT.NONE);
		comboSalutation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		comboSalutation.setBounds(0, 0, 91, 23);

		Label lblHomePhone = new Label(compositeMisc, SWT.NONE);
		lblHomePhone.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblHomePhone.setBounds(0, 0, 55, 15);
		lblHomePhone.setText("Home Phone");

		txtHomePhone = new Text(compositeMisc, SWT.BORDER);
		txtHomePhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		txtHomePhone.setBounds(0, 0, 76, 21);

		Label lblCategory = new Label(compositeMisc, SWT.NONE);
		lblCategory.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblCategory.setBounds(0, 0, 55, 15);
		lblCategory.setText("Category");

		comboCategory = new Combo(compositeMisc, SWT.NONE);
		comboCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		comboCategory.setBounds(0, 0, 91, 23);

		Label lblWorkPhone = new Label(compositeMisc, SWT.NONE);
		lblWorkPhone.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblWorkPhone.setBounds(0, 0, 55, 15);
		lblWorkPhone.setText("Work Phone");

		txtWorkPhone = new Text(compositeMisc, SWT.BORDER);
		txtWorkPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		txtWorkPhone.setBounds(0, 0, 76, 21);

		Label lblDonorSource = new Label(compositeMisc, SWT.NONE);
		lblDonorSource.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblDonorSource.setBounds(0, 0, 55, 15);
		lblDonorSource.setText("Donor Source");

		comboDonorSource = new Combo(compositeMisc, SWT.NONE);
		comboDonorSource.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		comboDonorSource.setBounds(0, 0, 91, 23);

		Label lblFax = new Label(compositeMisc, SWT.NONE);
		lblFax.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblFax.setBounds(0, 0, 55, 15);
		lblFax.setText("Fax");

		txtFax = new Text(compositeMisc, SWT.BORDER);
		txtFax.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));
		txtFax.setBounds(0, 0, 76, 21);
		compositeMisc.setTabList(new Control[] { comboSalutation,
				comboCategory, comboDonorSource, txtHomePhone, txtWorkPhone,
				txtFax });

		sep1 = new Label(compositeMain, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		compositeBigAddress = new Composite(compositeMain, SWT.NONE);
		GridData gd_compositeBigAddress = new GridData(SWT.CENTER, SWT.CENTER,
				true, false, 1, 1);
		gd_compositeBigAddress.exclude = true;
		gd_compositeBigAddress.widthHint = 800;
		compositeBigAddress.setLayoutData(gd_compositeBigAddress);
		GridLayout gl_compositeBigAddress = new GridLayout(1, false);
		gl_compositeBigAddress.marginWidth = 0;
		gl_compositeBigAddress.marginHeight = 0;
		compositeBigAddress.setLayout(gl_compositeBigAddress);
		compositeBigAddress.setVisible(false);

		tabFolder = new TabFolder(compositeMain, SWT.NONE);
		tabFolder.setSelection(0);
		GridData gd_tabFolder = new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1);
		gd_tabFolder.heightHint = 500;
		tabFolder.setLayoutData(gd_tabFolder);

		compositeAddressPadder = new Composite(tabFolder, SWT.NONE);
		compositeAddressPadder.setLayout(new GridLayout(1, false));

		compositeAddress = new Composite(compositeAddressPadder, SWT.NONE);
		compositeAddress.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				true, 1, 1));
		GridLayout gl_compositeAddress = new GridLayout(4, false);
		gl_compositeAddress.horizontalSpacing = 10;
		compositeAddress.setLayout(gl_compositeAddress);

		GridData gd_compositeAddress = new GridData(SWT.FILL, SWT.TOP, true,
				true, 1, 1);
		gd_compositeAddress.minimumWidth = 0;
		gd_compositeAddress.minimumHeight = 0;
		gd_compositeAddress.heightHint = SWT.DEFAULT;
		gd_compositeAddress.widthHint = SWT.DEFAULT;
		compositeAddress.setLayoutData(gd_compositeAddress);
		if (System.getProperty("os.name").indexOf("nux") >= 0) {
			compositeAddress.setBackground(compositeBigAddress.getBackground());
			compositeAddress.setBackgroundMode(SWT.INHERIT_FORCE);
		}
		((GridData) compositeBigAddress.getLayoutData()).exclude = false;
		compositeAddress.setParent(compositeBigAddress);
		compositeBigAddress.setVisible(true);
		compositeAddress.setVisible(true);

		compositeOptional = new Composite(compositeAddress, SWT.NONE);
		compositeOptional.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1));
		compositeOptional.setLayout(new StackLayout());

		lblOptional = new Label(compositeOptional, SWT.NONE);
		lblOptional.setAlignment(SWT.RIGHT);
		lblOptional.setText("(optional)");

		lblContact = new Label(compositeOptional, SWT.NONE);
		lblContact.setAlignment(SWT.RIGHT);
		lblContact.setText("Contact");

		txtOptional = new Text(compositeAddress, SWT.BORDER);
		txtOptional.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				true, 1, 1));
		txtOptional.setBounds(0, 0, 76, 21);

		Label lblCity = new Label(compositeAddress, SWT.NONE);
		lblCity.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblCity.setBounds(0, 0, 55, 15);
		lblCity.setText("City");

		comboCity = new Combo(compositeAddress, SWT.BORDER);
		comboCity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true,
				1, 1));
		comboCity.setBounds(0, 0, 76, 21);

		Label lblMailingName = new Label(compositeAddress, SWT.NONE);
		lblMailingName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblMailingName.setBounds(0, 0, 55, 15);
		lblMailingName.setText("Mailing Name");

		comboMailingName = new Combo(compositeAddress, SWT.NONE);
		comboMailingName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				true, 1, 1));
		comboMailingName.setBounds(0, 0, 91, 23);

		Label lblState = new Label(compositeAddress, SWT.NONE);
		lblState.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblState.setBounds(0, 0, 55, 15);
		lblState.setText("State/Province");

		String[] states = { "Alabama", "Alaska", "Alberta",
				"Arizona", "Arkansas", "British Columbia",
				"California", "Colorado", "Connecticut", "Delaware",
				"District of Columbia", "Florida", "Georgia", "Hawaii",
				"Idaho", "Illinois", "Indiana", "Iowa", "Kansas",
				"Kentucky", "Louisiana", "Maine", "Manitoba",
				"Maryland", "Massachusetts", "Michigan", "Minnesota",
				"Mississippi", "Missouri", "Montana", "Nebraska",
				"Nevada", "New Brunswick", "New Hampshire",
				"New Jersey", "New Mexico", "New York", "Newfoundland",
				"North Carolina", "North Dakota",
				"Northwest Territories", "Nova Scotia", "Nunavut",
				"Ohio", "Oklahoma", "Ontario", "Oregon",
				"Pennsylvania", "Prince Edward Island", "Quebec",
				"Rhode Island", "Saskatchewan", "South Carolina",
				"South Dakota", "Tennessee", "Texas", "Utah",
				"Vermont", "Virgin Islands", "Virginia", "Washington",
				"West Virginia", "Wisconsin", "Wyoming", "Yukon" };
		comboState = new Combo(compositeAddress, SWT.NONE);
		comboState.setItems(states);
		comboState.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true,
				1, 1));
		comboState.setBounds(0, 0, 91, 23);
		new AutoCompleteField(comboState, new ComboContentAdapter(), states);

		Label lblAddress1 = new Label(compositeAddress, SWT.NONE);
		lblAddress1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblAddress1.setBounds(0, 0, 55, 15);
		lblAddress1.setText("Address 1");

		txtAddress1 = new Text(compositeAddress, SWT.BORDER);
		txtAddress1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				true, 1, 1));
		txtAddress1.setBounds(0, 0, 76, 21);

		Label lblZip = new Label(compositeAddress, SWT.NONE);
		lblZip.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblZip.setBounds(0, 0, 55, 15);
		lblZip.setText("Zip");

		comboZip = new Combo(compositeAddress, SWT.BORDER);
		comboZip.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true,
				1, 1));
		comboZip.setBounds(0, 0, 76, 21);

		Label lblAddress2 = new Label(compositeAddress, SWT.NONE);
		lblAddress2
				.setToolTipText("This is the primary address the post office will deliver to. ");
		lblAddress2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblAddress2.setBounds(0, 0, 55, 15);
		lblAddress2.setText("Address 2");

		txtAddress2 = new Text(compositeAddress, SWT.BORDER);
		txtAddress2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				true, 1, 1));
		txtAddress2.setBounds(0, 0, 76, 21);

		Label lblCountry = new Label(compositeAddress, SWT.NONE);
		lblCountry.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblCountry.setBounds(0, 0, 55, 15);
		lblCountry.setText("Country");

		comboCountry = new Combo(compositeAddress, SWT.BORDER);
		comboCountry.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				true, 1, 1));
		comboCountry.setBounds(0, 0, 76, 21);
		compositeAddress.setTabList(new Control[] { txtOptional,
				comboMailingName, txtAddress1, txtAddress2, comboCity,
				comboState, comboZip, comboCountry });

		TabItem tbtmOther = new TabItem(tabFolder, SWT.NONE);
		tbtmOther.setText("Email/Web");

		Composite compositeOther = new Composite(tabFolder, SWT.NONE);
		tbtmOther.setControl(compositeOther);
		compositeOther.setLayout(new GridLayout(1, false));

		Group grpEmail = new Group(compositeOther, SWT.NONE);
		grpEmail.setLayout(new GridLayout(4, false));
		grpEmail.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
				1));
		grpEmail.setText("Email");
		grpEmail.setBounds(0, 0, 70, 82);

		Label lblMain = new Label(grpEmail, SWT.NONE);
		lblMain.setBounds(0, 0, 55, 15);
		lblMain.setText("Main");

		txtMain = new Text(grpEmail, SWT.BORDER);
		txtMain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		txtMain.setBounds(0, 0, 76, 21);

		Label lblOther = new Label(grpEmail, SWT.NONE);
		lblOther.setBounds(0, 0, 55, 15);
		lblOther.setText("Other");

		txtOther = new Text(grpEmail, SWT.BORDER);
		txtOther.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		txtOther.setBounds(0, 0, 76, 21);

		grpWeb = new LinkEditForm(compositeOther, SWT.NONE, donor);
		grpWeb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Group grpNotes = new Group(this, SWT.NONE);
		grpNotes.setText("Notes");
		grpNotes.setLayout(new FillLayout(SWT.HORIZONTAL));

		txtNotes = new Text(grpNotes, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL
				| SWT.MULTI);
		grpNotes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		compositeGifts = new Composite(DonorEditForm.this, SWT.NONE);
		GridLayout gl_compositeGifts = new GridLayout(1, false);
		gl_compositeGifts.marginWidth = 0;
		gl_compositeGifts.marginHeight = 0;
		compositeGifts.setLayout(gl_compositeGifts);
		compositeGifts.setBackgroundMode(SWT.INHERIT_DEFAULT);
		compositeGifts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		if (System.getProperty("os.name").indexOf("nux") >= 0) {
			compositeGifts.setBackground(DonorEditForm.this.getBackground());
		}

		ToolBar tbrGifts = new ToolBar(compositeGifts, SWT.FLAT | SWT.RIGHT);
		tbrGifts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		tbrGifts.setBounds(0, 0, 89, 23);

		tltmAdd = new ToolItem(tbrGifts, SWT.NONE);
		tltmAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Gift gift = new Gift(Main.getDonorDB().getUniqueRecNum());
				gift.setAccount(donor.id);
				editGift(gift);
			}
		});
		tltmAdd.setImage(ResourceManager.getIcon("new-gift.png"));
		tltmAdd.setText("Add");

		ToolItem tltmSep = new ToolItem(tbrGifts, SWT.SEPARATOR);
		tltmSep.setText("sep");

		tltmEdit = new ToolItem(tbrGifts, SWT.NONE);
		tltmEdit.setEnabled(false);
		tltmEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editGift(giftTable.getSelection());
			}
		});
		tltmEdit.setImage(ResourceManager.getIcon("edit-gift.png"));
		tltmEdit.setText("Edit");

		tltmDelete = new ToolItem(tbrGifts, SWT.NONE);
		tltmDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Main.getDonorDB().deleteGift(giftTable.getSelection().recnum);
				Main.getDonorDB().refreshGifts(donor);
				giftTable.refresh();
				tltmDelete.setEnabled(false);
				tltmEdit.setEnabled(false);
			}
		});
		tltmDelete.setImage(ResourceManager.getIcon("delete-gift.png"));
		tltmDelete.setEnabled(false);
		tltmDelete.setText("Delete");

		compositeEditForm = new Composite(compositeGifts, SWT.NONE);
		gd_compositeEditForm = new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1);
		gd_compositeEditForm.exclude = true;
		compositeEditForm.setLayoutData(gd_compositeEditForm);
		compositeEditForm.setLayout(new FillLayout(SWT.HORIZONTAL));
		//TODO:Add sorting functionality
		giftTable = new FlexTable<>(compositeGifts, SWT.BORDER);
		giftTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,1));
		giftTable.setHighlightColumn(1);
		giftTable.addSelectionListener(new FlexTableSelectionAdapter<Gift>() {
			@Override
			public void widgetSelected(FlexTableSelectionEvent<Gift> e) {
				if (e.target != null) {
					tltmEdit.setEnabled(true);
					tltmDelete.setEnabled(true);
				} else {
					tltmEdit.setEnabled(false);
					tltmDelete.setEnabled(false);
				}
			}
		});
		giftTable.setDataProvider(new FlexTableDataProvider<Gift>() {
			final String[] headers = {"Amount", "Date Given", "Letter", "Entry Date", "Gift Source", "Note", "Record Number"};
			final int AMOUNT = 0, DATE_GIVEN = 1, LETTER = 2, ENTRY_DATE = 3, GIFT_SOURCE = 4, NOTE = 5, RECORD_NUMBER = 6;
			List<Gift> gifts = donor.getGifts();
			{
				Collections.sort(gifts);
			}
			@Override
			public int size() {
				return gifts.size();
			}
			
			@Override
			public String[] getHeaders() {
				return headers;
			}
			
			@Override
			public String get(int i, int field) {
				Gift gift = this.get(i);
				Object data;
				switch (field) {
				case AMOUNT:
					data = gift.getAmount();
					break;
				case DATE_GIVEN:
					data = gift.getDategiven();
					break;
				case LETTER:
					data = gift.isLetter();
					break;
				case ENTRY_DATE:
					data = gift.getDt_entry();
					break;
				case GIFT_SOURCE:
					data = gift.getSource();
					break;
				case NOTE:
					data = gift.getNote();
					break;
				case RECORD_NUMBER:
					data = gift.getRecNum();
					break;
				default:
					data = "Missing Data";
				}
				if (data instanceof Date) {
					data = data != null ? Main.getDateFormat().format((Date) data) : "Never";
				}
				return String.valueOf(data);
			}
			
			@Override
			public Gift get(int i) {
				return gifts.get(i);
			}
			
			@Override
			public int columnCount() {
				return headers.length;
			}
			
			@Override
			public void refresh() {
				gifts = donor.getGifts();
				Collections.sort(gifts);
			}

			@Override
			public boolean sort(int field) {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
//		giftTable = new GiftTable(compositeGifts, SWT.NONE, donor);
//		giftTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
//				1));
//		giftTable.getTable().addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				TableItem[] selection = giftTable.getTable().getSelection();
//				if (selection.length == 1)
//					tltmEdit.setEnabled(true);
//				if (selection.length > 0)
//					tltmDelete.setEnabled(true);
//			}
//		});

		compositeDetails = new Composite(DonorEditForm.this, SWT.NONE);
		GridLayout gl_compositeDetails = new GridLayout(2, false);
		gl_compositeDetails.marginHeight = 0;
		gl_compositeDetails.marginWidth = 0;
		compositeDetails.setLayout(gl_compositeDetails);
		compositeDetails.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1));
		if (System.getProperty("os.name").indexOf("nux") >= 0) {
			compositeDetails.setBackground(DonorEditForm.this.getBackground());
			compositeDetails.setBackgroundMode(SWT.INHERIT_FORCE);
		}

		Group grpGifts = new Group(compositeDetails, SWT.NONE);
		grpGifts.setLayout(new GridLayout(4, false));
		grpGifts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		grpGifts.setText("Gifts");

		Label lblTotalGiven = new Label(grpGifts, SWT.NONE);
		lblTotalGiven.setBounds(0, 0, 55, 15);
		lblTotalGiven.setText("Total Given");

		txtTotalGiven = new Text(grpGifts, SWT.BORDER);
		txtTotalGiven.setEditable(false);
		txtTotalGiven.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		txtTotalGiven.setBounds(0, 0, 76, 21);
		txtTotalGiven.setData("money");

		Label lblLargestGift = new Label(grpGifts, SWT.NONE);
		lblLargestGift.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblLargestGift.setText("Largest Gift");

		txtLargestGift = new Text(grpGifts, SWT.BORDER);
		txtLargestGift.setEditable(false);
		txtLargestGift.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		txtLargestGift.setData("money");

		Label lblYearToDate = new Label(grpGifts, SWT.NONE);
		lblYearToDate.setText("Year to Date");

		txtYearToDate = new Text(grpGifts, SWT.BORDER);
		txtYearToDate.setEditable(false);
		txtYearToDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		txtYearToDate.setData("money");

		Label lblFirstGiftDate = new Label(grpGifts, SWT.NONE);
		lblFirstGiftDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		lblFirstGiftDate.setText("First Gift Date");

		txtFirstGiftDate = new Text(grpGifts, SWT.BORDER);
		txtFirstGiftDate.setEditable(false);
		txtFirstGiftDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Label lblLastGiftAmt = new Label(grpGifts, SWT.NONE);
		lblLastGiftAmt.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblLastGiftAmt.setText("Last Gift Amt");

		txtLastGiftAmt = new Text(grpGifts, SWT.BORDER);
		txtLastGiftAmt.setEditable(false);
		txtLastGiftAmt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		txtLastGiftAmt.setData("money");

		Label lblLastGiftDate = new Label(grpGifts, SWT.NONE);
		lblLastGiftDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		lblLastGiftDate.setText("Last Gift Date");

		txtLastGiftDate = new Text(grpGifts, SWT.BORDER);
		txtLastGiftDate.setEditable(false);
		txtLastGiftDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Group grpOther = new Group(compositeDetails, SWT.NONE);
		grpOther.setLayout(new GridLayout(2, false));
		grpOther.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		grpOther.setText("Other");

		Label lblLastEdited = new Label(grpOther, SWT.NONE);
		lblLastEdited.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblLastEdited.setText("Last Edited");

		txtLastEdited = new Text(grpOther, SWT.BORDER);
		txtLastEdited.setEditable(false);
		txtLastEdited.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Label lblAccountId = new Label(grpOther, SWT.NONE);
		lblAccountId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblAccountId.setText("Account ID");

		txtAccountID = new Text(grpOther, SWT.BORDER);
		txtAccountID.setEditable(false);
		txtAccountID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		tbtmCustom = new TabItem(tabFolder, SWT.NONE);
		tbtmCustom.setText("Custom");

		// TODO use custom fields from properties dialog
		compositeCustom = new Composite(tabFolder, SWT.NONE);
		tbtmCustom.setControl(compositeCustom);
		compositeCustom.setLayout(new GridLayout(2, false));

		lblCategory_1 = new Label(compositeCustom, SWT.NONE);
		lblCategory_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblCategory_1.setText("Category1");

		txtCategory = new Text(compositeCustom, SWT.BORDER);
		txtCategory.setText("Category 1");
		txtCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		lblCategory_2 = new Label(compositeCustom, SWT.NONE);
		lblCategory_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblCategory_2.setText("Category2");

		txtCategory_1 = new Text(compositeCustom, SWT.BORDER);
		txtCategory_1.setText("Category 2");
		txtCategory_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		// load previous values in another thread
		new Thread(new Runnable() {
			@Override
			public void run() {
				Object[][] toFill = new Object[][] {
						{ comboCategory, "category1" },
						{ comboDonorSource, "category2" },
						{ comboCity, "city" }, { comboZip, "zip" },
						{ comboCountry, "country" } };
				for (Object[] data : toFill) {
					final Combo combo = (Combo) data[0];
					String dbField = (String) data[1];
					final String[] previousValues = Main.getDonorDB()
							.getPreviousDonorValues(dbField).toArray(new String[0]);
					getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							// temporarily remove modifylistener to prevent tab
							// from incorrectly being marked as unsaved
							combo.removeVerifyListener(verifyListener);
							String prevText = combo.getText();
							combo.setItems(previousValues);
							new AutoCompleteField(combo, new ComboContentAdapter(), previousValues);
							combo.setText(prevText);
							combo.addVerifyListener(verifyListener);
						}
					});
				}
			}
		}).start();
		this.fillForm();
		this.attachModifyListeners();
		this.setBusiness(donor.data.getType() != Type.I);
		this.setEdited(false);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	private void attachModifyListeners() {
		txtFirstName.addVerifyListener(verifyListener);
		txtLastName.addVerifyListener(verifyListener); 
		txtBusinessName.addVerifyListener(verifyListener);
		txtSpouseFirst.addVerifyListener(verifyListener);
		txtSpouseLast.addVerifyListener(verifyListener);
		txtContactLast.addVerifyListener(verifyListener);
		txtContactFirst.addVerifyListener(verifyListener);
		txtHomePhone.addVerifyListener(verifyListener); 
		txtWorkPhone.addVerifyListener(verifyListener);
		txtFax.addVerifyListener(verifyListener); 
		txtOptional.addVerifyListener(verifyListener);
		comboCity.addVerifyListener(verifyListener); 
		txtAddress1.addVerifyListener(verifyListener);
		comboZip.addVerifyListener(verifyListener); 
		txtAddress2.addVerifyListener(verifyListener);
		comboCountry.addVerifyListener(verifyListener); 
		txtNotes.addVerifyListener(verifyListener);
		txtMain.addVerifyListener(verifyListener);
		txtOther.addVerifyListener(verifyListener); 
		comboSalutation.addVerifyListener(verifyListener);
		comboCategory.addVerifyListener(verifyListener);
		comboDonorSource.addVerifyListener(verifyListener);
		comboMailingName.addVerifyListener(verifyListener); 
		comboState.addVerifyListener(verifyListener);
	}

	private void fillForm() {
		txtFirstName.setText(donor.data.getFirstname());
		txtLastName.setText(donor.data.getLastname()); 
		txtBusinessName.setText(donor.data.getLastname());
		txtSpouseFirst.setText(donor.data.getSpousefrst());
		txtSpouseLast.setText(donor.data.getSpouselast());
		txtContactLast.setText(donor.data.getSpouselast());
		txtContactFirst.setText(donor.data.getSpousefrst());
		txtHomePhone.setText(donor.data.getHomephone()); 
		txtWorkPhone.setText(donor.data.getWorkphone());
		txtFax.setText(donor.data.getFax()); 
		txtOptional.setText(donor.data.getContact());
		comboCity.setText(donor.data.getCity()); 
		txtAddress1.setText(donor.data.getAddress1());
		comboZip.setText(donor.data.getZip()); 
		txtAddress2.setText(donor.data.getAddress2());
		comboCountry.setText(donor.data.getCountry()); 
		txtNotes.setText(donor.data.getNotes());
		txtMain.setText(donor.data.getEmail());
		txtOther.setText(donor.data.getEmail2()); 
		txtLastEdited.setText(Main.getDateFormat().format(donor.data.getChangedate()));
		comboSalutation.setText(donor.data.getSalutation());
		comboCategory.setText(donor.data.getCategory1());
		comboDonorSource.setText(donor.data.getCategory2());
		comboMailingName.setText(donor.data.getMailname()); 
		comboState.setText(donor.data.getState());
		txtAccountID.setText(donor.getAccountNum());
		
		GiftStats gs = donor.getGiftStats();
		txtTotalGiven.setText(String.valueOf(gs.getAllTime())); 
		txtYearToDate.setText(String.valueOf(gs.getYearToDt()));
		txtLargestGift.setText(String.valueOf(gs.getLargest()));
		txtFirstGiftDate.setText(Main.getDateFormat().format(gs.getFirstGift()));
		txtLastGiftAmt.setText(String.valueOf(gs.getLastAmt()));
		txtLastGiftDate.setText(Main.getDateFormat().format(gs.getLastGiveDt()));
	}

	public void saveForm(boolean refresh) {
		if (!business) {
			txtBusinessName.setText(txtLastName.getText());
			txtContactLast.setText(txtSpouseLast.getText());
			txtContactFirst.setText(txtSpouseFirst.getText());
		} else {
			txtLastName.setText(txtBusinessName.getText());
			txtSpouseFirst.setText(txtContactFirst.getText());
			txtSpouseLast.setText(txtContactLast.getText());
		}
		donor.data.setType(business ? Type.B : Type.I);
		Format dateFormat = Main.getDateFormat();
		//TODO actually update the changeDate field
		txtLastEdited.setText(dateFormat.format(new Date()));
		
		donor.data.setFirstname(txtFirstName.getText());
		donor.data.setLastname(txtLastName.getText()); 
		donor.data.setSpousefrst(txtSpouseFirst.getText());
		donor.data.setSpouselast(txtSpouseLast.getText());
		donor.data.setHomephone(txtHomePhone.getText()); 
		donor.data.setWorkphone(txtWorkPhone.getText());
		donor.data.setFax(txtFax.getText()); 
		donor.data.setContact(txtOptional.getText());
		donor.data.setCity(comboCity.getText()); 
		donor.data.setAddress1(txtAddress1.getText());
		donor.data.setZip(comboZip.getText()); 
		donor.data.setAddress2(txtAddress2.getText());
		donor.data.setCountry(comboCountry.getText()); 
		donor.data.setNotes(txtNotes.getText());
		donor.data.setEmail(txtMain.getText());
		donor.data.setEmail2(txtOther.getText()); 
		donor.data.setSalutation(comboSalutation.getText());
		donor.data.setCategory1(comboCategory.getText());
		donor.data.setCategory2(comboDonorSource.getText());
		donor.data.setMailname(comboMailingName.getText()); 
		donor.data.setState(comboState.getText());
		
		grpWeb.saveLinks();
		Main.getDonorDB().saveDonor(this.donor);
		if (refresh)
			Main.getWindow().refresh(true, false);
		this.fillForm();
		this.setEdited(false);
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
		((StackLayout) compositeNames.getLayout()).topControl = business ? compositeBusiNames
				: compositeIndvNames;
		((StackLayout) compositeOptional.getLayout()).topControl = business ? lblContact
				: lblOptional;
		this.getShell().layout();
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
		String lastname = donor.data.getLastname();
		String firstname = donor.data.getFirstname();
		String tabTitle = lastname
				+ (!(lastname.equals("") || firstname.equals("")) ? ", " : "")
				+ firstname;
		if (tabTitle.equals(""))
			tabTitle = donor.getAccountNum();
		donorTab.setText((edited ? "*" : "") + tabTitle);
		donorTab.setImage(edited ? DonorTab.edited : DonorTab.unedited);
		ToolItem saveButton = Main.getWindow().getSaveButton();
		saveButton.setEnabled(edited);
		if (edited) {
			final DonorEditForm me = this;
			Main.getWindow().setSaveAction(new Runnable() {
				@Override
				public void run() {
					me.saveForm(true);
				}
			});
		}
	}

	public void editGift(Gift gift) {
		if (compositeEditForm.getChildren().length == 0) {
			final GiftEditForm giftEditForm = new GiftEditForm(
					compositeEditForm, SWT.NONE, gift);
			giftEditForm.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					try {
						if (!giftEditForm.canceled) {
							Gift gift = giftEditForm.getGift();
							donor.addGift(gift);
							setEdited(true);
						}
						compositeEditForm.setVisible(false);
						gd_compositeEditForm.exclude = true;
						compositeEditForm.layout();
						compositeGifts.layout();
						giftTable.refresh();
						giftTable.setEnabled(true);
						tltmAdd.setEnabled(true);
					} catch (SWTException e1) {
					}
				}
			});
			compositeEditForm.setVisible(true);
			gd_compositeEditForm.exclude = false;
			compositeEditForm.layout();
			compositeGifts.layout();
			giftEditForm.initializePointer();
			tltmAdd.setEnabled(false);
			tltmEdit.setEnabled(false);
			tltmDelete.setEnabled(false);
			giftTable.setEnabled(false);
		}
	}
}
