package dev.ftb.mods.ftbultimine.api.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

/**
 * The context for an ultimining operation, passed to {@link Shape#getBlocks(ShapeContext)}. This provides all the
 * information needed for a shape to determine the blocks that should be affected by the operation. In particular,
 * implementations of {@link Shape} should call {@link #check(BlockPos)} to determine if a particular block position
 * contains a blockstate suitable for inclusion in the ultimining process.
 *
 * @param player the player doing the ultimining
 * @param pos the position of the original broken block
 * @param face the face of the original block that was clicked by the player
 * @param origState the blockstate of the original block
 * @param matcher the block matcher predicate to determine if some blockstate is equivalent to the original state
 * @param maxBlocks the maximum number of blocks that may be broken in this operation
 */
public record ShapeContext(ServerPlayer player, BlockPos pos, Direction face, BlockState origState, Matcher matcher, int maxBlocks) {
	public BlockState getBlockState(BlockPos pos) {
		return player.level().getBlockState(pos);
	}

	public boolean check(BlockPos pos) {
		return check(getBlockState(pos));
	}

	public boolean check(BlockState state) {
		return matcher.check(origState, state);
	}

	/**
	 * A predicate interface to determine if some blockstate is equivalent to the original state.
	 */
	@FunctionalInterface
	@ApiStatus.NonExtendable
    public interface Matcher {
		boolean check(BlockState original, BlockState state);
	}
}