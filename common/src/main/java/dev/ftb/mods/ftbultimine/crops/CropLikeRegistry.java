package dev.ftb.mods.ftbultimine.crops;

import dev.ftb.mods.ftbultimine.api.crop.CropLikeHandler;
import dev.ftb.mods.ftbultimine.api.crop.RegisterCropLikeEvent;
import net.minecraft.core.BlockPos;
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
}
