package net.sf.librefundraiser;

import java.util.HashMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ResourceManager {
	public static HashMap<String, Image> icons = new HashMap<String, Image>();
	
	public static Image getIcon(String filename) {
		Display display = Display.getCurrent();
		Image icon = icons.get(filename);
		if (icon == null) {
			icon = new Image(display, ResourceManager.class.getResourceAsStream("/net/sf/librefundraiser/icons/"+filename));
		}
		return icon;
	}
	
	public static Image[] getLogo() {
		Display display = Display.getCurrent();
		return new Image[]{
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon16.png")),
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon24.png")),
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon32.png")),
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon48.png")),
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon64.png")),
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon128.png")),
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon256.png"))
				};
	}
	
	public static Image getLogo(int size) {
		int[] sizes = {16,24,32,48,64,128,256};
		for (int i = 0; i < sizes.length; i++) {
			if (sizes[i] == size) return getLogo()[i];
		}
		return null;
	}
}
