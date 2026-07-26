package com.itsame;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

/** Detects vanilla "X joined/left the game" system messages and turns them into banner notifications. */
public final class JoinLeaveListener {
	// Locale-independent fallback in case the translation key check below ever needs adjusting.
	private static final Pattern JOINED_PATTERN = Pattern.compile("^(.+?) joined the game$");
	private static final Pattern LEFT_PATTERN = Pattern.compile("^(.+?) left the game$");

	private JoinLeaveListener() {
	}

	public static void register(BannerManager bannerManager) {
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (overlay) {
				return;
			}

			ParsedEvent event = parse(message);
			if (event != null) {
				bannerManager.add(event.playerName, event.joined);
			}
		});
	}

	private static ParsedEvent parse(Component message) {
		if (message.getContents() instanceof TranslatableContents translatable) {
			String key = translatable.getKey();
			boolean joined;

			if (key.equals("multiplayer.player.joined") || key.equals("multiplayer.player.joined.renamed")) {
				joined = true;
			} else if (key.equals("multiplayer.player.left")) {
				joined = false;
			} else {
				return null;
			}

			Object[] args = translatable.getArgs();
			if (args.length == 0) {
				return null;
			}

			Object first = args[0];
			String name = first instanceof Component component ? component.getString() : String.valueOf(first);
			return new ParsedEvent(name, joined);
		}

		String text = message.getString();
		Matcher joinedMatcher = JOINED_PATTERN.matcher(text);
		if (joinedMatcher.matches()) {
			return new ParsedEvent(joinedMatcher.group(1), true);
		}

		Matcher leftMatcher = LEFT_PATTERN.matcher(text);
		if (leftMatcher.matches()) {
			return new ParsedEvent(leftMatcher.group(1), false);
		}

		return null;
	}

	private record ParsedEvent(String playerName, boolean joined) {
	}
}
