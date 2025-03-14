package dev.ftb.mods.ftbultimine.crops;

import dev.ftb.mods.ftbultimine.ItemCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ICropLikeHandler {
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
     * Actually harvest the crop, adding any item drops to the {@link ItemCollection} object. This is called for each
     * matching block in the current Ultimine shape, when {@link #isApplicable(Level, BlockPos, BlockState)} has
     * returned true for the block.
     *
     * @param player player doing the harvesting
     * @param pos the block position
     * @param state the block state of the crop
     * @param itemCollection collect harvesting drops into this via {@link ItemCollection#add(ItemStack)}
     */
    void doHarvesting(Player player, BlockPos pos, BlockState state, ItemCollection itemCollection);

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
