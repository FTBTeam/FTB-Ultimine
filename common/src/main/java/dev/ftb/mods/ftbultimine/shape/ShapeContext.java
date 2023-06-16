package dev.ftb.mods.ftbultimine.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author LatvianModder
 */
public record ShapeContext(ServerPlayer player, BlockPos pos, Direction face, BlockState original, BlockMatcher matcher, int maxBlocks) {
	public boolean check(BlockState state) {
		return matcher.actualCheck(original, state);
	}

	public BlockState block(BlockPos pos) {
		return player.level().getBlockState(pos);
	}

	public boolean check(BlockPos pos) {
		return check(block(pos));
	}
}