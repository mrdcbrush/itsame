package com.itsame;

/** A small curated palette for the banner background, cycled through in the config screen. */
public enum BackgroundPreset {
	BLACK(0x000000),
	DARK_GRAY(0x2B2B2B),
	WHITE(0xFFFFFF),
	RED(0xAA0000),
	GREEN(0x00AA00),
	BLUE(0x0000AA),
	PURPLE(0x5500AA);

	public final int rgb;

	BackgroundPreset(int rgb) {
		this.rgb = rgb;
	}

	public static BackgroundPreset closestTo(int rgb) {
		for (BackgroundPreset preset : values()) {
			if (preset.rgb == rgb) {
				return preset;
			}
		}

		return BLACK;
	}

	public String displayName() {
		String[] parts = name().split("_");
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			if (!sb.isEmpty()) sb.append(' ');
			sb.append(part.charAt(0)).append(part.substring(1).toLowerCase());
		}
		return sb.toString();
	}
}
