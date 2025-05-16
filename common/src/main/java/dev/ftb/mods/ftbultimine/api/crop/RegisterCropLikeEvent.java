package dev.ftb.mods.ftbultimine.api.crop;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

/**
 * Listen to this interface to register a custom croplike detection handler. This can be used to implement right-click
 * harvesting behaviour for crops that don't behave like vanilla crops (Ultimine registers a builtin handler for all
 * vanilla crops including blocks like sweet berries and cocoa beans).
 */
@FunctionalInterface
public interface RegisterCropLikeEvent {
    Event<RegisterCropLikeEvent> REGISTER = EventFactory.createLoop();

    void register(Dispatcher registry);

    interface Dispatcher {
        void registerHandler(CropLikeHandler handler);
    }
}
