package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.shape.RegisterShapeEvent;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public enum ShapeRegistry implements RegisterShapeEvent.Registry {
    INSTANCE;

    // list of all known shapes
    private final List<Shape> shapesList = new CopyOnWriteArrayList<>();

    private Shape defaultShape = null;

    /**
     * Register a new shape. Only call this via {@link RegisterShapeEvent#register(RegisterShapeEvent.Registry)} !
     *
     * @param shape the shape to register
     */
    @Override
    public void register(Shape shape) {
        shapesList.add(shape);

        if (shape.isDefault()) {
            if (defaultShape != null) {
                FTBUltimine.LOGGER.warn("default shape already set to {}! ignoring attempt to make {} default",
                        defaultShape.getName(), shape.getName());
            } else {
                defaultShape = shape;
            }
        }
    }

    @NotNull
    public Shape getShape(int idx) {
        if (idx < 0) {
            idx += shapesList.size();
        } else if (idx >= shapesList.size()) {
            idx -= shapesList.size();
        }
        return idx >= 0 && idx < shapesList.size() ? shapesList.get(idx) : defaultShape;
    }

    public int shapeCount() {
        return shapesList.size();
    }
}
