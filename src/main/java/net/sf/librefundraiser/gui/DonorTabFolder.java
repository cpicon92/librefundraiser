package net.sf.librefundraiser.gui;
import org.eclipse.nebula.widgets.opal.dialog.ChoiceItem;
import org.eclipse.nebula.widgets.opal.dialog.Dialog;
import org.eclipse.nebula.widgets.opal.dialog.Dialog.OpalDialogType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.tabs.TabFolder;
import net.sf.librefundraiser.tabs.TabFolderEvent;
import net.sf.librefundraiser.tabs.TabFolderListener;
import net.sf.librefundraiser.tabs.TabItem;



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
			@Override
			public void close(TabFolderEvent event) {
				TabItem closing = event.item;
				if (!closing.getText().substring(0, 1).equals("*")) return;
				int choice = DonorTabFolder.verifyUnsaved(getShell(), closing.getText().substring(1));
				if (choice == 2) event.doit = false;
				if (choice == 0) ((DonorTab) closing).save(false);
			}
		});
		new Label(tabFolder, SWT.NONE);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DonorTab t = (DonorTab) tabFolder.getSelection();
				Main.getWindow().setSaveAction(t != null ? t.getSaveAction() : null);

			}
		});
		
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void newDonor() {
		tabFolder.setSelection(new DonorTab(-1,tabFolder));
	}

	public void saveAll() {
		for (TabItem i : tabFolder.getTabs()) {
			if (i.getClass().equals(DonorTab.class)) {
				((DonorTab)i).save(false);
			}
		}
		TabItem t = tabFolder.getSelection();
		if (!t.getClass().equals(DonorTab.class)) {
			Main.getWindow().setSaveAction(null);
		} else {
			Main.getWindow().setSaveAction(((DonorTab)t).getSaveAction());
		}
		Main.getWindow().refresh();
	}

	public boolean closeAllTabs() {
		for (TabItem closing : tabFolder.getTabs()) {
			if (!closing.getText().substring(0, 1).equals("*")) continue;
			int choice = DonorTabFolder.verifyUnsaved(getShell(), closing.getText().substring(1));
			if (choice == 2) return false;
			if (choice == 0) ((DonorTab) closing).save(false);
			closing.dispose();
		}
		return true;
	}

	public static int verifyUnsaved(Shell shell, String donor) {
		final Dialog dialog = new Dialog(shell);
		dialog.setTitle("LibreFundraiser Warning");
		dialog.setButtonType(OpalDialogType.NONE);
		dialog.getMessageArea().setTitle(donor + " has unsaved changes, are you sure you want to close this donor?")
		.setIcon(shell.getDisplay().getSystemImage(SWT.ICON_WARNING))
		.addChoice(2, new ChoiceItem("Close and save my changes", "Save changes to this donor, then close. "),
				new ChoiceItem("Close and don't save", "Close without saving changes to donor.  All unsaved changes will \nbe erased."), 
				new ChoiceItem("Don't close", "Return to edit " + donor + ".")
				);
		dialog.show();
		return dialog.getMessageArea().getChoice();
	}

}
