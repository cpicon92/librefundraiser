package net.sf.librefundraiser.gui;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.ResourceManager;
import net.sf.librefundraiser.Util;
import net.sf.librefundraiser.gui.flextable.FlexTable;
import net.sf.librefundraiser.gui.flextable.FlexTableDataProvider;
import net.sf.librefundraiser.gui.flextable.FlexTableSelectionAdapter;
import net.sf.librefundraiser.gui.flextable.FlexTableSelectionEvent;
import net.sf.librefundraiser.io.Donor;
import net.sf.librefundraiser.io.DonorData.Type;
import net.sf.librefundraiser.io.GiftStats;
import net.sf.librefundraiser.tabs.TabFolder;
import net.sf.librefundraiser.tabs.TabItem;



public class DonorTable extends Composite {
	private FlexTable<Donor> table;

	final String[] headers = { "Account", "Type", "Last Name/Business",
			"First Name", "Spouse/Contact Last", "Spouse/Contact First",
			"Salutation", "Home Phone", "Work Phone", "Fax", "Category",
			"Donor Source", "Mail Name", "Address 1", "Address 2", "City",
			"State", "Zip", "Country", "Email", "Other Email", "Web", "Obsolete",
			"Last Change", "Last Gift Date", "Last Gift", "Total Gifts",
			"Year-to-date", "First Gift", "Largest Gift",
			"Last Entry Date", "Last Entry Amount", "Notes" };

	public List<Donor> donors = new ArrayList<>();
	private TabFolder tabFolder;

	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public DonorTable(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout(SWT.HORIZONTAL));


		Composite compositeTable = new Composite(this, SWT.NONE);
		GridLayout gl_compositeTable = new GridLayout(2, false);
		gl_compositeTable.marginHeight = 0;
		gl_compositeTable.marginWidth = 0;
		compositeTable.setLayout(gl_compositeTable);
		
		Label lblFilter = new Label(compositeTable, SWT.NONE);
		lblFilter.setAlignment(SWT.RIGHT);
		GridData gd_lblFilter = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_lblFilter.widthHint = 20;
		lblFilter.setLayoutData(gd_lblFilter);
		lblFilter.setImage(ResourceManager.getIcon("filter.png"));
				
