package com.itsame;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

/** The real in-game HUD layer: draws every currently active join/leave banner, stacked at the configured anchor. */
public final class BannerHudElement implements HudElement {
	private final ItsAMeConfig config;
	private final BannerManager bannerManager;

	public BannerHudElement(ItsAMeConfig config, BannerManager bannerManager) {
		this.config = config;
		this.bannerManager = bannerManager;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		BannerRenderer.renderAll(graphics, Minecraft.getInstance().font, config, bannerManager);
	}
}
