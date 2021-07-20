package dev.ftb.mods.ftbultimine.config;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import me.shedaniel.architectury.platform.Platform;

/**
 * @author LatvianModder
 */
public interface FTBUltimineCommonConfig {

	SNBTConfig CONFIG = SNBTConfig.create(FTBUltimine.MOD_ID)
			.comment("Configuration for FTB Ultimine that is common to both Clients and Servers",
					"This file is used to control instance (e.g. modpack) specific things like mod integrations.",
					"Changes to this file require you to restart the game!");

	static void load() {
		CONFIG.load(Platform.getConfigFolder().resolve(CONFIG.key + ".snbt"));
	}
}
