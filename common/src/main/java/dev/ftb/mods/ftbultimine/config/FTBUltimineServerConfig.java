package dev.ftb.mods.ftbultimine.config;

import dev.ftb.mods.ftblibrary.snbt.config.DoubleValue;
import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringListValue;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import me.shedaniel.architectury.hooks.LevelResourceHooks;
import me.shedaniel.architectury.hooks.TagHooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author LatvianModder
 */
public interface FTBUltimineServerConfig {

	SNBTConfig CONFIG = SNBTConfig.create(FTBUltimine.MOD_ID + "-server")
			.comment("Server-specific configuration for FTB Ultimine",
					"This file is meant for server administrators to control user behaviour.",
					"Changes in this file currently require a server restart to take effect");
	LevelResource CONFIG_FILE_PATH = LevelResourceHooks.create("serverconfig/" + CONFIG.key + ".snbt");

	IntValue MAX_BLOCKS = CONFIG.getInt("max_blocks", 64)
			.range(32768)
			.comment("Max amount of blocks that can be ultimined at once");

	DoubleValue EXHAUSTION_PER_BLOCK = CONFIG.getDouble("exhaustion_per_block", 20)
			.range(10000)
			.comment("Hunger multiplied for each block mined with ultimine");

	BlockTagsConfig MERGE_TAGS = new BlockTagsConfig(CONFIG, "merge_tags", Collections.singletonList("minecraft:base_stone_overworld"),
			"These tags will be considered the same block when checking for blocks to Ultimine");

	static void load(MinecraftServer server) {
		CONFIG.load(server.getWorldPath(CONFIG_FILE_PATH));
		MERGE_TAGS.tags = null;

		if (MAX_BLOCKS.get() > 8192) {
			FTBUltimine.LOGGER.warn("maxBlocks is set to more than 8192 blocks!");
			FTBUltimine.LOGGER.warn("This may cause a lot of tick and FPS lag!");
		}
	}

	class BlockTagsConfig {
		public final String name;
		public final SNBTConfig config;
		public final StringListValue value;

		private Collection<Tag<Block>> tags = null;

		public BlockTagsConfig(SNBTConfig parent, String name, List<String> defaults, String... comment) {
			this.name = name;
			this.config = parent;
			this.value = config.getStringList(name, defaults).comment(comment);
		}

		public Collection<Tag<Block>> getTags() {
			if (tags == null) {
				tags = new HashSet<>();
				value.get().forEach(s -> tags.add(TagHooks.getBlockOptional(new ResourceLocation(s))));
			}
			return tags;
		}
	}
}
