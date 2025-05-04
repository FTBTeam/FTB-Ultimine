package dev.ftb.mods.ftbultimine.utils.fabric;

import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.RightClickHandlers;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.shape.BlockMatcher;
import dev.ftb.mods.ftbultimine.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ShovelItem;

public class PlatformMethodsImpl {
	public static double reach(ServerPlayer player) {
		return 5.0;
	}

	public static int blockRightClick(ShapeContext shapeContext, ServerPlayer serverPlayer, InteractionHand hand, BlockPos clickPos, Direction face, FTBUltiminePlayerData data) {
		int didWork = 0;
		if (FTBUltimineServerConfig.RIGHT_CLICK_HARVESTING.get() && shapeContext.matcher() == BlockMatcher.CROP_LIKE) {
			return RightClickHandlers.cropHarvesting(serverPlayer, hand, clickPos, face, data);
		}

		if (FTBUltimineServerConfig.RIGHT_CLICK_HOE.get() && serverPlayer.getItemInHand(hand).getItem() instanceof HoeItem) {
			return RightClickHandlers.farmlandConversion(serverPlayer, hand, clickPos, data);
		}

		if (FTBUltimineServerConfig.RIGHT_CLICK_AXE.get() && serverPlayer.getItemInHand(hand).getItem() instanceof AxeItem) {
			return RightClickHandlers.axeStripping(serverPlayer, hand, clickPos, data);
		}

		if (FTBUltimineServerConfig.RIGHT_CLICK_SHOVEL.get() && serverPlayer.getItemInHand(hand).getItem() instanceof ShovelItem) {
			return RightClickHandlers.shovelFlattening(serverPlayer, hand, clickPos, data);
		}

		return didWork;
	}
}