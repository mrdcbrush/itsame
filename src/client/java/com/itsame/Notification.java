package com.itsame;

import net.minecraft.world.entity.player.PlayerSkin;

/** A single active join/leave banner. */
public final class Notification {
	public final String playerName;
	public final boolean joined;
	private final long createdAtMillis;
	public volatile PlayerSkin skin;
	/** Current animated pixel offset from the anchor, eased toward its target stack slot each frame. */
	public volatile double animatedOffset;

	public Notification(String playerName, boolean joined, PlayerSkin skin) {
		this.playerName = playerName;
		this.joined = joined;
		this.skin = skin;
		this.createdAtMillis = System.currentTimeMillis();
	}

	/** Real wall-clock elapsed time, not tick count, so fade timing holds up under lag/high TPS. */
	public double elapsedSeconds() {
		return (System.currentTimeMillis() - createdAtMillis) / 1000.0;
	}

	public String label() {
		return playerName + (joined ? " has joined" : " has left");
	}
}
