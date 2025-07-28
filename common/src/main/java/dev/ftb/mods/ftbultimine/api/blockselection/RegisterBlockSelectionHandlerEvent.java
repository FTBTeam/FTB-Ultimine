package dev.ftb.mods.ftbultimine.api.blockselection;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

/**
 * Listen to this event (fired on server startup) to register a custom block-selection handler.
 */
@FunctionalInterface
public interface RegisterBlockSelectionHandlerEvent {
    Event<RegisterBlockSelectionHandlerEvent> REGISTER = EventFactory.createLoop();

    /**
     * Register a new handler.
     *
     * @param registry the registry
     */
    void register(RegisterBlockSelectionHandlerEvent.Dispatcher registry);

    interface Dispatcher {
        /**
         * Register a new handler.
         *
         * @param handler the handler to register
         */
        void registerHandler(BlockSelectionHandler handler);
    }
}
