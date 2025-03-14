package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.crops.CropLikeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BlockMatcher {
	boolean check(BlockState original, BlockState state);

	default boolean actualCheck(BlockState original, BlockState state) {
		return (TagCache.isEmptyBlockWhitelist() || state.is(FTBUltimine.BLOCK_WHITELIST))
				&& !state.is(FTBUltimine.EXCLUDED_BLOCKS)
				&& check(original, state);
	}

	BlockMatcher MATCH = (original, state) -> original.getBlock() == state.getBlock();
	BlockMatcher TAGS_MATCH_SHAPELESS = FTBUltimineServerConfig.MERGE_TAGS_SHAPELESS::match;
	BlockMatcher TAGS_MATCH_SHAPED = FTBUltimineServerConfig.MERGE_TAGS_SHAPED::match;
	BlockMatcher CROP_LIKE = (original, state) -> state.getBlock() instanceof BushBlock || CropLikeRegistry.getInstance().areStatesEquivalent(original, state);

	class TagCache {
		private static Boolean emptyBlockWhitelist = null;  // null = need to recompute

		private static boolean isEmptyBlockWhitelist() {
			if (emptyBlockWhitelist == null) {
				emptyBlockWhitelist = BuiltInRegistries.BLOCK.getTag(FTBUltimine.BLOCK_WHITELIST)
						.map(holders -> holders.size() == 0)
						.orElse(true);
			}
			return emptyBlockWhitelist;
		}

		public static void onReload() {
			emptyBlockWhitelist = null;
		}
	}
}