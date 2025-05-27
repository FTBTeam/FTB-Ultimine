package dev.ftb.mods.ftbultimine.integration.ezcrystals.neoforge;

import dev.ftb.mods.ftbezcrystals.FTBEZCrystals;
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

public enum EZCrystalsHandler implements RightClickHandler {
    INSTANCE;

    @Override
    public int handleRightClickBlock(ShapeContext shapeContext, InteractionHand hand, Collection<BlockPos> positions) {
        if (!FTBUltimineServerConfig.RIGHT_CLICK_CRYSTALS.get()) {
            return 0;
        } else {
            ServerPlayer player = shapeContext.player();
            int harvested = 0;

            for (BlockPos pos : positions) {
                BlockState before = player.level().getBlockState(pos);
                FTBEZCrystals.harvestCrystal(player.level(), pos, player.getMainHandItem(), player);
                if (player.level().getBlockState(pos) != before) {
                    harvested++;
                }
            }

            return harvested;
        }
    }
}
