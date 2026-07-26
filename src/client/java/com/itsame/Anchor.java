package com.itsame;

/**
 * The nine screen positions the banner can be anchored to. Coordinates are resolved against
 * {@code guiWidth}/{@code guiHeight}, which Minecraft already derives from the physical screen
 * resolution and the user's GUI Scale setting - so anchoring here is resolution/DPI-correct
 * without any extra detection code.
 */
public enum Anchor {
	// Declared in clockwise order (starting top-right) so cycling through them via values() feels like
	// walking around the screen edge instead of jumping row by row, with CENTER last.
	TOP_RIGHT,
	CENTER_RIGHT,
	BOTTOM_RIGHT,
	BOTTOM_CENTER,
	BOTTOM_LEFT,
	CENTER_LEFT,
	TOP_LEFT,
	TOP_CENTER,
	CENTER;

	public String displayName() {
		String[] parts = name().split("_");
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			if (!sb.isEmpty()) sb.append(' ');
			sb.append(part.charAt(0)).append(part.substring(1).toLowerCase());
		}
		return sb.toString();
	}

	public Anchor next() {
		Anchor[] values = values();
		return values[(ordinal() + 1) % values.length];
	}

	/**
	 * Resolves the top-left draw origin for a box of the given size, at this anchor, with the
	 * given pixel offset applied inward from the anchor's edge(s).
	 */
	public int originX(int guiWidth, int boxWidth, int offsetX) {
		return switch (this) {
			case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> offsetX;
			case TOP_CENTER, CENTER, BOTTOM_CENTER -> (guiWidth - boxWidth) / 2 + offsetX;
			case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> guiWidth - boxWidth - offsetX;
		};
	}

	public int originY(int guiHeight, int boxHeight, int offsetY) {
		return switch (this) {
			case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> offsetY;
			case CENTER_LEFT, CENTER, CENTER_RIGHT -> (guiHeight - boxHeight) / 2 + offsetY;
			case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> guiHeight - boxHeight - offsetY;
		};
	}

	/** Whether notifications stacked at this anchor should grow downward (true) or upward (false). */
	public boolean stacksDownward() {
		return this == TOP_LEFT || this == TOP_CENTER || this == TOP_RIGHT || this == CENTER_LEFT || this == CENTER || this == CENTER_RIGHT;
	}
}
