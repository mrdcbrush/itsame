package com.itsame;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/** Persisted user settings, stored as JSON at config/itsame.json. */
public final class ItsAMeConfig {
	public static final double MIN_SCALE = 0.05;
	public static final double MAX_SCALE = 0.20;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("itsame.json");

	public Anchor anchor = Anchor.TOP_RIGHT;
	public int offsetX = 8;
	public int offsetY = 8;
	public double displaySeconds = 5.0;
	public double fadeInSeconds = 0.5;
	public double fadeOutSeconds = 0.5;
	public boolean showAvatar = true;
	/** Banner height as a fraction of the screen height (clamped to [MIN_SCALE, MAX_SCALE]). */
	public double scale = 0.10;
	public int backgroundColor = 0x000000;
	/** 0 = fully transparent background, 100 = fully opaque (before per-notification fade is applied). */
	public int backgroundOpacityPercent = 60;
	/**
	 * Player names to show banners for, keyed by server (see {@link ServerScope#current()}). An empty or
	 * missing list for the current server means everyone on that server. Matched case-insensitively.
	 */
	public Map<String, List<String>> whitelistsByServer = new LinkedHashMap<>();

	public static ItsAMeConfig load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				ItsAMeConfig loaded = GSON.fromJson(reader, ItsAMeConfig.class);
				if (loaded != null) {
					return loaded;
				}
			} catch (IOException | RuntimeException e) {
				ItsAMeClient.LOGGER.warn("Failed to read itsame.json, using defaults", e);
			}
		}

		ItsAMeConfig defaults = new ItsAMeConfig();
		defaults.save();
		return defaults;
	}

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());

			try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			ItsAMeClient.LOGGER.warn("Failed to save itsame.json", e);
		}
	}

	public double totalSeconds() {
		return fadeInSeconds + displaySeconds + fadeOutSeconds;
	}

	public boolean isAllowed(String playerName) {
		List<String> whitelist = whitelistsByServer.get(ServerScope.current());
		if (whitelist == null || whitelist.isEmpty()) {
			return true;
		}

		for (String name : whitelist) {
			if (name.equalsIgnoreCase(playerName)) {
				return true;
			}
		}

		return false;
	}

	/** The mutable whitelist for whichever server the player is currently connected to. */
	public List<String> currentWhitelist() {
		return whitelistsByServer.computeIfAbsent(ServerScope.current(), key -> new ArrayList<>());
	}

	/** A snapshot of the current values, independent of this instance, used for the config screen's Cancel button. */
	public ItsAMeConfig copy() {
		ItsAMeConfig copy = new ItsAMeConfig();
		copy.copyFrom(this);
		return copy;
	}

	public void copyFrom(ItsAMeConfig other) {
		this.anchor = other.anchor;
		this.offsetX = other.offsetX;
		this.offsetY = other.offsetY;
		this.displaySeconds = other.displaySeconds;
		this.fadeInSeconds = other.fadeInSeconds;
		this.fadeOutSeconds = other.fadeOutSeconds;
		this.showAvatar = other.showAvatar;
		this.scale = other.scale;
		this.backgroundColor = other.backgroundColor;
		this.backgroundOpacityPercent = other.backgroundOpacityPercent;

		this.whitelistsByServer = new LinkedHashMap<>();
		for (Map.Entry<String, List<String>> entry : other.whitelistsByServer.entrySet()) {
			this.whitelistsByServer.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
	}
}
