package dev.ftb.mods.ftbultimine.integration.ezcrystals.forge;

import dev.ftb.mods.ftbezcrystals.FTBEZCrystals;
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.Collection;

public enum EZCrystalsHandler implements RightClickHandler {
    INSTANCE;

    @Override
    public int handleRightClickBlock(ShapeContext shapeContext, InteractionHand hand, Collection<BlockPos> positions) {
        if (!FTBUltimineServerConfig.RIGHT_CLICK_CRYSTALS.get()) {
            return 0;
        } else {
            ServerPlayer player = shapeContext.player();
            return (int) positions.stream()
                    .filter(pos -> FTBEZCrystals.harvestCrystal(player.level(), pos, player.getItemInHand(hand), player))
                    .count();
        }
    }
}
