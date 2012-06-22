package net.sf.librefundraiser.gui;
import net.sf.librefundraiser.Donor;
import net.sf.librefundraiser.Main;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.wb.swt.SWTResourceManager;


public class DonorTab extends CTabItem {
	private final Donor donor;
	Rectangle closeRect = new Rectangle(0, 0, 0, 0);
	
	public DonorTab(int id, CTabFolder tabFolder) {
		super(tabFolder, SWT.NONE);
		this.setShowClose(true);
		Donor donor;
		try {
			donor = Main.getDonorDB().getDonors("where ACCOUNT=\""+String.format("%06d",id)+"\"",true)[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			donor = new Donor(id);
		}
		this.donor = donor;
		DonorEditForm editForm = new DonorEditForm(this.getParent(),SWT.NONE,this);
		this.setControl(editForm);
		this.setImage(SWTResourceManager.getImage(DonorList.class, "/net/sf/librefundraiser/icons/donor-tab.png"));
	}
	
	public Donor getDonor() {
		return donor;
	}

	public void alterSaveButton() {
		DonorEditForm form = ((DonorEditForm)this.getControl());
		form.setEdited(form.isEdited());
	}
	
	public void save() {
		DonorEditForm form = ((DonorEditForm)this.getControl());
		if (form.isEdited()) {
			form.saveForm();
		}
	}
	
}
