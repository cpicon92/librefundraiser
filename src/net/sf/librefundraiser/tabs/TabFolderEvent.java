package net.sf.librefundraiser.tabs;

public class TabFolderEvent {
	public boolean doit = true;
	public final TabItem item;
	public TabFolderEvent(TabItem item) {
		this.item = item;
	}
}
