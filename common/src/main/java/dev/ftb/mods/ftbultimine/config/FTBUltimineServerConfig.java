package dev.ftb.mods.ftbultimine.config;

import dev.ftb.mods.ftblibrary.config.value.*;
import dev.ftb.mods.ftblibrary.util.Lazy;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import dev.ftb.mods.ftbultimine.integration.IntegrationHandler;
import dev.ftb.mods.ftbultimine.integration.ranks.FTBRanksIntegration;
import dev.ftb.mods.ftbultimine.registry.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.regex.Pattern;

import static dev.ftb.mods.ftbultimine.FTBUltimine.LOGGER;

public interface FTBUltimineServerConfig {
	String KEY = FTBUltimineAPI.MOD_ID + "-server";

	Config CONFIG = Config.create(KEY).standardTopLevelComment(FTBUltimineAPI.MOD_NAME, KEY, false);

	Config FEATURES = CONFIG.addGroup("features");

	BooleanValue RIGHT_CLICK_AXE = FEATURES.addBoolean("right_click_axe", true)
			.comment("Right-click with an axe with the Ultimine key held to strip multiple logs and scrape/unwax copper blocks");
	BooleanValue RIGHT_CLICK_SHOVEL = FEATURES.addBoolean("right_click_shovel", true)
			.comment("Right-click with a shovel with the Ultimine key held to flatten multiple grass/dirt blocks into dirt paths");
	BooleanValue RIGHT_CLICK_HOE = FEATURES.addBoolean("right_click_hoe", true)
			.comment("Right-click with a hoe with the Ultimine key held to till multiple grass/dirt blocks into farmland");
	BooleanValue RIGHT_CLICK_PLANTING = FEATURES.addBoolean("right_click_planting", true)
			.comment("Right-click with seeds (wheat, carrots, potatoes, etc.) on farmland with the Ultimine key held to plant on multiple farmland blocks at once");
	BooleanValue RIGHT_CLICK_HARVESTING = FEATURES.addBoolean("right_click_harvesting", true)
			.comment("Right-click crops with the Ultimine key held to harvest multiple crop blocks");
	BooleanValue RIGHT_CLICK_CRYSTALS = FEATURES.addBoolean("right_click_crystals", true)
			.comment("Right-click budding crystals (e.g. amethyst, AE2 certus) with the Ultimine key held to harvest multiple crystals",
					"FTB EZ Crystals must also be installed");
	BooleanValue SINGLE_CROP_HARVESTING = FEATURES.addBoolean("single_crop_harvesting", true)
			.comment("When true, right-clicking a crop block without the Ultimine key held harvests it");

	Config COSTS_LIMITS = CONFIG.addGroup("costs_limits");

	IntValue MAX_BLOCKS = COSTS_LIMITS.addInt("max_blocks", 64)
			.range(32768)
			.comment("Max amount of blocks that can be ultimined at once",
							 "If FTB Ranks is installed, the 'ftbultimine.max_blocks' ranks node can override this",
							 "This value can also be modified with the 'ftbultimine:max_blocks_modifier' entity attribute");
	DoubleValue EXHAUSTION_PER_BLOCK = COSTS_LIMITS.addDouble("exhaustion_per_block", 20.0)
			.range(10000.0)
			.comment("Hunger multiplier for each block ultimined (fractional values allowed)");
	DoubleValue EXPERIENCE_PER_BLOCK = COSTS_LIMITS.addDouble("experience_per_block", 0.0)
			.range(20000.0)
			.comment("Amount of experience taken per block ultimined (fractional values allowed)");
	BooleanValue REQUIRE_TOOL = COSTS_LIMITS.addBoolean("require_tool", false)
			.comment("Require a damageable tool, or an item in the 'ftbultimine:tools' tag, to ultimine.");
	BooleanValue REQUIRE_VALID_TOOL_FOR_BLOCK = COSTS_LIMITS.addBoolean("require_valid_tool_for_block", false)
			.comment("Require a valid tool for the block being ultimined, i.e. one that can properly harvest the block");
	LongValue ULTIMINE_COOLDOWN = COSTS_LIMITS.addLong("ultimine_cooldown", 0L, 0L, Long.MAX_VALUE)
			.comment("Cooldown in ticks between successive uses of the ultimine feature",
					"If FTB Ranks is installed, the 'ftbultimine.ultimine_cooldown' ranks node can override this",
					"This value can also be modified with the 'ftbultimine:cooldown_modifier' entity attribute");

