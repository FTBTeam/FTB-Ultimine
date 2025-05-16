package dev.ftb.mods.ftbultimine.api.restriction;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

/**
 * Listen to this event to register custom restriction handlers; this can be used to impose limits on when ultimining
 * is possible. E.g. require player to be holding a specific item or have a particular bauble equipped.
 */
public interface RegisterRestrictionHandlerEvent {
    Event<RegisterRestrictionHandlerEvent> REGISTER = EventFactory.createLoop();

    /**
     * Register a new handler.
     *
     * @param registry the registry
     */
    void register(Registry registry);

    interface Registry {
        /**
         * Register a new handler.
         *
         * @param handler the handler to register
         */
        void register(RestrictionHandler handler);
    }
}
