package dev.ftb.mods.ftbultimine.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.integration.FTBRanksIntegration;
import dev.ftb.mods.ftbultimine.net.SyncConfigToServerPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.SERVER_CONFIG_DIR;
import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.loadDefaulted;
import static dev.ftb.mods.ftbultimine.FTBUltimine.*;

public interface FTBUltimineServerConfig {

	SNBTConfig CONFIG = SNBTConfig.create(FTBUltimine.MOD_ID + "-server")
			.comment("Server-specific configuration for FTB Ultimine",
					"This file is meant for server administrators to control user behaviour.",
					"Changes in this file currently require a server restart to take effect");

	IntValue MAX_BLOCKS = CONFIG.addInt("max_blocks", 64)
			.range(32768)
			.comment("Max amount of blocks that can be ultimined at once");

	DoubleValue EXHAUSTION_PER_BLOCK = CONFIG.addDouble("exhaustion_per_block", 20)
			.range(10000)
			.comment("Hunger multiplied for each block mined with ultimine");
	DoubleValue EXPERIENCE_PER_BLOCK = CONFIG.addDouble("experience_per_block", 0.0)
			.range(20000)
			.comment("Amount of experience taken per block mined (fractional values allowed)");

	BlockTagsConfig MERGE_TAGS_SHAPELESS = new BlockTagsConfig(CONFIG, "merge_tags",
			new ArrayList<>(List.of(
					"minecraft:base_stone_overworld",
					"c:*_ores",
					"forge:ores/*"
			)),
			"These tags will be considered the same block when checking for blocks to Ultimine in shapeless mining mode");
	BlockTagsConfig MERGE_TAGS_SHAPED = new BlockTagsConfig(CONFIG, "merge_tags_shaped",
			new ArrayList<>(List.of(
					"*"
			)),
			"These tags will be considered the same block when checking for blocks to Ultimine in shaped mining modes");

	IntValue PREVENT_TOOL_BREAK = CONFIG.addInt("prevent_tool_break", 0, 0, 100)
			.comment("This will stop mining if tool reaches X durability. It's possible it won't work with special tool types.");

	BooleanValue CANCEL_ON_BLOCK_BREAK_FAIL = CONFIG.addBoolean("cancel_on_block_break_fail", false)
			.comment("This is an advanced option, that you better leave alone This will stop ultimining on first block that it can't mine, rather than skipping it.");

	BooleanValue REQUIRE_TOOL = CONFIG.addBoolean("require_tool", false)
			.comment("Require a damageable tool, or an item in the ftbultimine:tools tag, to ultimine.");

	BooleanValue RIGHT_CLICK_AXE = CONFIG.addBoolean("right_click_axe", true)
			.comment("Right-click with an axe with the Ultimine key held to strip multiple logs and scrape/unwax copper blocks");
	BooleanValue RIGHT_CLICK_SHOVEL = CONFIG.addBoolean("right_click_shovel", true)
			.comment("Right-click with a shovel with the Ultimine key held to flatten multiple grass/dirt blocks into dirt paths");
	BooleanValue RIGHT_CLICK_HOE = CONFIG.addBoolean("right_click_hoe", true)
			.comment("Right-click with a hoe with the Ultimine key held to till multiple grass/dirt blocks into farmland");
	BooleanValue RIGHT_CLICK_HARVESTING = CONFIG.addBoolean("right_click_harvesting", true)
			.comment("Right-click crops with the Ultimine key held to harvest multiple crop blocks");

	LongValue ULTIMINE_COOLDOWN = CONFIG.addLong("ultimine_cooldown", 0L, 0L, Long.MAX_VALUE)
			.comment("Cooldown in ticks between successive uses of the ultimine feature");

//	BooleanValue USE_TRINKET = CONFIG.addBoolean("use_trinket", false)
//			.comment("(This only works if the mod 'Lost Trinkets' is installed!)",
//					"Adds a custom 'Ultiminer' trinket players will need to activate to be able to use Ultimine.",
//					"Make sure you disable the 'Octopick' trinket if this is enabled!");

	/*********************************************************************/

