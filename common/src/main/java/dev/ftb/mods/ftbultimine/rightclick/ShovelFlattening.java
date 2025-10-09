package dev.ftb.mods.ftbultimine.rightclick;

import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import dev.ftb.mods.ftbultimine.client.PlatformUtil;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public enum ShovelFlattening implements RightClickHandler {
    INSTANCE;

    @Override
    public int handleRightClickBlock(ShapeContext shapeContext, InteractionHand hand, Collection<BlockPos> positions) {
        ServerPlayer player = shapeContext.player();

        //noinspection ConstantValue
        if (!FTBUltimineServerConfig.RIGHT_CLICK_SHOVEL.get() || !PlatformUtil.canFlattenPath(player.getItemInHand(hand))) {
            return 0;
        }

        int clicked = 0;
        for (BlockPos pos : positions) {
            BlockHitResult hitResult = new BlockHitResult(Vec3.atBottomCenterOf(pos.above()), Direction.UP, pos, false);
            if (player.getMainHandItem().useOn(new UseOnContext(player, hand, hitResult)).consumesAction()) {
                clicked++;
            }
        }
        return clicked;
    }
}
