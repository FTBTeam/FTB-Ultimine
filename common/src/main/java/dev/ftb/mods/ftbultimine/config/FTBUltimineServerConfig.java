package dev.ftb.mods.ftbultimine.config;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.DoubleValue;
import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import me.shedaniel.architectury.hooks.LevelResourceHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

/**
 * @author LatvianModder
 */
public interface FTBUltimineServerConfig {

	SNBTConfig CONFIG = SNBTConfig.create(FTBUltimine.MOD_ID + "-server");
	LevelResource CONFIG_FILE_PATH = LevelResourceHooks.create("serverconfig/" + CONFIG.key + ".snbt");

	IntValue maxBlocks = CONFIG.getInt("max_blocks", 64)
			.range(32768)
			.comment("Max amount of blocks that can be ultimined at once");

	DoubleValue exhaustionPerBlock = CONFIG.getDouble("exhaustion_per_block", 20)
			.range(10000)
			.comment("Hunger multiplied for each block mined with ultimine");

	// TODO: More merging
	BooleanValue mergeStone = CONFIG.getBoolean("merge_stone", true);

	static void load(MinecraftServer server) {
		CONFIG.load(server.getWorldPath(CONFIG_FILE_PATH));

		if (maxBlocks.get() > 8192) {
			FTBUltimine.LOGGER.warn("maxBlocks is set to more than 8192 blocks!");
			FTBUltimine.LOGGER.warn("This may cause a lot of tick and FPS lag!");
			/*
			FTBUltimine.LOGGER.warn("Outline rendering is enabled for more than {} blocks per excavation!", outlineBlockWarning);
			FTBUltimine.LOGGER.warn("This will almost definitely cause a lot of FPS lag!");
			 */
		}
	}
}
