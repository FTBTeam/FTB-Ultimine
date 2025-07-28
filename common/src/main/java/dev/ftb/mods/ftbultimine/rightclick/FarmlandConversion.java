package dev.ftb.mods.ftbultimine.rightclick;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import dev.ftb.mods.ftbultimine.client.PlatformUtil;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Collection;

public enum FarmlandConversion implements RightClickHandler {
    INSTANCE;

    @Override
    public int handleRightClickBlock(ShapeContext shapeContext, InteractionHand hand, Collection<BlockPos> positions) {
        ServerPlayer player = shapeContext.player();

        //noinspection ConstantValue
        if (!FTBUltimineServerConfig.RIGHT_CLICK_HOE.get() || !PlatformUtil.canTillSoil(player.getMainHandItem())) {
            return 0;
        }

        int clicked = 0;

        for (BlockPos pos : positions) {
            if (!player.level().getBlockState(pos.above()).isAir()) {
                continue;
            }
            BlockState state = player.level().getBlockState(pos);
            if (state.is(FTBUltimine.TILLABLE_TAG)) {
                player.level().setBlock(pos, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                player.level().gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, Blocks.FARMLAND.defaultBlockState()));
                clicked++;

                player.causeFoodExhaustion((float) (FTBUltimineServerConfig.getExhaustionPerBlock(player) * 0.005D));
                if (hurtItemAndCheckIfBroken(player, hand) || FTBUltimineAPI.isTooExhausted(player)) {
                    break;
                }
            }
        }

        if (clicked > 0) {
            player.level().playSound(player, shapeContext.origPos(), SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1F, 1F);
        }

        return clicked;
    }
}
