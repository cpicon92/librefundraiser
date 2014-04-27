package net.sf.librefundraiser.gui;
import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.tabs.TabFolder;
import net.sf.librefundraiser.tabs.TabFolderEvent;
import net.sf.librefundraiser.tabs.TabFolderListener;
import net.sf.librefundraiser.tabs.TabItem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;



public class DonorTabFolder extends Composite {

	public TabFolder tabFolder;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public DonorTabFolder(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout(SWT.HORIZONTAL));
		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.addTabFolderListener(new TabFolderListener() {
			public void close(TabFolderEvent event) {
				TabItem closing = event.item;
				if (!closing.getText().substring(0, 1).equals("*")) return;
				MessageBox verify = new MessageBox(getShell(),SWT.YES | SWT.NO | SWT.ICON_WARNING);
				verify.setMessage(closing.getText().substring(1)+" has unsaved changes, are you sure you want to close this donor?");
				verify.setText("LibreFundraiser Warning");
				event.doit = verify.open() == SWT.YES;
			}
		});
		new Label(tabFolder, SWT.NONE);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TabItem t = tabFolder.getSelection();
				if (!t.getClass().equals(DonorTab.class)) {
					Main.getWindow().getSaveButton().setEnabled(false);
				} else {
					((DonorTab)t).alterSaveButton();
				}
			}
		});
		
	}

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
		tabFolder.setSelection(new DonorTab(id,tabFolder));
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
		Main.getWindow().refresh(true, false);
	}

	public boolean closeAllTabs() {
		for (TabItem closing : tabFolder.getItems()) {
			if (!closing.getText().substring(0, 1).equals("*")) continue;
			MessageBox verify = new MessageBox(getShell(),SWT.YES | SWT.NO | SWT.ICON_WARNING);
			verify.setMessage(closing.getText().substring(1)+" has unsaved changes, are you sure you want to close this donor?");
			verify.setText("LibreFundraiser Warning");
			if (verify.open() != SWT.YES) {
				return false;
			}
			closing.dispose();
		}
		return true;
	}

}
