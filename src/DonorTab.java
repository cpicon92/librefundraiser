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
		String lastname = donor.getData("lastname");
		String firstname = donor.getData("firstname");
		String tabTitle = lastname+(!(lastname.equals("")||firstname.equals(""))?", ":"")+firstname;
		if (tabTitle.equals("")) tabTitle = donor.getData("account");
		this.setText(tabTitle);
		EditForm editForm = new EditForm(this.getParent(),SWT.NONE,this.donor);
		this.setControl(editForm);
	}
	
	public Donor getDonor() {
		return donor;
	}
	
}
