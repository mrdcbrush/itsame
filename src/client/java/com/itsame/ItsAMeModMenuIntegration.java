package com.itsame;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Optional integration: only loaded/used if ModMenu is present (registered via the "modmenu"
 * entrypoint in fabric.mod.json), giving a keybind-free way to reach settings via Mods -> It's a Me -> gear icon.
 */
public final class ItsAMeModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> new ItsAMeConfigScreen(parent, ItsAMeClient.config(), ItsAMeClient.bannerManager());
	}
}
