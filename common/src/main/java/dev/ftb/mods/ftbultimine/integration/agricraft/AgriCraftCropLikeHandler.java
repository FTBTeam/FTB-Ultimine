package dev.ftb.mods.ftbultimine.integration.agricraft;

import com.agricraft.agricraft.api.AgriApi;
import com.agricraft.agricraft.api.crop.AgriCrop;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.ItemCollection;
import dev.ftb.mods.ftbultimine.crops.CropLikeRegistry;
import dev.ftb.mods.ftbultimine.crops.ICropLikeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public enum AgriCraftCropLikeHandler implements ICropLikeHandler {
    INSTANCE;

    @Override
    public boolean isApplicable(Level level, BlockPos pos, BlockState state) {
        return AgriApi.getCrop(level, pos).map(AgriCrop::isFullyGrown).orElse(false);
    }

    @Override
    public void doHarvesting(Player player, BlockPos pos, BlockState state, ItemCollection itemCollection) {
        AgriApi.getCrop(player.level(), pos)
                .ifPresent(crop -> crop.harvest(itemCollection::add, player));
    }

    @Override
    public boolean isEquivalent(BlockState original, BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(original.getBlock()).getNamespace().equals("agricraft")
                && original.getBlock() == state.getBlock();
    }

    public static void init() {
        CropLikeRegistry.getInstance().registerHandler(INSTANCE);
        FTBUltimine.LOGGER.info("Agricraft detected, registered Agricraft crop harvest handler");
    }
}
