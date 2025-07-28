package dev.ftb.mods.ftbultimine.rightclick;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import dev.ftb.mods.ftbultimine.client.PlatformUtil;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.mixin.ShovelItemAccess;
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

public enum ShovelFlattening implements RightClickHandler {
    INSTANCE;

    @Override
    public int handleRightClickBlock(ShapeContext shapeContext, InteractionHand hand, Collection<BlockPos> positions) {
        ServerPlayer player = shapeContext.player();

        //noinspection ConstantValue
        if (!FTBUltimineServerConfig.RIGHT_CLICK_SHOVEL.get() || !PlatformUtil.canFlattenPath(player.getMainHandItem())) {
            return 0;
        }

        int didWork = 0;

        for (BlockPos pos : positions) {
            if (!player.level().getBlockState(pos.above()).isAir()) {
                continue;
            }
            BlockState state = player.level().getBlockState(pos);

            BlockState newState = ShovelItemAccess.getFlattenables().get(state.getBlock());
            if (newState == null && state.is(FTBUltimine.FLATTENABLE_TAG)) {
                newState = Blocks.DIRT_PATH.defaultBlockState();
            }
            if (newState != null) {
                player.level().setBlock(pos, newState, Block.UPDATE_ALL_IMMEDIATE);
                didWork++;

                var result = hurtItemAndCheckIfBroken(player, hand);
                player.level().gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));

                if (result || player.getFoodData().getFoodLevel() <= 0) {
                    break;
                }
            }
        }

        //noinspection ConstantConditions
        if (didWork > 0) {
            // suppress warning: didWork only looks false due to mixin
            player.level().playSound(player, shapeContext.origPos(), SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1F, 1F);
        }

        return didWork;
    }
}