	Config MISC = CONFIG.addGroup("misc");
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

	///
	static void onConfigChanged(boolean isServerSide) {
		if (isServerSide) {
			MERGE_TAGS_SHAPELESS.tags.invalidate();
			MERGE_TAGS_SHAPED.tags.invalidate();

			if (MAX_BLOCKS.get() > 8192) {
				LOGGER.warn("'max_blocks' server config setting is set to more than 8192 blocks; this may cause performance issues!");
			}

			FTBUltimine.getInstance().schedulePlayerTimeSync();
		}
	}

	static int getMaxBlocks(ServerPlayer player) {
		int max = IntegrationHandler.ftbRanksLoaded ? FTBRanksIntegration.getMaxBlocks(player, MAX_BLOCKS.get()) : MAX_BLOCKS.get();
		return Math.max(0, max + (int) Math.round(getAttrSafe(player, ModAttributes.FixedHolder.MAX_BLOCKS_MODIFIER.get())));
	}

	static long getUltimineCooldown(ServerPlayer player) {
		long cooldown = IntegrationHandler.ftbRanksLoaded ? FTBRanksIntegration.getUltimineCooldown(player, ULTIMINE_COOLDOWN.get()) : ULTIMINE_COOLDOWN.get();
		return Math.max(0, cooldown + Math.round(getAttrSafe(player, ModAttributes.FixedHolder.COOLDOWN_MODIFIER.get())));
	}

	static double getExhaustionPerBlock(ServerPlayer player) {
		double ex = IntegrationHandler.ftbRanksLoaded ? FTBRanksIntegration.getExhaustionPerBlock(player) : EXHAUSTION_PER_BLOCK.get();
		return Math.max(0.0, ex + getAttrSafe(player, ModAttributes.FixedHolder.EXHAUSTION_MODIFIER.get()));
	}

	static double getExperiencePerBlock(ServerPlayer player) {
		double ex = IntegrationHandler.ftbRanksLoaded ? FTBRanksIntegration.getExperiencePerBlock(player) : EXPERIENCE_PER_BLOCK.get();
		return Math.max(0.0, ex + getAttrSafe(player, ModAttributes.FixedHolder.EXPERIENCE_MODIFIER.get()));
	}

	private static double getAttrSafe(ServerPlayer player, Holder<Attribute> attr) {
		return player.getAttributes().hasAttribute(attr) ? player.getAttributeValue(attr) : 0.0;
	}

	class BlockTagsConfig {
		private final StringListValue value;

		private final Lazy<Set<TagKey<Block>>> tags = Lazy.of(this::buildTags);
		private boolean matchAny = false;

		public BlockTagsConfig(Config parent, String name, List<String> defaults, String... comment) {
			this.value = parent.addStringList(name, defaults).comment(comment);
		}

		public boolean match(BlockState original, BlockState toTest) {
			var tags = getTags();
			return matchAny && !toTest.isAir() && !(toTest.getBlock() instanceof LiquidBlock) && !(original.getBlock() instanceof EntityBlock)
					|| tags.stream().filter(original::is).anyMatch(toTest::is);
		}

		public Collection<TagKey<Block>> getTags() {
			return tags.get();
		}

		private Set<TagKey<Block>> buildTags() {
			if (value.get().contains("*")) {
				// special-case: this makes for far faster matching when we just want to match everything
				matchAny = true;
				return Set.of();
			} else {
				Set<TagKey<Block>> res = new HashSet<>();
				value.get().forEach(s -> {
					Identifier id = Identifier.tryParse(s);
					if (id != null) {
						res.add(TagKey.create(Registries.BLOCK, id));
					} else {
						Pattern pattern = regexFromGlobString(s);
						BuiltInRegistries.BLOCK.getTags().forEach((tag) -> {
							if (pattern.asPredicate().test(tag.key().location().toString())) {
								res.add(tag.key());
							}
						});
					}
				});
				return res;
			}
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
