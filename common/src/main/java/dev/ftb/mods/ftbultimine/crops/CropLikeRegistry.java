package dev.ftb.mods.ftbultimine.crops;

import dev.ftb.mods.ftbultimine.api.crop.CropLikeHandler;
import dev.ftb.mods.ftbultimine.api.crop.RegisterCropLikeEvent;
import dev.ftb.mods.ftbultimine.api.util.ItemCollector;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum CropLikeRegistry implements RegisterCropLikeEvent.Dispatcher {
    INSTANCE;

    private final Set<CropLikeHandler> handlers = ConcurrentHashMap.newKeySet();

    public static CropLikeRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerHandler(CropLikeHandler handler) {
        handlers.add(handler);
    }

    public Optional<CropLikeHandler> getHandlerFor(Level level, BlockPos pos, BlockState state) {
        return handlers.stream().filter(h -> h.isApplicable(level, pos, state)).findFirst();
    }

    public boolean areStatesEquivalent(BlockState original, BlockState state) {
        return handlers.stream().anyMatch(h -> h.isEquivalent(original, state));
    }

    public void clear() {
        handlers.clear();
    }

    public static boolean checkForSingleCropHarvesting(Player player, BlockPos clickPos) {
        BlockState state = player.level().getBlockState(clickPos);
        var cropHandler = getInstance().getHandlerFor(player.level(), clickPos, state);
        if (cropHandler.isPresent() && cropHandler.get().isApplicable(player.level(), clickPos, state)) {
            ItemCollector collector = new ItemCollector();
            if (cropHandler.get().doHarvesting(player, clickPos, state, collector)) {
                collector.drop(player.level(), clickPos);
                return true;
            }
        }
        return false;
    }
}
