package com.itsame;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

/**
 * Draws a single banner box. Shared by the real HUD element and the config screen's live preview
 * so what you configure is exactly what you'll see in-game.
 */
public final class BannerRenderer {
	private static final Identifier FALLBACK_SKIN = Identifier.withDefaultNamespace("textures/entity/player/wide/steve.png");

	// "Natural" (unscaled) layout constants; the whole box is drawn at this size and then scaled up/down
	// via the pose stack to hit the configured on-screen size, so none of this math needs to change per-scale.
	private static final int AVATAR_SIZE = 16;
	private static final int PADDING = 4;
	private static final int GAP = 4;

	private BannerRenderer() {
	}

	private static int naturalContentHeight(Font font) {
		return Math.max(AVATAR_SIZE, font.lineHeight);
	}

	private static int naturalBoxHeight(Font font) {
		return PADDING * 2 + naturalContentHeight(font);
	}

	/** Scale factor mapping natural (unscaled) pixel sizes to real screen pixels, from config.scale. */
	public static float scaleFactor(ItsAMeConfig config, Font font, int guiHeight) {
		double clampedScale = Math.min(ItsAMeConfig.MAX_SCALE, Math.max(ItsAMeConfig.MIN_SCALE, config.scale));
		double targetBoxHeight = guiHeight * clampedScale;
		return (float) (targetBoxHeight / naturalBoxHeight(font));
	}

	/** Real-screen-pixel distance between stacked banners, including the gap. */
	public static double rowHeight(ItsAMeConfig config, Font font, int guiHeight) {
		return (naturalBoxHeight(font) + GAP) * scaleFactor(config, font, guiHeight);
	}

	/**
	 * Renders every currently active notification, stacked and animated exactly as the real HUD does.
	 * Used by both the actual in-game HUD layer and the settings screen's Test button, so a test
	 * notification looks identical whether or not the settings screen happens to be open.
	 */
	public static void renderAll(GuiGraphicsExtractor graphics, Font font, ItsAMeConfig config, BannerManager bannerManager) {
		double rowHeight = rowHeight(config, font, graphics.guiHeight());

		for (Notification notification : bannerManager.activeNotifications(rowHeight)) {
			double alpha = computeAlpha(config, notification.elapsedSeconds());
			render(graphics, font, config, notification.label(), notification.skin, alpha, notification.animatedOffset);
		}
	}

	/**
	 * Like {@link #renderAll}, but when nothing is actually queued (e.g. opening settings from ModMenu's
	 * main-menu mod list, where nothing has ever joined/left, or before Test has been pressed) it falls
	 * back to a static full-opacity sample so there's always something to see and tune.
	 */
	public static void renderAllOrSample(GuiGraphicsExtractor graphics, Font font, ItsAMeConfig config, BannerManager bannerManager, String sampleLabel) {
		double rowHeight = rowHeight(config, font, graphics.guiHeight());
		var notifications = bannerManager.activeNotifications(rowHeight);

		if (notifications.isEmpty()) {
			render(graphics, font, config, sampleLabel, null, 1.0, 0);
			return;
		}

		for (Notification notification : notifications) {
			double alpha = computeAlpha(config, notification.elapsedSeconds());
			render(graphics, font, config, notification.label(), notification.skin, alpha, notification.animatedOffset);
		}
	}

	/**
	 * Renders one notification box.
	 *
	 * @param stackOffsetPx how far, in real screen pixels, this box sits from the anchor edge
	 */
	public static void render(GuiGraphicsExtractor graphics, Font font, ItsAMeConfig config, String label, PlayerSkin skin, double alpha, double stackOffsetPx) {
		if (alpha <= 0.0) {
			return;
		}

		float scale = scaleFactor(config, font, graphics.guiHeight());
		int textWidth = font.width(label);
		int contentHeight = naturalContentHeight(font);
		int boxWidth = PADDING * 2 + (config.showAvatar ? AVATAR_SIZE + PADDING : 0) + textWidth;
		int boxHeight = naturalBoxHeight(font);

		int scaledBoxWidth = Math.round(boxWidth * scale);
		int scaledBoxHeight = Math.round(boxHeight * scale);

		int originX = config.anchor.originX(graphics.guiWidth(), scaledBoxWidth, config.offsetX);
		int baseY = config.anchor.originY(graphics.guiHeight(), scaledBoxHeight, config.offsetY);
		int roundedOffset = (int) Math.round(stackOffsetPx);
		int originY = config.anchor.stacksDownward() ? baseY + roundedOffset : baseY - roundedOffset - scaledBoxHeight;

		int alphaByte = clampByte(alpha * 255.0);
		int bgAlphaByte = clampByte(alpha * (config.backgroundOpacityPercent / 100.0) * 255.0);
		int backgroundColor = (bgAlphaByte << 24) | (config.backgroundColor & 0xFFFFFF);
		int textColor = (alphaByte << 24) | 0xFFFFFF;
		int tint = (alphaByte << 24) | 0xFFFFFF;

		graphics.pose().pushMatrix();
		graphics.pose().translate(originX, originY);
		graphics.pose().scale(scale);

		graphics.fill(0, 0, boxWidth, boxHeight, backgroundColor);

		int contentX = PADDING;
		int contentY = PADDING;

		if (config.showAvatar) {
			drawAvatar(graphics, skin, contentX, contentY, AVATAR_SIZE, tint);
			contentX += AVATAR_SIZE + PADDING;
		}

		int textY = contentY + (contentHeight - font.lineHeight) / 2;
		graphics.text(font, label, contentX, textY, textColor);

		graphics.pose().popMatrix();
	}

	private static void drawAvatar(GuiGraphicsExtractor graphics, PlayerSkin skin, int x, int y, int size, int tint) {
		// extractRenderState(graphics, Identifier, x, y, size, hat, flip, color) - hat/flip in that order;
		// getting this backwards is what previously rendered the fallback avatar upside down.
		if (skin != null) {
			PlayerFaceExtractor.extractRenderState(graphics, skin, x, y, size, tint);
		} else {
			PlayerFaceExtractor.extractRenderState(graphics, FALLBACK_SKIN, x, y, size, true, false, tint);
		}
	}

	private static int clampByte(double value) {
		long rounded = Math.round(value);
		if (rounded < 0) return 0;
		if (rounded > 255) return 255;
		return (int) rounded;
	}

	public static double computeAlpha(ItsAMeConfig config, double elapsedSeconds) {
		double fadeIn = config.fadeInSeconds;
		double display = config.displaySeconds;
		double fadeOut = config.fadeOutSeconds;

		if (elapsedSeconds < fadeIn) {
			return fadeIn <= 0 ? 1.0 : elapsedSeconds / fadeIn;
		}

		if (elapsedSeconds < fadeIn + display) {
			return 1.0;
		}

		double intoFadeOut = elapsedSeconds - fadeIn - display;
		if (intoFadeOut < fadeOut) {
			return fadeOut <= 0 ? 0.0 : 1.0 - (intoFadeOut / fadeOut);
		}

		return 0.0;
	}
}
