package dev.ftb.mods.ftbultimine.integration.agricraft;

import com.agricraft.agricraft.api.AgriApi;
import com.agricraft.agricraft.api.crop.AgriCrop;
import dev.ftb.mods.ftbultimine.api.util.ItemCollector;
import dev.ftb.mods.ftbultimine.api.crop.CropLikeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public enum AgriCraftCropLikeHandler implements CropLikeHandler {
    INSTANCE;

    @Override
    public boolean isApplicable(Level level, BlockPos pos, BlockState state) {
        return AgriApi.getCrop(level, pos).map(AgriCrop::isFullyGrown).orElse(false);
    }

    @Override
    public boolean doHarvesting(Player player, BlockPos pos, BlockState state, ItemCollector itemCollector) {
        return AgriApi.getCrop(player.level(), pos)
                .map(crop -> crop.harvest(itemCollector::add, player))
                .orElse(false);
    }

    @Override
    public boolean isEquivalent(BlockState original, BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(original.getBlock()).getNamespace().equals("agricraft")
                && original.getBlock() == state.getBlock();
    }
}
