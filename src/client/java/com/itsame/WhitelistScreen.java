package com.itsame;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

/**
 * Lets the player view/add/remove whitelisted names, either by typing them or by picking from a
 * live, searchable list of players currently visible on the server.
 */
public final class WhitelistScreen extends Screen {
	private static final int LIST_ITEM_HEIGHT = 16;

	private final Screen parent;
	private final ItsAMeConfig config;

	private EditBox searchBox;
	private EditBox manualAddBox;
	private NameListWidget whitelistList;
	private NameListWidget onlinePlayersList;
	private List<String> lastOnlineNames = List.of();

	private int leftX;
	private int rightX;

	private final String serverScope;

	public WhitelistScreen(Screen parent, ItsAMeConfig config) {
		super(Component.literal("It's a Me - Whitelist"));
		this.parent = parent;
		this.config = config;
		this.serverScope = ServerScope.current();
	}

	@Override
	protected void init() {
		int columnWidth = Math.min(220, (this.width - 40) / 2);
		leftX = this.width / 2 - columnWidth - 10;
		rightX = this.width / 2 + 10;
		int listY = 70;
		int listHeight = this.height - listY - 60;

		whitelistList = new NameListWidget(this.minecraft, columnWidth, listHeight, listY, LIST_ITEM_HEIGHT, "click to remove");
		whitelistList.setX(leftX);
		this.addRenderableWidget(whitelistList);
		refreshWhitelistList();

		manualAddBox = new EditBox(this.font, leftX, listY + listHeight + 8, columnWidth, 20, Component.literal("Add name"));
		manualAddBox.setMaxLength(32);
		this.addRenderableWidget(manualAddBox);

		this.addRenderableWidget(Button.builder(Component.literal("Add"), button -> {
			String name = manualAddBox.getValue().trim();
			if (!name.isEmpty()) {
				addToWhitelist(name);
				manualAddBox.setValue("");
			}
		}).bounds(leftX, listY + listHeight + 32, columnWidth, 20).build());

		searchBox = new EditBox(this.font, rightX, 45, columnWidth, 20, Component.literal("Search"));
		searchBox.setMaxLength(48);
		searchBox.setResponder(text -> applyOnlinePlayersFilter(lastOnlineNames));
		this.addRenderableWidget(searchBox);

		onlinePlayersList = new NameListWidget(this.minecraft, columnWidth, listHeight, listY, LIST_ITEM_HEIGHT, "click to add");
		onlinePlayersList.setX(rightX);
		this.addRenderableWidget(onlinePlayersList);
		refreshOnlinePlayers();

		this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.minecraft.setScreenAndShow(parent))
				.bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		// Keep the online-player list live as people join/leave while this screen is open.
		if (!onlinePlayerNames().equals(lastOnlineNames)) {
			refreshOnlinePlayers();
		}

		String scopeText = "Server: " + serverScope;
		graphics.text(this.font, scopeText, this.width / 2 - this.font.width(scopeText) / 2, 20, 0xFFFFFF55);

		graphics.text(this.font, "Whitelist (" + config.currentWhitelist().size() + ") - empty means everyone", leftX, 55, 0xFFFFFFFF);
		graphics.text(this.font, "Online players", rightX, 35, 0xFFFFFFFF);
	}

	private void addToWhitelist(String name) {
		if (!containsIgnoreCase(config.currentWhitelist(), name)) {
			config.currentWhitelist().add(name);
		}
		refreshWhitelistList();
	}

	private void removeFromWhitelist(String name) {
		config.currentWhitelist().removeIf(existing -> existing.equalsIgnoreCase(name));
		refreshWhitelistList();
	}

	private void refreshWhitelistList() {
		List<String> sorted = new ArrayList<>(config.currentWhitelist());
		sorted.sort(String.CASE_INSENSITIVE_ORDER);
		whitelistList.setNames(sorted, this::removeFromWhitelist);
	}

	private void refreshOnlinePlayers() {
		lastOnlineNames = onlinePlayerNames();
		applyOnlinePlayersFilter(lastOnlineNames);
	}

	private void applyOnlinePlayersFilter(List<String> names) {
		String filter = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
		List<String> filtered = names.stream()
				.filter(name -> filter.isEmpty() || name.toLowerCase(Locale.ROOT).contains(filter))
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.toList();
		onlinePlayersList.setNames(filtered, this::addToWhitelist);
	}

	private static List<String> onlinePlayerNames() {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null) {
			return List.of();
		}

		List<String> names = new ArrayList<>();
		for (PlayerInfo info : connection.getOnlinePlayers()) {
			names.add(info.getProfile().name());
		}
		return names;
	}

	private static boolean containsIgnoreCase(List<String> list, String value) {
		for (String s : list) {
			if (s.equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(parent);
	}
}
