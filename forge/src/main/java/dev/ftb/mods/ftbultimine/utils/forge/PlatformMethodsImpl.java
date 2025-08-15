package dev.ftb.mods.ftbultimine.utils.forge;

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
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolActions;

public class PlatformMethodsImpl {
	public static double reach(ServerPlayer player) {
		return player.getAttributeValue(ForgeMod.BLOCK_REACH.get());
	}

	public static int blockRightClick(ShapeContext shapeContext, ServerPlayer serverPlayer, InteractionHand hand, BlockPos clickPos, Direction face, FTBUltiminePlayerData data) {
		int didWork = 0;
		if (FTBUltimineServerConfig.RIGHT_CLICK_HARVESTING.get() && shapeContext.matcher() == BlockMatcher.CROP_LIKE) {
			return RightClickHandlers.cropHarvesting(serverPlayer, hand, clickPos, face, data);
		}

		if (FTBUltimineServerConfig.RIGHT_CLICK_HOE.get() && serverPlayer.getItemInHand(hand).canPerformAction(ToolActions.HOE_TILL)) {
			return RightClickHandlers.farmlandConversion(serverPlayer, hand, clickPos, data);
		}

		if (FTBUltimineServerConfig.RIGHT_CLICK_AXE.get() && serverPlayer.getItemInHand(hand).canPerformAction(ToolActions.AXE_STRIP)) {
			return RightClickHandlers.axeStripping(serverPlayer, hand, clickPos, data);
		}

		if (FTBUltimineServerConfig.RIGHT_CLICK_SHOVEL.get() && serverPlayer.getItemInHand(hand).canPerformAction(ToolActions.SHOVEL_FLATTEN)) {
			return RightClickHandlers.shovelFlattening(serverPlayer, hand, clickPos, data);
		}

		return didWork;
	}
}
