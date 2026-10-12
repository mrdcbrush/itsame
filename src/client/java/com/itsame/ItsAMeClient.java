package com.itsame;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.Command;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

public final class ItsAMeClient implements ClientModInitializer {
	public static final String MOD_ID = "itsame";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Screen opens are deferred to the next client tick rather than done inline from a command/keybind
	// handler: vanilla closes the chat/current screen right after a command runs, which would otherwise
	// immediately undo an in-place setScreenAndShow() call.
	private static final AtomicBoolean openConfigRequested = new AtomicBoolean(false);

	// Static so the optional ModMenu integration (instantiated independently by ModMenu, not wired
	// through onInitializeClient) can reach the same live config/queue.
	private static ItsAMeConfig config;
	private static BannerManager bannerManager;

	public static ItsAMeConfig config() {
		return config;
	}

	public static BannerManager bannerManager() {
		return bannerManager;
	}

	@Override
	public void onInitializeClient() {
		config = ItsAMeConfig.load();
		bannerManager = new BannerManager(config);

		JoinLeaveListener.register(bannerManager);

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "banner"), new BannerHudElement(config, bannerManager));

		KeyMapping openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.itsame.open_config",
				InputConstants.Type.KEYBOARD,
				InputConstants.UNKNOWN.getValue(),
				KeyMapping.Category.MISC));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfigKey.consumeClick()) {
				LOGGER.info("Keybind click consumed (bound key: {}), requesting config screen", openConfigKey.getTranslatedKeyMessage().getString());
				openConfigRequested.set(true);
			}

			if (openConfigRequested.compareAndSet(true, false)) {
				LOGGER.info("Opening config screen");
				client.setScreenAndShow(new ItsAMeConfigScreen(null, config, bannerManager));
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> dispatcher.register(
				ClientCommands.literal("itsame").executes(context -> {
					context.getSource().sendFeedback(Component.literal("[It's a Me] Opening settings..."));
					openConfigRequested.set(true);
					return Command.SINGLE_SUCCESS;
				})));

		LOGGER.info("It's a Me initialized");
	}
}
