package dev.ftb.mods.ftbultimine.crops;

import dev.ftb.mods.ftbultimine.ItemCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public enum VanillaCropLikeHandler implements ICropLikeHandler {
    INSTANCE;

    public static boolean looksLikeACrop(BlockState state) {
        // just checking if it's crop-like for breakability purposes (not harvestability)
        return state.getBlock() instanceof BushBlock || state.getBlock() instanceof CocoaBlock;
    }

    public static boolean equivalentForSelection(BlockState orig, BlockState other) {
        return looksLikeACrop(orig) && looksLikeACrop(other)
                && getBushType(orig.getBlock()) == getBushType(other.getBlock());
    }

    @Override
    public boolean isApplicable(Level level, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)
                || state.getBlock() instanceof SweetBerryBushBlock && state.getValue(SweetBerryBushBlock.AGE) >= SweetBerryBushBlock.MAX_AGE
                || state.getBlock() instanceof CocoaBlock && state.getValue(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE;
    }

    @Override
    public void doHarvesting(Player player, BlockPos pos, BlockState state, ItemCollection itemCollection) {
        BlockEntity blockEntity = state.hasBlockEntity() ? player.level().getBlockEntity(pos) : null;
        List<ItemStack> drops = Block.getDrops(state, (ServerLevel) player.level(), pos, blockEntity, player, ItemStack.EMPTY);

        for (ItemStack stack : drops) {
            // should work for most if not all modded crop blocks, hopefully
            if (Block.byItem(stack.getItem()) == state.getBlock() && consumesItemToReplant(state)) {
                stack.shrink(1);
            }
            itemCollection.add(stack);
        }

        resetAge(player.level(), pos, state);
    }

    @Override
    public boolean isEquivalent(BlockState original, BlockState state) {
        return equivalentForSelection(original, state);
    }

    private static int getBushType(Block block) {
        if (block instanceof CropBlock) {
            return 1;
        } else if (block instanceof SaplingBlock) {
            return 2;
        } else if (block instanceof CocoaBlock) {
            return 3;
        } else if (block instanceof SweetBerryBushBlock) {
            return 4;
        }

        return 0;
    }

    private boolean consumesItemToReplant(BlockState state) {
        return state.getBlock() != Blocks.SWEET_BERRY_BUSH;
    }

    private void resetAge(Level level, BlockPos pos, BlockState currentState) {
        if (currentState.getBlock() instanceof CropBlock cropBlock) {
            level.setBlock(pos, cropBlock.getStateForAge(0), Block.UPDATE_ALL);
        } else if (currentState.getBlock() instanceof SweetBerryBushBlock) {
            int currentAge = currentState.getValue(SweetBerryBushBlock.AGE);
            level.setBlock(pos, currentState.setValue(SweetBerryBushBlock.AGE, Math.min(currentAge, 1)), Block.UPDATE_ALL);
        } else if (currentState.getBlock() instanceof CocoaBlock) {
            level.setBlock(pos, currentState.setValue(CocoaBlock.AGE, 0), Block.UPDATE_ALL);
        }
    }
}
