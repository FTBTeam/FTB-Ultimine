package dev.ftb.mods.ftbultimine.config;

import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import me.shedaniel.architectury.platform.Platform;

/**
 * @author LatvianModder
 */
public interface FTBUltimineClientConfig {

	SNBTConfig CONFIG = SNBTConfig.create(FTBUltimine.MOD_ID + "-client");

	IntValue xOffset = CONFIG.getInt("x_offset", -1)
			.comment("Manual x offset of FTB Ultimine overlay, required for some modpacks");

	IntValue renderOutline = CONFIG.getInt("render_outline", 512)
			.comment("Maximum number of blocks the white outline should be rendered for",
					"Keep in mind this may get *very* laggy for large amounts of blocks!");

	static void init() {
		CONFIG.load(Platform.getGameFolder().resolve("local/ftbultimine/client.snbt"));
	}
}
