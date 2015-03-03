package net.sf.librefundraiser.gui;
import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.ResourceManager;
import net.sf.librefundraiser.io.Donor;
import net.sf.librefundraiser.tabs.TabFolder;
import net.sf.librefundraiser.tabs.TabItem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;


public class DonorTab extends TabItem {
	private final Donor donor;
	Rectangle closeRect = new Rectangle(0, 0, 0, 0);
	public static final Image unedited = ResourceManager.getIcon("donor-tab.png");
	public static final Image edited = ResourceManager.getIcon("donor-tab_edited.png");
	
	public DonorTab(int id, TabFolder tabFolder) {
		super(tabFolder, SWT.NONE);
		Donor donor = Main.getDonorDB().getDonor(id);
		if (donor == null) {
			donor = new Donor(id);
		}
		this.donor = donor;
		DonorEditForm editForm = new DonorEditForm(tabFolder,SWT.NONE,this);
		this.setControl(editForm);
		this.setImage(unedited);
	}
	
	public DonorTab(Donor donor, TabFolder tabFolder) {
		super(tabFolder, SWT.NONE);
		this.donor = donor;
		DonorEditForm editForm = new DonorEditForm(tabFolder,SWT.NONE,this);
		this.setControl(editForm);
		this.setImage(unedited);
	}
	
	public Donor getDonor() {
		return donor;
	}

	public void alterSaveButton() {
		if (this.isDisposed()) return;
		DonorEditForm form = ((DonorEditForm)this.getControl());
		form.setEdited(form.isEdited());
	}
	
	public void save(boolean refresh) {
		DonorEditForm form = ((DonorEditForm)this.getControl());
		if (form.isEdited()) {
			form.saveForm(refresh);
		}
	}
	
}
