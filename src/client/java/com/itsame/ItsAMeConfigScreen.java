package com.itsame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Lets the player position and time the banner, with a live preview matching the real in-game rendering. */
public final class ItsAMeConfigScreen extends Screen {
	private static final int ROW_HEIGHT = 22;

	private final Screen parent;
	private final ItsAMeConfig config;
	private final BannerManager bannerManager;
	private final List<Row> rows = new ArrayList<>();

	private final ItsAMeConfig snapshot;
	private boolean nextTestIsJoin = true;

	public ItsAMeConfigScreen(Screen parent, ItsAMeConfig config, BannerManager bannerManager) {
		super(Component.literal("It's a Me"));
		this.parent = parent;
		this.config = config;
		this.bannerManager = bannerManager;
		this.snapshot = config.copy();
	}

	@Override
	protected void init() {
		rows.clear();
		int centerX = this.width / 2;
		int y = this.height / 2 - 160;

		this.addRenderableWidget(CycleButton.builder((Anchor a) -> Component.literal(a.displayName()), config.anchor)
				.withValues(Anchor.values())
				.create(centerX - 100, y, 200, 20, Component.literal("Position"), (button, value) -> config.anchor = value));
		y += ROW_HEIGHT;

		y = addStepper(centerX, y, "Offset X", () -> config.offsetX + "px",
				delta -> config.offsetX = Math.max(0, config.offsetX + delta * 4));
		y = addStepper(centerX, y, "Offset Y", () -> config.offsetY + "px",
				delta -> config.offsetY = Math.max(0, config.offsetY + delta * 4));
		y = addStepper(centerX, y, "Size", () -> Math.round(config.scale * 100) + "%",
				delta -> config.scale = clamp(round2(config.scale + delta * 0.01), ItsAMeConfig.MIN_SCALE, ItsAMeConfig.MAX_SCALE));
		y = addStepper(centerX, y, "Display seconds", () -> String.format("%.1fs", config.displaySeconds),
				delta -> config.displaySeconds = Math.max(0.0, round1(config.displaySeconds + delta * 0.5)));
		y = addStepper(centerX, y, "Fade in", () -> String.format("%.1fs", config.fadeInSeconds),
				delta -> config.fadeInSeconds = Math.max(0.0, round1(config.fadeInSeconds + delta * 0.1)));
		y = addStepper(centerX, y, "Fade out", () -> String.format("%.1fs", config.fadeOutSeconds),
				delta -> config.fadeOutSeconds = Math.max(0.0, round1(config.fadeOutSeconds + delta * 0.1)));

		this.addRenderableWidget(CycleButton.builder((BackgroundPreset p) -> Component.literal(p.displayName()), BackgroundPreset.closestTo(config.backgroundColor))
				.withValues(BackgroundPreset.values())
				.create(centerX - 100, y, 200, 20, Component.literal("Background"), (button, value) -> config.backgroundColor = value.rgb));
		y += ROW_HEIGHT;

		y = addStepper(centerX, y, "Background opacity", () -> config.backgroundOpacityPercent + "%",
				delta -> config.backgroundOpacityPercent = (int) clamp(config.backgroundOpacityPercent + delta * 10, 0, 100));

		this.addRenderableWidget(CycleButton.onOffBuilder(config.showAvatar)
				.create(centerX - 100, y, 200, 20, Component.literal("Show avatar"), (button, value) -> config.showAvatar = value));
		y += ROW_HEIGHT;

		this.addRenderableWidget(Button.builder(Component.literal("Manage Whitelist (" + config.currentWhitelist().size() + ")"), button ->
				this.minecraft.setScreenAndShow(new WhitelistScreen(this, config)))
				.bounds(centerX - 100, y, 200, 20).build());
		y += ROW_HEIGHT;

		this.addRenderableWidget(Button.builder(Component.literal("Test"), button -> {
			boolean joined = nextTestIsJoin;
			nextTestIsJoin = !nextTestIsJoin;
			bannerManager.addTest(testPlayerName(), joined);
		}).bounds(centerX - 100, y, 200, 20).build());
		y += ROW_HEIGHT + 12;

		this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> {
			config.save();
			this.minecraft.setScreenAndShow(parent);
		}).bounds(centerX - 100, y, 95, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> {
			config.copyFrom(snapshot);
			this.minecraft.setScreenAndShow(parent);
		}).bounds(centerX + 5, y, 95, 20).build());
	}

	private int addStepper(int centerX, int y, String label, Supplier<String> valueText, IntConsumer onDelta) {
		this.addRenderableWidget(Button.builder(Component.literal("-"), button -> onDelta.accept(-1))
				.bounds(centerX - 100, y, 20, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), button -> onDelta.accept(1))
				.bounds(centerX + 80, y, 20, 20).build());

		rows.add(new Row(label, valueText, centerX, y));
		return y + ROW_HEIGHT;
	}

	private static double round1(double value) {
		return Math.round(value * 10.0) / 10.0;
	}

	private static double round2(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	private static double clamp(double value, double min, double max) {
		return Math.min(max, Math.max(min, value));
	}

	private static String testPlayerName() {
		var player = Minecraft.getInstance().player;
		return player != null ? player.getGameProfile().name() : "Steve";
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		for (Row row : rows) {
			String value = row.valueText().get();
			String text = value.isEmpty() ? row.label() : row.label() + ": " + value;
			int textWidth = this.font.width(text);
			graphics.text(this.font, text, row.centerX() - textWidth / 2, row.y() + 6, 0xFFFFFFFF);
		}

		// Draws whatever's actually queued (e.g. from the Test button) with the exact same routine and
		// stacking/animation the real HUD uses; falls back to a static sample when nothing's queued yet
		// (e.g. opened from ModMenu's main-menu mod list, where nothing has ever joined/left).
		BannerRenderer.renderAllOrSample(graphics, this.font, config, bannerManager, "Steve has joined");
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(parent);
	}

	private record Row(String label, Supplier<String> valueText, int centerX, int y) {
	}
}
