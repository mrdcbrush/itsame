package com.itsame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Holds the queue of currently-visible join/leave banners. */
public final class BannerManager {
	/** How long to keep retrying a skin lookup after a notification is created (covers the race where the
	 *  join message arrives slightly before the tab-list entry does). */
	private static final double SKIN_RETRY_WINDOW_SECONDS = 2.0;
	/** How quickly a bumped notification eases toward its new stack slot; higher = snappier. */
	private static final double ANIMATION_RATE_PER_SECOND = 10.0;

	private final ItsAMeConfig config;
	private final List<Notification> active = new ArrayList<>();
	private long lastUpdateNanos = System.nanoTime();

	public BannerManager(ItsAMeConfig config) {
		this.config = config;
	}

	public void add(String playerName, boolean joined) {
		if (!config.isAllowed(playerName)) {
			return;
		}

		active.add(new Notification(playerName, joined, SkinLookup.resolve(playerName)));
	}

	/** Like {@link #add}, but bypasses the whitelist - used by the settings screen's Test button. */
	public void addTest(String playerName, boolean joined) {
		active.add(new Notification(playerName, joined, SkinLookup.resolve(playerName)));
	}

	/**
	 * Prunes expired notifications, lazily re-resolves skins, and animates each notification's stack
	 * offset toward its target slot - the newest notification sits at the configured anchor (offset 0)
	 * and older ones are smoothly bumped away by {@code rowHeight} per slot as new ones arrive.
	 *
	 * @return the live notifications, newest first
	 */
	public List<Notification> activeNotifications(double rowHeight) {
		long now = System.nanoTime();
		double deltaSeconds = Math.min(0.25, Math.max(0.0, (now - lastUpdateNanos) / 1_000_000_000.0));
		lastUpdateNanos = now;

		active.removeIf(n -> n.elapsedSeconds() >= config.totalSeconds());

		for (Notification notification : active) {
			if (notification.skin == null && notification.elapsedSeconds() < SKIN_RETRY_WINDOW_SECONDS) {
				notification.skin = SkinLookup.resolve(notification.playerName);
			}
		}

		double smoothing = 1.0 - Math.exp(-ANIMATION_RATE_PER_SECOND * deltaSeconds);

		// active is stored oldest-first (append order); the newest entry is last and belongs at slot 0.
		for (int slot = 0; slot < active.size(); slot++) {
			Notification notification = active.get(active.size() - 1 - slot);
			double target = slot * rowHeight;
			notification.animatedOffset += (target - notification.animatedOffset) * smoothing;
		}

		List<Notification> newestFirst = new ArrayList<>(active);
		Collections.reverse(newestFirst);
		return newestFirst;
	}
}
