package dev.ftb.mods.ftbultimine.mixin;

import dev.ftb.mods.ftbultimine.integration.acceldecay.LogBreakTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "playerWillDestroy", at = @At("HEAD"))
    public void onPlayerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player, CallbackInfoReturnable<BlockState> cir) {
        // using a mixin here and not a block break event; need to know if the block is *definitely* bring broken
        if (blockState.is(BlockTags.LOGS)) {
            LogBreakTracker.INSTANCE.playerBrokeLog(player);
        }
    }
}
