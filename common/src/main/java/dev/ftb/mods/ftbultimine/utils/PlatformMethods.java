package dev.ftb.mods.ftbultimine.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class PlatformMethods {
	@ExpectPlatform
	public static double reach(ServerPlayer player) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static int blockRightClick(ShapeContext shapeContext, ServerPlayer player, InteractionHand hand, BlockPos clickPos, Direction face, FTBUltiminePlayerData data) {
		throw new AssertionError();
	}
}
