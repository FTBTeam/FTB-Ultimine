package dev.ftb.mods.ftbultimine.api.shape;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;

/**
 * Listen to this event (fired on common startup) to register custom ultimining shapes.
 */
@FunctionalInterface
public interface RegisterShapeEvent {
    Event<RegisterShapeEvent> REGISTER = EventFactory.createLoop();

    /**
     * Register a new shape.
     *
     * @param shapeRegistry the shape registry; call {@link ShapeRegistry#register(Shape)} to register an implementation of {@link Shape}
     */
    void register(Registry shapeRegistry);

    interface Registry {
        /**
         * Register a new shape.
         *
         * @param shape the shape to register
         */
        void register(Shape shape);
    }
}
