package dev.ftb.mods.ftbultimine.api.crop;

import dev.ftb.mods.ftbultimine.api.util.ItemCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implementations of this can be registered via {@link RegisterCropLikeEvent} to add harvesting behaviour for custom
 * crop-like blocks.
 */
public interface CropLikeHandler {
    /**
     * Is this handler applicable for the block at the given position? Implementations must check if the block is
     * a harvestable crop, and that it is fully grown, ready for harvesting. This is called for blocks in the current
     * Ultimine shape when the player right-clicks a block.
     *
     * @param level the player's level
     * @param pos the blockpos for the candidate crop
     * @param state the blockstate for the candidate crop
     * @return true if this is a crop ready for harvesting
     */
    boolean isApplicable(Level level, BlockPos pos, BlockState state);

    /**
     * Actually harvest the crop, adding any item drops to the {@link ItemCollector} object. This is called for each
     * matching block in the current Ultimine shape, when {@link #isApplicable(Level, BlockPos, BlockState)} has
     * returned true for the block. Growth levels for the crop should also be reset here (either by setting the age to
     * zero, or breaking the block, as appropriate).
     *
     * @param player player doing the harvesting
     * @param pos the block position
     * @param state the block state of the crop
     * @param itemCollector collect harvesting drops into this via {@link ItemCollector#add(ItemStack)}
     */
    boolean doHarvesting(Player player, BlockPos pos, BlockState state, ItemCollector itemCollector);

    /**
     * Check if the two block states are equivalent for harvesting purposes, i.e. if {@code original} would be
     * harvested, would {@code state} also be harvested?
     *
     * @param original the original blockstate, right-clicked by the player
     * @param state a blockstate in the current Ultimine shape
     * @return true if this is candidate for harvesting (but not necessarily fully-grown)
     */
    boolean isEquivalent(BlockState original, BlockState state);
}
