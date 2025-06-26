package dev.ftb.mods.ftbultimine.api.blockbreaking;

import dev.ftb.mods.ftbultimine.api.shape.Shape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Implementations of this can be registered via {@link RegisterBlockBreakHandlerEvent} to add custom block-breaking
 * behaviour for specific blocks, e.g. complex blocks where the block hit result may be important.
 * <p>
 * Note that if multiple handlers are registered that want to handle the same block, the first handler registered wins.
 */
@FunctionalInterface
public interface BlockBreakHandler {
    /**
     * Called for every block that should be ultimined, other than the original block, which has already been broken.
     * <p>
     * Your implementation should initially check that the block in question is one that you specifically wish to handle,
     * and immediately return {@code PASS} if not. If the block is of interest, you should carry out your custom
     * block-breaking logic and return {@code SUCCESS} or {@code FAIL} as appropriate.
     *
     * @param player the player carrying out the ultimining operation
     * @param pos the position of the candidate block
     * @param state the blockstate for the candidate block
     * @param shape the ultimining shape currently in use
     * @param hitResult the player's block hit result for the block that was originally broken
     * @return {@code PASS} if the candidate block is not of interest, {@code SUCCESS} if the candidate block was successfully broken,
     *      or {@code FAIL} if the candidate block could not be broken
     */
    Result breakBlock(Player player, BlockPos pos, BlockState state, Shape shape, BlockHitResult hitResult);

    enum Result {
        SUCCESS,
        PASS,
        FAIL
    }
}
