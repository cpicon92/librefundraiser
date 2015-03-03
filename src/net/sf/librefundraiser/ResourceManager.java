package net.sf.librefundraiser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ResourceManager {
	private static final Map<String, Image> icons = new ConcurrentHashMap<>();
	private static final Map<Integer, Color> colors = new ConcurrentHashMap<>();
	
	public static Image getIcon(String filename) {
		Display display = Display.getCurrent();
		Image icon = icons.get(filename);
		if (icon == null) {
			icon = new Image(display, ResourceManager.class.getResourceAsStream("/net/sf/librefundraiser/icons/"+filename));
			icons.put(filename, icon);
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
	
	public static Color getColor(int argb, boolean alpha) {
		if (!alpha) {
			argb |= 0xff000000;
		}
		Color c = colors.get(argb);
		if (c == null) {
			c = new Color(Display.getCurrent(), argb & 0xff, (argb >> 8) & 0xff, (argb >> 16) & 0xff);
		}
		return c;
	}
	
	public static Color getColor(int rgb) {
		return getColor(rgb, false);
	}
}
