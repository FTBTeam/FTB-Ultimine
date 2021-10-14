package dev.ftb.mods.ftbultimine.config;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
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

	IntValue PREVENT_TOOL_BREAK = CONFIG.getInt("prevent_tool_break", 0, 0, 100)
			.comment("This will stop mining if tool reaches X durability. It's possible it won't work with special tool types.");

	BooleanValue USE_TRINKET = CONFIG.getBoolean("use_trinket", false)
			.comment("(This only works if the mod 'Lost Trinkets' is installed!)",
					"Adds a custom 'Ultiminer' trinket players will need to activate to be able to use Ultimine.",
					"Make sure you disable the 'Octopick' trinket if this is enabled!");

	BooleanValue CANCEL_ON_BLOCK_BREAK_FAIL = CONFIG.getBoolean("cancel_on_block_break_fail", false)
			.comment("This is an advanced option, that you better leave alone This will stop ultimining on first block that it can't mine, rather than skipping it.");

	static void load() {
		CONFIG.load(Platform.getConfigFolder().resolve(CONFIG.key + ".snbt"));
	}
}
