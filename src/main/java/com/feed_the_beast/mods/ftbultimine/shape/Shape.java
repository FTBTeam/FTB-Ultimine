package com.feed_the_beast.mods.ftbultimine.shape;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
public abstract class Shape
{
	private static final Map<String, Shape> MAP = new LinkedHashMap<>();
	private static Shape defaultShape;

	public static void register(Shape shape)
	{
		MAP.put(shape.getName(), shape);

		if (shape.isDefault())
		{
			defaultShape = shape;
		}
	}

	public static Shape get(String id)
	{
		if (id.isEmpty())
		{
			return defaultShape;
		}

		return MAP.getOrDefault(id, defaultShape);
	}

	public abstract String getName();

	public abstract List<BlockPos> getBlocks(ShapeContext context);

	public boolean isDefault()
	{
		return false;
	}

	public final Shape next()
	{
		List<Shape> list = new ArrayList<>(MAP.values());
		return list.get((list.indexOf(this) + 1) % list.size());
	}
}