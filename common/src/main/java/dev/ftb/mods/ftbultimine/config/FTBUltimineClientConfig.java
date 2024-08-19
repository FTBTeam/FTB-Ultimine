package dev.ftb.mods.ftbultimine.config;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.EnumValue;
import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.util.PanelPositioning;

import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.LOCAL_DIR;
import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.loadDefaulted;
import static dev.ftb.mods.ftbultimine.FTBUltimine.MOD_ID;

public interface FTBUltimineClientConfig {
	SNBTConfig CONFIG = SNBTConfig.create(MOD_ID + "-client")
			.comment("Client-specific configuration for FTB Ultimine",
					"This file is meant for users to control Ultimine's clientside behaviour and rendering.",
					"Changes to this file require you to reload the world");

	IntValue SHAPE_MENU_CONTEXT_LINES = CONFIG.addInt("shape_menu_context_lines", 2)
			.range(1, 5)
			.comment("When displaying the shape selection menu by holding the Ultimine key",
					"and sneaking at the same time, the number of shape names to display",
					"above and below the selected shape");
	BooleanValue REQUIRE_SNEAK_FOR_MENU = CONFIG.addBoolean("require_sneak_for_menu", true)
			.comment("When holding the Ultimine key, must the player also be sneaking to show the shapes menu?");

	IntValue RENDER_OUTLINE = CONFIG.addInt("render_outline", 256)
			.range(0, Integer.MAX_VALUE)
			.comment("Maximum number of blocks the white outline should be rendered for",
					"Keep in mind this may get *very* laggy for large amounts of blocks!");

	EnumValue<PanelPositioning> OVERLAY_POS = CONFIG.addEnum("overlay_pos", PanelPositioning.NAME_MAP, PanelPositioning.TOP_LEFT);
	IntValue OVERLAY_INSET_X = CONFIG.addInt("overlay_inset_x", 2);
	IntValue OVERLAY_INSET_Y = CONFIG.addInt("overlay_inset_y", 2);

	static void load() {
		loadDefaulted(CONFIG, LOCAL_DIR, MOD_ID);
	}

	static ConfigGroup getConfigGroup() {
		ConfigGroup group = new ConfigGroup(MOD_ID + ".client_settings", accepted -> {
			if (accepted) {
				CONFIG.save(Platform.getGameFolder().resolve("local/" + MOD_ID + "-client.snbt"));
			}
		});
		CONFIG.createClientConfig(group);

		return group;
	}

}
