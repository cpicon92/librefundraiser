package net.sf.librefundraiser.gui;
import net.sf.librefundraiser.Donor;
import net.sf.librefundraiser.LibreFundraiser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;


public class DonorTab extends CTabItem {
	@SuppressWarnings("unused")
	private final int id;
	private final Donor donor;
	
	public DonorTab(int id, CTabFolder tabFolder) {
		super(tabFolder, SWT.NONE);
		this.id = id;
		this.setShowClose(true);
		donor = LibreFundraiser.getLocalDB().getDonors("where ACCOUNT=\""+String.format("%06d",id)+"\"")[0];
		DonorEditForm editForm = new DonorEditForm(this.getParent(),SWT.NONE,this);
		this.setControl(editForm);
	}
	
	public Donor getDonor() {
		return donor;
	}

	public void alterSaveButton() {
		DonorEditForm form = ((DonorEditForm)this.getControl());
		form.setEdited(form.isEdited());
	}
	
}
