package net.sf.librefundraiser.gui;

import net.sf.librefundraiser.io.Donor;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class DonorListFilter extends ViewerFilter {
	String filter;
	@Override
	public boolean select(Viewer tableViewer, Object parent, Object e) {
		if (filter == null || filter.isEmpty()) return true;
		Donor donor = (Donor) e;
		return donor.match(this.filter);
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
}
