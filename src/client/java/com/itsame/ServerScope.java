package com.itsame;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

/** Identifies which "server" the player is currently connected to, for scoping the whitelist per-server. */
public final class ServerScope {
	public static final String SINGLEPLAYER = "Singleplayer";
	public static final String NONE = "Not connected";

	private ServerScope() {
	}

	/** A stable key for the current connection: the server address, "Singleplayer", or "Not connected". */
	public static String current() {
		Minecraft client = Minecraft.getInstance();

		if (client.hasSingleplayerServer()) {
			return SINGLEPLAYER;
		}

		ServerData server = client.getCurrentServer();
		if (server != null && server.ip != null && !server.ip.isBlank()) {
			return server.ip;
		}

		return NONE;
	}
}
