package com.feed_the_beast.mods.ftbultimine.shape;

import com.feed_the_beast.mods.ftbultimine.EntityDistanceComparator;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ShapelessShape extends Shape
{
	@Override
	public String getName()
	{
		return "shapeless";
	}

	@Override
	public boolean isDefault()
	{
		return true;
	}

	@Override
	public List<BlockPos> getBlocks(ShapeContext context)
	{
		HashSet<BlockPos> known = new HashSet<>();
		walk(context, context.pos, known, context.maxBlocks, false);

		List<BlockPos> list = new ArrayList<>(known);
		list.sort(new EntityDistanceComparator(context.pos));

		if (list.size() > context.maxBlocks)
		{
			list.subList(context.maxBlocks, list.size()).clear();
		}

		return list;
	}

	private void walk(ShapeContext context, BlockPos pos, HashSet<BlockPos> known, int remaining, boolean checkBlock)
	{
		if (remaining <= 0 || known.contains(pos))
		{
			return;
		}

		if (checkBlock)
		{
			if (!context.check(pos))
			{
				return;
			}
		}

		known.add(pos);

		if (remaining <= 1)
		{
			return;
		}

		if (context.matcher == BlockMatcher.BUSH)
		{
			for (int x = -3; x <= 3; x++)
			{
				for (int z = -3; z <= 3; z++)
				{
					if (x != 0 || z != 0)
					{
						walk(context, pos.add(x, 0, z), known, remaining - 1, true);
					}
				}
			}
		}
		else
		{
			for (int x = -1; x <= 1; x++)
			{
				for (int y = -1; y <= 1; y++)
				{
					for (int z = -1; z <= 1; z++)
					{
						if (x != 0 || y != 0 || z != 0)
						{
							walk(context, pos.add(x, y, z), known, remaining - 1, true);
						}
					}
				}
			}
		}
	}
}