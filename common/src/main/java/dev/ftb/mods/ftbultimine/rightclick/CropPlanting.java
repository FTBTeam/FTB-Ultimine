package dev.ftb.mods.ftbultimine.rightclick;

import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum CropPlanting implements RightClickHandler {
    INSTANCE;

    private static final TagKey<Item> SEEDS_TAG = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.parse("c:seeds"));

    @Override
    public int handleRightClickBlock(ShapeContext shapeContext, InteractionHand hand, Collection<BlockPos> positions) {
        ServerPlayer player = shapeContext.player();
        ItemStack stack = player.getItemInHand(hand);

        if (!FTBUltimineServerConfig.RIGHT_CLICK_PLANTING.get() || !isPlantable(stack)) {
            return 0;
        }

        Level level = player.level();
        int planted = 0;
        for (BlockPos pos : positions) {
            if (level.getBlockState(pos).getBlock() != Blocks.FARMLAND) continue;
            BlockPos above = pos.above();
            if (!level.getBlockState(above).isAir()) continue;

            BlockHitResult hitResult = new BlockHitResult(Vec3.atBottomCenterOf(above), Direction.UP, pos, false);
            if (stack.useOn(new UseOnContext(player, hand, hitResult)).consumesAction()) {
                planted++;
                if (stack.isEmpty()) break;
            }
        }
        return planted;
    }

    @Override
    public @Nullable List<BlockPos> filterPreview(ServerPlayer player, List<BlockPos> positions) {
        if (!FTBUltimineServerConfig.RIGHT_CLICK_PLANTING.get()) return null;

        ItemStack stack = player.getMainHandItem();
        if (!isPlantable(stack)) {
            stack = player.getOffhandItem();
            if (!isPlantable(stack)) return null;
        }

        int seedCount = stack.getCount();
        Level level = player.level();
        List<BlockPos> filtered = new ArrayList<>();
        for (BlockPos pos : positions) {
            if (filtered.size() >= seedCount) break;
            if (level.getBlockState(pos).getBlock() != Blocks.FARMLAND) continue;
            if (!level.getBlockState(pos.above()).isAir()) continue;
            filtered.add(pos);
        }
        return filtered;
    }

    private static boolean isPlantable(ItemStack stack) {
        if (stack.is(SEEDS_TAG)) return true;
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock() instanceof CropBlock || blockItem.getBlock() instanceof StemBlock;
        }
        return false;
    }
}
