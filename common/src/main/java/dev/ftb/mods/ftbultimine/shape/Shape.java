package dev.ftb.mods.ftbultimine.shape;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
public abstract class Shape {
	private static final Map<String, Shape> MAP = new LinkedHashMap<>();
	private static Shape defaultShape;

	public static void register(Shape shape) {
		MAP.put(shape.getName(), shape);

		if (shape.isDefault()) {
			defaultShape = shape;
		}
	}

	public static Shape get(String id) {
		if (id.isEmpty()) {
			return defaultShape;
		}

		return MAP.getOrDefault(id, defaultShape);
	}

	public Shape next;
	public Shape prev;

	public static void postinit() {
		List<Shape> list = new ArrayList<>(MAP.values());

		for (int i = 0; i < list.size() - 1; i++) {
			list.get(i).next = list.get(i + 1);
			list.get(i + 1).prev = list.get(i);
		}

		list.get(0).prev = list.get(list.size() - 1);
		list.get(list.size() - 1).next = list.get(0);
	}

	public abstract String getName();

	public abstract List<BlockPos> getBlocks(ShapeContext context);

	public boolean isDefault() {
		return false;
	}

	public BlockMatcher getTagMatcher() {
		return BlockMatcher.TAGS_MATCH_SHAPED;
	}
}