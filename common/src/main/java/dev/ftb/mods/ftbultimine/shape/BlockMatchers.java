package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.crops.CropLikeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockMatchers {
    ShapeContext.Matcher MATCH_BY_BLOCK = BlockMatcher.wrap(
            (original, state) -> original.getBlock() == state.getBlock());

    ShapeContext.Matcher MATCH_BY_TAGS_SHAPELESS = BlockMatcher.wrap(
            FTBUltimineServerConfig.MERGE_TAGS_SHAPELESS::match);

    ShapeContext.Matcher MATCH_BY_TAGS_SHAPED = BlockMatcher.wrap(
            FTBUltimineServerConfig.MERGE_TAGS_SHAPED::match);

    ShapeContext.Matcher MATCH_BY_CROP_TYPE = BlockMatcher.wrap(
            (original, state) -> state.getBlock() instanceof BushBlock
                    || CropLikeRegistry.getInstance().areStatesEquivalent(original, state));

    static ShapeContext.Matcher determineBestMatcher(Level level, BlockPos pos, BlockState origState, Shape shape) {
        ShapeContext.Matcher tagMatcher = shape.isIndeterminateShape() ? MATCH_BY_TAGS_SHAPELESS : MATCH_BY_TAGS_SHAPED;

        if (tagMatcher.check(origState, origState)) {
            return tagMatcher;
        } else if (CropLikeRegistry.getInstance().getHandlerFor(level, pos, origState).isPresent()) {
            return BlockMatchers.MATCH_BY_CROP_TYPE;
        } else {
            return BlockMatchers.MATCH_BY_BLOCK;
        }
    }
}
