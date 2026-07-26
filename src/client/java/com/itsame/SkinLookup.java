package com.itsame;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.PlayerSkin;

/** Resolves a connected player's skin from the client's current tab-list/player-info data. */
public final class SkinLookup {
	private SkinLookup() {
	}

	public static PlayerSkin resolve(String playerName) {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null) {
			return null;
		}

		PlayerInfo info = connection.getPlayerInfo(playerName);
		return info != null ? info.getSkin() : null;
	}
}
