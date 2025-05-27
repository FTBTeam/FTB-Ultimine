package dev.ftb.mods.ftbultimine.config;

import dev.architectury.utils.GameInstance;
import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import dev.ftb.mods.ftbultimine.integration.ranks.FTBRanksIntegration;
import dev.ftb.mods.ftbultimine.integration.IntegrationHandler;
import dev.ftb.mods.ftbultimine.net.SyncUltimineTimePacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.regex.Pattern;

import static dev.ftb.mods.ftbultimine.FTBUltimine.LOGGER;

public interface FTBUltimineServerConfig {
	String KEY = FTBUltimineAPI.MOD_ID + "-server";

	SNBTConfig CONFIG = SNBTConfig.create(KEY)
			.comment("Server-specific configuration for FTB Ultimine",
					"Modpack defaults should be defined in <instance>/config/" + KEY + ".snbt",
					"  (may be overwritten on modpack update)",
					"Server admins may locally override this by copying into <instance>/world/serverconfig/" + KEY + ".snbt",
					"  (will NOT be overwritten on modpack update)"
			);

	SNBTConfig FEATURES = CONFIG.addGroup("features");

	BooleanValue RIGHT_CLICK_AXE = FEATURES.addBoolean("right_click_axe", true)
			.comment("Right-click with an axe with the Ultimine key held to strip multiple logs and scrape/unwax copper blocks");
	BooleanValue RIGHT_CLICK_SHOVEL = FEATURES.addBoolean("right_click_shovel", true)
			.comment("Right-click with a shovel with the Ultimine key held to flatten multiple grass/dirt blocks into dirt paths");
	BooleanValue RIGHT_CLICK_HOE = FEATURES.addBoolean("right_click_hoe", true)
			.comment("Right-click with a hoe with the Ultimine key held to till multiple grass/dirt blocks into farmland");
	BooleanValue RIGHT_CLICK_HARVESTING = FEATURES.addBoolean("right_click_harvesting", true)
			.comment("Right-click crops with the Ultimine key held to harvest multiple crop blocks");
	BooleanValue RIGHT_CLICK_CRYSTALS = (BooleanValue)FEATURES.addBoolean("right_click_crystals", true)
			.comment("Right-click budding crystals (e.g. amethyst, AE2 certus) with the Ultimine key held to harvest multiple crystals",
					"FTB EZ Crystals must also be installed");

	SNBTConfig COSTS_LIMITS = CONFIG.addGroup("costs_limits");

	IntValue MAX_BLOCKS = COSTS_LIMITS.addInt("max_blocks", 64)
			.range(32768)
			.comment("Max amount of blocks that can be ultimined at once");
	DoubleValue EXHAUSTION_PER_BLOCK = COSTS_LIMITS.addDouble("exhaustion_per_block", 20.0)
			.range(10000.0)
			.comment("Hunger multiplier for each block ultimined (fractional values allowed)");
	DoubleValue EXPERIENCE_PER_BLOCK = COSTS_LIMITS.addDouble("experience_per_block", 0.0)
			.range(20000.0)
			.comment("Amount of experience taken per block ultimined (fractional values allowed)");
	BooleanValue REQUIRE_TOOL = COSTS_LIMITS.addBoolean("require_tool", false)
			.comment("Require a damageable tool, or an item in the 'ftbultimine:tools' tag, to ultimine.");
	LongValue ULTIMINE_COOLDOWN = COSTS_LIMITS.addLong("ultimine_cooldown", 0L, 0L, Long.MAX_VALUE)
			.comment("Cooldown in ticks between successive uses of the Ultimine feature");

	SNBTConfig MISC = CONFIG.addGroup("misc");
	BlockTagsConfig MERGE_TAGS_SHAPELESS = new BlockTagsConfig(MISC, "merge_tags",
			new ArrayList<>(List.of(
					"minecraft:base_stone_overworld",
					"c:ores/*",
					"forge:ores/*"
			)),
			"These tags will be considered the same block when checking for blocks to Ultimine in shapeless mining mode");
	BlockTagsConfig MERGE_TAGS_SHAPED = new BlockTagsConfig(MISC, "merge_tags_shaped",
			new ArrayList<>(List.of(
					"*"
			)),
			"These tags will be considered the same block when checking for blocks to Ultimine in shaped mining modes");
	IntValue PREVENT_TOOL_BREAK = MISC.addInt("prevent_tool_break", 0, 0, 100)
			.comment("This will stop mining if tool reaches X durability. It's possible this won't work with some modded tools if they use non-standard durability handling.");

	BooleanValue CANCEL_ON_BLOCK_BREAK_FAIL = MISC.addBoolean("cancel_on_block_break_fail", false)
			.comment("If a block couldn't be broken (even though it should be), stop ultimining immediately instead of skipping to the next block.");

//	BooleanValue USE_TRINKET = CONFIG.addBoolean("use_trinket", false)
//			.comment("(This only works if the mod 'Lost Trinkets' is installed!)",
//					"Adds a custom 'Ultiminer' trinket players will need to activate to be able to use Ultimine.",
//					"Make sure you disable the 'Octopick' trinket if this is enabled!");

	/*********************************************************************/

	static void onConfigChanged(boolean isServerSide) {
		if (isServerSide) {
			MERGE_TAGS_SHAPELESS.tags = null;
			MERGE_TAGS_SHAPED.tags = null;

			if (MAX_BLOCKS.get() > 8192) {
				LOGGER.warn("'max_blocks' server config setting is set to more than 8192 blocks; this may cause performance issues!");
			}

			if (GameInstance.getServer() != null) {
				GameInstance.getServer().getPlayerList().getPlayers().forEach(sp ->
						NetworkHelper.sendTo(sp, new SyncUltimineTimePacket(getUltimineCooldown(sp), SyncUltimineTimePacket.TimeType.COOLDOWN))
				);
			}
		}
	}

	static int getMaxBlocks(ServerPlayer player) {
		return IntegrationHandler.ranksMod ? FTBRanksIntegration.getMaxBlocks(player) : MAX_BLOCKS.get();
	}

	static long getUltimineCooldown(ServerPlayer player) {
		return IntegrationHandler.ranksMod ? FTBRanksIntegration.getUltimineCooldown(player) : ULTIMINE_COOLDOWN.get();
	}

	static double getExperiencePerBlock(ServerPlayer player) {
		return IntegrationHandler.ranksMod ? FTBRanksIntegration.getExperiencePerBlock(player) : EXPERIENCE_PER_BLOCK.get();
	}

	static double getExhaustionPerBlock(ServerPlayer player) {
		return IntegrationHandler.ranksMod ? FTBRanksIntegration.getExhaustionPerBlock(player) : EXHAUSTION_PER_BLOCK.get();
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
