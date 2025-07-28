package dev.ftb.mods.ftbultimine.api.blockbreaking;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

/**
 * Listen to this event (fired on server startup) to register a custom block-breaking handler.
 */
@FunctionalInterface
public interface RegisterBlockBreakHandlerEvent {
    Event<RegisterBlockBreakHandlerEvent> REGISTER = EventFactory.createLoop();

    /**
     * Register a new handler.
     *
     * @param registry the registry
     */
    void register(Registry registry);

    @FunctionalInterface
    interface Registry {
        /**
         * Register a new handler.
         *
         * @param handler the handler to register
         */
        void registerHandler(BlockBreakHandler handler);
    }
}
