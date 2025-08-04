package dev.ftb.mods.ftbultimine.api.shape;

import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import dev.ftb.mods.ftbultimine.api.blockselection.BlockSelectionHandler;
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
 * @param origPos the position of the original broken block
 * @param face the face of the original block that was clicked by the player
 * @param origState the blockstate of the original block
 * @param matcher the block matcher predicate to determine if some blockstate is equivalent to the original state
 * @param maxBlocks the maximum number of blocks that may be broken in this operation
 */
public record ShapeContext(ServerPlayer player, BlockPos origPos, Direction face, BlockState origState, Matcher matcher, int maxBlocks) {
	public BlockState getBlockState(BlockPos pos) {
		return player.level().getBlockState(pos);
	}

	@Deprecated
	BlockPos pos() {
		return origPos;
	}

	/**
	 * Called by implementations of {@link Shape} to check if a particular block position should be part of an
	 * ultimining operation.
	 *
	 * @param pos the block position to check
	 * @return true if the block should be included, false otherwise
	 */
	public boolean check(BlockPos pos) {
		BlockState state = getBlockState(pos);
		BlockSelectionHandler.Result res = FTBUltimineAPI.api().customSelectionCheck(player, this.origPos, pos, origState, state);
		return res.asBoolean().orElse(check(state));
	}

	@ApiStatus.Internal
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