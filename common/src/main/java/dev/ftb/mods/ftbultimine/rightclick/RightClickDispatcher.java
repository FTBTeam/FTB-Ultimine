package dev.ftb.mods.ftbultimine.rightclick;

import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.api.rightclick.RegisterRightClickHandlerEvent;
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import net.minecraft.world.InteractionHand;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public enum RightClickDispatcher implements RegisterRightClickHandlerEvent.Dispatcher {
    INSTANCE;

    public static RightClickDispatcher getInstance() {
        return INSTANCE;
    }

    private final List<RightClickHandler> handlers = new CopyOnWriteArrayList<>();

    public int dispatchRightClick(ShapeContext shapeContext, InteractionHand hand, FTBUltiminePlayerData playerData) {
        for (RightClickHandler handler : handlers) {
            int nBlocks = handler.handleRightClickBlock(shapeContext, hand, playerData.cachedPositions());
            if (nBlocks > 0) {
                return nBlocks;
            }
        }
        return 0;
    }

    @Override
    public void registerHandler(RightClickHandler handler) {
        handlers.add(handler);
    }

    public void clear() {
        handlers.clear();
    }
}