	static void load(MinecraftServer server) {
		loadDefaulted(CONFIG, server.getWorldPath(SERVER_CONFIG_DIR), MOD_ID);
		clearTagCache();

		// TODO legacy compat - remove in 1.19.3+
		Path commonPath = ConfigUtil.CONFIG_DIR.resolve(MOD_ID + ".snbt").toAbsolutePath();
		if (Files.exists(commonPath)) {
			// merge in and remove the old common config; this is now part of the server config
			PREVENT_TOOL_BREAK.set(FTBUltimineCommonConfig.PREVENT_TOOL_BREAK.get());
			CANCEL_ON_BLOCK_BREAK_FAIL.set(FTBUltimineCommonConfig.CANCEL_ON_BLOCK_BREAK_FAIL.get());
			REQUIRE_TOOL.set(FTBUltimineCommonConfig.REQUIRE_TOOL.get());
			LOGGER.info("Merged setting from old common config file {} into server config", commonPath);
			try {
				Files.delete(commonPath);
				LOGGER.info("Deleted old common config file {}", commonPath);
			} catch (IOException e) {
				LOGGER.warn("can't delete {}: {}", commonPath, e.getMessage());
			}
		}

		if (MAX_BLOCKS.get() > 8192) {
			LOGGER.warn("maxBlocks is set to more than 8192 blocks!");
			LOGGER.warn("This may cause a lot of tick and FPS lag!");
		}
	}

	static ConfigGroup getConfigGroup() {
		ConfigGroup group = new ConfigGroup(MOD_ID + ".server_settings", accepted -> {
			if (accepted) {
				clearTagCache();
				SNBTCompoundTag config = new SNBTCompoundTag();
				FTBUltimineServerConfig.CONFIG.write(config);
				new SyncConfigToServerPacket(config).sendToServer();
			}
		});
		CONFIG.createClientConfig(group);
		return group;
	}

	static void clearTagCache() {
		MERGE_TAGS_SHAPELESS.tags = null;
		MERGE_TAGS_SHAPED.tags = null;
	}

	static int getMaxBlocks(ServerPlayer player) {
		return ranksMod ? FTBRanksIntegration.getMaxBlocks(player) : MAX_BLOCKS.get();
	}

	static long getUltimineCooldown(ServerPlayer player) {
		return ranksMod ? FTBRanksIntegration.getUltimineCooldown(player) : ULTIMINE_COOLDOWN.get();
	}

	class BlockTagsConfig {
		private final StringListValue value;

		private Set<TagKey<Block>> tags = null;
		private boolean matchAny = false;

		public BlockTagsConfig(SNBTConfig parent, String name, List<String> defaults, String... comment) {
			this.value = parent.addStringList(name, defaults).comment(comment);
		}

		public boolean match(BlockState original, BlockState toTest) {
			var tags = getTags();
			return matchAny && !toTest.isAir() && !(toTest.getBlock() instanceof LiquidBlock) && !(original.getBlock() instanceof EntityBlock)
					|| tags.stream().filter(original::is).anyMatch(toTest::is);
		}

		public Collection<TagKey<Block>> getTags() {
			if (tags == null) {
				if (value.get().contains("*")) {
					// special-case: this makes for far faster matching when we just want to match everything
					matchAny = true;
					tags = Collections.emptySet();
				} else {
					tags = new HashSet<>();
					value.get().forEach(s -> {
						ResourceLocation rl = ResourceLocation.tryParse(s);
						if (rl != null) {
							tags.add(TagKey.create(Registries.BLOCK, rl));
						} else {
							Pattern pattern = regexFromGlobString(s);
							BuiltInRegistries.BLOCK.getTags().forEach((tag) -> {
								if (pattern.asPredicate().test(tag.getFirst().location().toString())) {
									tags.add(tag.getFirst());
								}
							});
						}
					});
				}
			}
			return tags;
		}

		private static Pattern regexFromGlobString(String glob) {
			StringBuilder sb = new StringBuilder();
			sb.append("^");
			for (int i = 0; i < glob.length(); i++) {
				char c = glob.charAt(i);
				if (c == '*') {
					sb.append(".*");
				} else if (c == '?') {
					sb.append(".");
				} else if (c == '.') {
					sb.append("\\.");
				} else if (c == '\\') {
					sb.append("\\\\");
				} else {
					sb.append(c);
				}
			}
			sb.append("$");
			return Pattern.compile(sb.toString());
		}
	}
}
