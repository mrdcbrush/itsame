package com.itsame;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** A simple clickable list of player names, reused for both the current whitelist and the online-player search results. */
public final class NameListWidget extends ObjectSelectionList<NameListWidget.NameEntry> {
	private final String actionHint;

	public NameListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight, String actionHint) {
		super(minecraft, width, height, y, itemHeight);
		this.actionHint = actionHint;
	}

	public void setNames(List<String> names, Consumer<String> onClick) {
		this.replaceEntries(names.stream().map(name -> new NameEntry(name, onClick)).toList());
	}

	@Override
	public int getRowWidth() {
		return Math.max(50, this.getWidth() - 14);
	}

	public final class NameEntry extends ObjectSelectionList.Entry<NameEntry> {
		private final String name;
		private final Consumer<String> onClick;

		public NameEntry(String name, Consumer<String> onClick) {
			this.name = name;
			this.onClick = onClick;
		}

		@Override
		public Component getNarration() {
			return Component.literal(name);
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
			var font = Minecraft.getInstance().font;
			int textY = getContentY() + (getContentHeight() - font.lineHeight) / 2;
			graphics.text(font, name, getContentX(), textY, hovered ? 0xFFFFFF55 : 0xFFFFFFFF);

			if (hovered) {
				int hintWidth = font.width(actionHint);
				graphics.text(font, actionHint, getContentRight() - hintWidth, textY, 0xFFAAAAAA);
			}
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			onClick.accept(name);
			return true;
		}
	}
}
