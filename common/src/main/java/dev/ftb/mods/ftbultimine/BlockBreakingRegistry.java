package dev.ftb.mods.ftbultimine;

import dev.ftb.mods.ftbultimine.api.blockbreaking.BlockBreakHandler;
import dev.ftb.mods.ftbultimine.api.blockbreaking.RegisterBlockBreakHandlerEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

public enum BlockBreakingRegistry implements RegisterBlockBreakHandlerEvent.Registry {
    INSTANCE;

    private final Collection<BlockBreakHandler> handlers = new CopyOnWriteArrayList<>();

    @Override
    public void registerHandler(BlockBreakHandler handler) {
        handlers.add(handler);
    }

    public Collection<BlockBreakHandler> getHandlers() {
        return Collections.unmodifiableCollection(handlers);
    }
}
