package dev.ftb.mods.ftbultimine.api.rightclick;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

/**
 * Listen to this event (fired on server startup) to register custom right-click ultimining handlers.
 */
@FunctionalInterface
public interface RegisterRightClickHandlerEvent {
    Event<RegisterRightClickHandlerEvent> REGISTER = EventFactory.createLoop();

    /**
     * Register a new handler.
     *
     * @param registry the right-click dispatcher registry
     */
    void register(Dispatcher registry);

    @FunctionalInterface
    interface Dispatcher {
        /**
         * Register a new handler.
         * @param handler the right-click handler to be registered
         */
        void registerHandler(RightClickHandler handler);
    }
}
