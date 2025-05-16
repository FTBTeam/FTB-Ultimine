package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

public record BlockMatcher(ShapeContext.Matcher wrapped) implements ShapeContext.Matcher {
	public static BlockMatcher wrap(ShapeContext.Matcher matcher) {
		return new BlockMatcher(matcher);
	}

	@Override
	public boolean check(BlockState original, BlockState state) {
		return (TagCache.isEmptyBlockWhitelist() || state.is(FTBUltimine.BLOCK_WHITELIST))
				&& !state.is(FTBUltimine.EXCLUDED_BLOCKS)
				&& wrapped.check(original, state);
	}

	public static class TagCache {
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