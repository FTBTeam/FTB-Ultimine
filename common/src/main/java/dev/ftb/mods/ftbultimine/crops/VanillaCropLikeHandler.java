package dev.ftb.mods.ftbultimine.crops;

import dev.ftb.mods.ftbultimine.api.util.ItemCollector;
import dev.ftb.mods.ftbultimine.api.crop.CropLikeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public enum VanillaCropLikeHandler implements CropLikeHandler {
    INSTANCE;

    @Override
    public boolean isApplicable(Level level, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)
                || state.getBlock() instanceof SweetBerryBushBlock && state.getValue(SweetBerryBushBlock.AGE) >= SweetBerryBushBlock.MAX_AGE
                || state.getBlock() instanceof CocoaBlock && state.getValue(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE;
    }

    @Override
    public boolean doHarvesting(Player player, BlockPos pos, BlockState state, ItemCollector itemCollector) {
        BlockEntity blockEntity = state.hasBlockEntity() ? player.level().getBlockEntity(pos) : null;
        List<ItemStack> drops = Block.getDrops(state, (ServerLevel) player.level(), pos, blockEntity, player, ItemStack.EMPTY);

        for (ItemStack stack : drops) {
            // should work for most if not all modded crop blocks, hopefully
            if (Block.byItem(stack.getItem()) == state.getBlock() && consumesItemToReplant(state)) {
                stack.shrink(1);
            }
            itemCollector.add(stack);
        }

        resetAge(player.level(), pos, state);

        return true;
    }

    @Override
    public boolean isEquivalent(BlockState original, BlockState state) {
        return (state.getBlock() instanceof BushBlock || state.getBlock() instanceof CocoaBlock)
                && getBushType(original.getBlock()) == getBushType(state.getBlock());
    }

    private static int getBushType(Block block) {
        if (block instanceof CropBlock) {
            return 1;
        } else if (block instanceof SaplingBlock) {
            return 2;
        } else if (block instanceof CocoaBlock) {
            return 3;
        }

        return 0;
    }

    private boolean consumesItemToReplant(BlockState state) {
        return state.getBlock() != Blocks.SWEET_BERRY_BUSH;
    }

    private void resetAge(Level level, BlockPos pos, BlockState currentState) {
        if (currentState.getBlock() instanceof CropBlock cropBlock) {
            BlockState newState = currentState.setValue(cropBlock.getAgeProperty(), 0);
            level.setBlock(pos, newState, Block.UPDATE_ALL);
        } else if (currentState.getBlock() instanceof SweetBerryBushBlock) {
            level.setBlock(pos, currentState.setValue(SweetBerryBushBlock.AGE, 1), Block.UPDATE_ALL);
        } else if (currentState.getBlock() instanceof CocoaBlock) {
            level.setBlock(pos, currentState.setValue(CocoaBlock.AGE, 0), Block.UPDATE_ALL);
        }
    }
}
