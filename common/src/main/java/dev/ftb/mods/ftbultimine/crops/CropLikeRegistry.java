package dev.ftb.mods.ftbultimine.crops;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum CropLikeRegistry {
    INSTANCE;

    private final Set<ICropLikeHandler> handlers = ConcurrentHashMap.newKeySet();

    public static CropLikeRegistry getInstance() {
        return INSTANCE;
    }

    public void registerHandler(ICropLikeHandler handler) {
        handlers.add(handler);
    }

    public Optional<ICropLikeHandler> getHandlerFor(Level level, BlockPos pos, BlockState state) {
        return handlers.stream().filter(h -> h.isApplicable(level, pos, state)).findFirst();
    }

    public boolean areStatesEquivalent(BlockState original, BlockState state) {
        return handlers.stream().anyMatch(h -> h.isEquivalent(original, state));
    }
}