		final Text txtFilter = new Text(compositeTable, SWT.BORDER);
		txtFilter.addModifyListener(new ModifyListener() {
			private long recentId;
			@Override
			public void modifyText(ModifyEvent e) {
				recentId = System.currentTimeMillis();
				DonorTable.this.getDisplay().timerExec(300, new Runnable() {
					long id = recentId;
					@Override
					public void run() {
						if (id != recentId) return;
						table.setFilter(txtFilter.getText());
						table.refresh();
					}
				});
			}
		});
		txtFilter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		table = new FlexTable<>(compositeTable, SWT.NONE);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table.addSelectionListener(new FlexTableSelectionAdapter<Donor>() {
			@Override
			public void widgetDefaultSelected(FlexTableSelectionEvent<Donor> e) {
				Donor selectedItem = table.getFirstSelection();
				int id = selectedItem.id;
				DonorTab newTab = DonorTable.this.openDonorTab(id);
				DonorTable.this.tabFolder.setSelection(newTab);
			}
		});
		table.setHeaderVisible(true);
		table.setMultiple(true);
		table.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				if (table.getClientArea().width < 700) {
					table.setSummaryMode(true);
				} else {
					table.setSummaryMode(false);
				}
			}
		});
		table.setDataProvider(new FlexTableDataProvider<Donor>() {
			boolean summaryMode;
			List<Donor> filteredDonors = new ArrayList<>();
			
			@Override
			public int size() {
				return filteredDonors.size();
			}
			
			@Override
			public int columnCount() {
				return headers.length;
			}
			
			@Override
			public String[] getHeaders() {
				return headers;
			}
			
			@Override
			public String get(int i, int field) {
				Donor donor = this.get(i);
				if (summaryMode) {
					GiftStats stats = donor.getGiftStats();
					switch (field) {
					case 0:
						return donor.getAccountNum();
					case 1:
						return donor.data.getType().getName();
					case 2:
						if (donor.data.getFirstname().isEmpty() || donor.data.getType() != Type.INDIVIDUAL) {
							return donor.data.getLastname();
						}
						return String.format("%s, %s", donor.data.getLastname(), donor.data.getFirstname());
					case 3:
						return stats.getLastGiveDt() != null ? new SimpleDateFormat("MMM. yyyy").format(stats.getLastGiveDt()) : "Never";
					default:
						return "";
					}
				} else {
					Object data = this.getData(donor, field);
					if (data instanceof Date) {
						data = formatDate((Date) data);
					}
					return String.valueOf(data);
				}
			}
			
			private String formatDate(Date d) {
				return d != null ? Main.getDateFormat().format(d) : "Never";
			}
			
			//TODO: incorporate these field index numbers into everything else somehow, or remove them
			public Object getData(Donor donor, int field) {
				GiftStats stats = donor.getGiftStats();
				Object data;
				switch (field) {
				case 0:
					data = donor.getAccountNum();
					break;
				case 1:
					data = donor.data.getType();
					break;
				case 2:
					data = donor.data.getLastname();
					break;
				case 3:
					data = donor.data.getFirstname();
					break;
				case 4:
					data = donor.data.getSpouselast();
					break;
				case 5:
					data = donor.data.getSpousefrst();
					break;
				case 6:
					data = donor.data.getSalutation();
					break;
				case 7:
					data = donor.data.getHomephone();
					break;
				case 8:
					data = donor.data.getWorkphone();
					break;
				case 9:
					data = donor.data.getFax();
					break;
				case 10:
					data = donor.data.getCategory();
					break;
				case 11:
					data = donor.data.getSource();
					break;
				case 12:
					data = donor.data.getMailname();
					break;
				case 13:
					data = donor.data.getAddress1();
					break;
				case 14:
					data = donor.data.getAddress2();
					break;
				case 15:
					data = donor.data.getCity();
					break;
				case 16:
					data = donor.data.getState();
					break;
				case 17:
					data = donor.data.getZip();
					break;
				case 18:
					data = donor.data.getCountry();
					break;
				case 19:
					data = donor.data.getEmail();
					break;
				case 20:
					data = donor.data.getEmail2();
					break;
				case 21:
					String url = donor.data.getWeb();
					// display web urls on one line in table
					url = url.replace("\n", "; ");
					// remove trailing semi-colon
					if (url.length() > 2) {
						url = url.substring(0, url.length() - 2);
					}
					data = url;
					break;
				case 22:
					data = donor.data.isObsolete() ? "Yes" : "No";
					break;
				case 23:
					data = donor.data.getChangedate();
					break;
				case 24:
					data = stats.getLastGiveDt();
					break;
				case 25:
					data = stats.getLastAmt();
					break;
				case 26:
					data = stats.getAllTime();
					break;
				case 27:
					data = stats.getYearToDt();
					break;
				case 28:
					data = stats.getFirstGift();
					break;
				case 29:
					data = stats.getLargest();
					break;
				case 30:
					data = stats.getLastEntDt();
					break;
				case 31:
					data = stats.getLastEntAmt();
					break;
				case 32:
					data = donor.data.getNotes();
					break;
				default:
					data = "Missing Data";
				}
				return data;
			}
			
			@Override
			public Donor get(int i) {
				return filteredDonors.get(i);
			}

			@Override
			public void refresh() {
				donors = Main.getDonorDB().getDonors();
				this.refilter();
			}

			private int currentField = -1;
			private boolean desc;
			@Override
			public boolean sort(final int field) {
				if (field == currentField) {
					desc = !desc;
				} else {
					currentField = field;
					desc = false;
				}
				Collections.sort(filteredDonors, new Comparator<Donor>() {
					@SuppressWarnings({ "unchecked", "rawtypes" })
					@Override
					public int compare(Donor d0, Donor d1) {
						if (desc) {
							Donor temp = d0;
							d0 = d1;
							d1 = temp;
						}
						Object data0 = getData(d0, field), 
						data1 = getData(d1, field);
						if (data0 instanceof Comparable && data1 instanceof Comparable) {
							try {
								return ((Comparable) data0).compareTo(data1);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						return String.valueOf(data0).compareTo(String.valueOf(data1));
					}
				});
				return true;
			}

			@Override
			public void setSummaryMode(boolean summaryMode) {
				this.summaryMode = summaryMode;
			}

			@Override
			public int getSortField() {
				if (summaryMode && currentField == 3) {
					return 2;
				}
				if (summaryMode && currentField == 4) {
					return -1;
				}
				if (summaryMode && currentField == 23) {
					return 3;
				}
				return currentField;
			}

			String filter = "";
			@Override
			public String getFilter() {
				return filter;
			}

			@Override
			public void setFilter(String filter) {
				if (!this.filter.equals(filter)) {
					this.filter = filter;
					this.refilter();
				}
			}
			
			private void refilter() {
				filteredDonors = new ArrayList<>(donors);
				if (filter == null || filter.isEmpty()) {
					return;
				}
				for (Iterator<Donor> iter = filteredDonors.iterator(); iter.hasNext();) {
					Donor d = iter.next();
					if (!d.match(filter)) {
						iter.remove();
					}
				}
			}

			@Override
			public boolean getSortAsc() {
				return !desc;
			}

		});

		Menu menuDonorList = new Menu(table);
		table.setMenu(menuDonorList);

		MenuItem mntmOpenDonor = new MenuItem(menuDonorList, SWT.NONE);
		mntmOpenDonor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final List<Donor> items = table.getSelection();
				int i = 0;
				for (Donor selectedItem : items) {
					DonorTab dt = DonorTable.this.openDonorTab(selectedItem);
					if (i++ == items.size() - 1) {
						DonorTable.this.tabFolder.setSelection(dt);
					}
				}
			}
		});
		mntmOpenDonor.setText("Open Donor(s)");

		MenuItem mntmOpenBackground = new MenuItem(menuDonorList, SWT.NONE);
		mntmOpenBackground.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (Donor selectedItem : table.getSelection()) {
					DonorTable.this.openDonorTab(selectedItem);
				}
			}
		});
		mntmOpenBackground.setText("Open Donor(s) in the Background");

		new MenuItem(menuDonorList, SWT.SEPARATOR);

		MenuItem mntmDeleteDonor = new MenuItem(menuDonorList, SWT.NONE);
		mntmDeleteDonor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteDonors();
			}
		});
		mntmDeleteDonor.setText("Delete Donor(s)");
	}

	public void refresh() {
		setVisible(false);
		final long beforetime = System.currentTimeMillis();
		table.refresh();
		System.out.printf("List refresh took: %ds\n", (System.currentTimeMillis()-beforetime)/1000);
		setVisible(true);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void newDonor() {
		int id = Main.getDonorDB().getMaxAccount()+1;
		for (TabItem t : tabFolder.getItems()) {
			try {
				DonorTab dt = (DonorTab) t;
				int dtId = dt.getDonor().getId();
				if (dtId >= id) id = dtId+1;
			} catch (Exception e) {
			}
		}
		tabFolder.setSelection(DonorTable.this.openDonorTab(id));
	}

	public void saveAll() {
		for (TabItem i : tabFolder.getItems()) {
			if (i.getClass().equals(DonorTab.class)) {
				((DonorTab)i).save(false);
			}
		}
		TabItem t = tabFolder.getSelection();
		if (!t.getClass().equals(DonorTab.class)) {
			Main.getWindow().getSaveButton().setEnabled(false);
		} else {
			((DonorTab)t).alterSaveButton();
		}
		refresh();
	}

	public void deleteDonors() {
		MessageBox warning = new MessageBox(this.getShell(),SWT.ICON_WARNING|SWT.YES|SWT.NO);
		warning.setText("LibreFundraiser Warning");
		warning.setMessage("All data for these donors will be erased IRRETRIEVABLY. Do you want to continue?");
		if (warning.open() == SWT.NO) return;
		int[] ids = new int[table.getSelectionCount()];
		int i = 0;
		for (Donor selectedItem : table.getSelection()) {
			ids[i++] = selectedItem.id;
		}
		Main.getDonorDB().deleteDonors(ids);
		refresh();
	}

	public TabFolder getTabFolder() {
		return tabFolder;
	}

	public void setTabFolder(TabFolder tabFolder) {
		this.tabFolder = tabFolder;
	}
	
	public DonorTab openDonorTab(Donor donor) {
		return new DonorTab(donor, DonorTable.this.tabFolder);
	}	
	
	public DonorTab openDonorTab(int id) {
		return new DonorTab(id, DonorTable.this.tabFolder);
	}
}
