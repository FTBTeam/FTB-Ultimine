package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShapeRegistry {
    // list of all known shapes
    private static final List<Shape> LIST = new CopyOnWriteArrayList<>();

    private static Shape defaultShape = null;
    private static boolean frozen = false;

    public static void register(Shape shape) {
        register(shape, false);
    }

    public static void register(Shape shape, boolean isDefault) {
        if (frozen) {
            throw new IllegalStateException("Shape registry is frozen!");
        }

        LIST.add(shape);

        if (isDefault) {
            if (defaultShape != null) {
                FTBUltimine.LOGGER.warn("default shape already set to {}! ignoring attempt to make {} default", defaultShape.getName(), shape.getName());
            }
            defaultShape = shape;
        }
    }

    public static void freeze() {
        frozen = true;
    }

    @NotNull
    public static Shape getShape(int idx) {
        if (idx < 0) {
            idx += LIST.size();
        } else if (idx >= LIST.size()) {
            idx -= LIST.size();
        }
        return idx >= 0 && idx < LIST.size() ? LIST.get(idx) : defaultShape;
    }

    public static int shapeCount() {
        return LIST.size();
    }
}
