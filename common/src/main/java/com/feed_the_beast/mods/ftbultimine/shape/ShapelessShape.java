package com.feed_the_beast.mods.ftbultimine.shape;

import com.feed_the_beast.mods.ftbultimine.EntityDistanceComparator;
import net.minecraft.core.BlockPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author LatvianModder
 */
public class ShapelessShape extends Shape
{
	public static final BlockPos[] NEIGHBOR_POSITIONS = new BlockPos[26];
	public static final BlockPos[] NEIGHBOR_POSITIONS_PLANT = new BlockPos[24];

	static
	{
		NEIGHBOR_POSITIONS[0] = new BlockPos(1, 0, 0);
		NEIGHBOR_POSITIONS[1] = new BlockPos(-1, 0, 0);
		NEIGHBOR_POSITIONS[2] = new BlockPos(0, 0, 1);
		NEIGHBOR_POSITIONS[3] = new BlockPos(0, 0, -1);
		NEIGHBOR_POSITIONS[4] = new BlockPos(0, 1, 0);
		NEIGHBOR_POSITIONS[5] = new BlockPos(0, -1, 0);

		NEIGHBOR_POSITIONS[6] = new BlockPos(1, 0, 1);
		NEIGHBOR_POSITIONS[7] = new BlockPos(1, 0, -1);
		NEIGHBOR_POSITIONS[8] = new BlockPos(-1, 0, 1);
		NEIGHBOR_POSITIONS[9] = new BlockPos(-1, 0, -1);

		NEIGHBOR_POSITIONS[10] = new BlockPos(1, 1, 0);
		NEIGHBOR_POSITIONS[11] = new BlockPos(-1, 1, 0);
		NEIGHBOR_POSITIONS[12] = new BlockPos(0, 1, 1);
		NEIGHBOR_POSITIONS[13] = new BlockPos(0, 1, -1);

		NEIGHBOR_POSITIONS[14] = new BlockPos(1, -1, 0);
		NEIGHBOR_POSITIONS[15] = new BlockPos(-1, -1, 0);
		NEIGHBOR_POSITIONS[16] = new BlockPos(0, -1, 1);
		NEIGHBOR_POSITIONS[17] = new BlockPos(0, -1, -1);

		NEIGHBOR_POSITIONS[18] = new BlockPos(1, 1, 1);
		NEIGHBOR_POSITIONS[19] = new BlockPos(1, 1, -1);
		NEIGHBOR_POSITIONS[20] = new BlockPos(-1, 1, 1);
		NEIGHBOR_POSITIONS[21] = new BlockPos(-1, 1, -1);

		NEIGHBOR_POSITIONS[22] = new BlockPos(1, -1, 1);
		NEIGHBOR_POSITIONS[23] = new BlockPos(1, -1, -1);
		NEIGHBOR_POSITIONS[24] = new BlockPos(-1, -1, 1);
		NEIGHBOR_POSITIONS[25] = new BlockPos(-1, -1, -1);

		NEIGHBOR_POSITIONS_PLANT[0] = new BlockPos(1, 0, 0);
		NEIGHBOR_POSITIONS_PLANT[1] = new BlockPos(-1, 0, 0);
		NEIGHBOR_POSITIONS_PLANT[2] = new BlockPos(0, 0, 1);
		NEIGHBOR_POSITIONS_PLANT[3] = new BlockPos(0, 0, -1);

		NEIGHBOR_POSITIONS_PLANT[4] = new BlockPos(1, 0, 1);
		NEIGHBOR_POSITIONS_PLANT[5] = new BlockPos(1, 0, -1);
		NEIGHBOR_POSITIONS_PLANT[6] = new BlockPos(-1, 0, 1);
		NEIGHBOR_POSITIONS_PLANT[7] = new BlockPos(-1, 0, -1);

		NEIGHBOR_POSITIONS_PLANT[8] = new BlockPos(2, 0, 0);
		NEIGHBOR_POSITIONS_PLANT[9] = new BlockPos(-2, 0, 0);
		NEIGHBOR_POSITIONS_PLANT[10] = new BlockPos(0, 0, 2);
		NEIGHBOR_POSITIONS_PLANT[11] = new BlockPos(0, 0, -2);

		NEIGHBOR_POSITIONS_PLANT[12] = new BlockPos(-1, 0, -2);
		NEIGHBOR_POSITIONS_PLANT[13] = new BlockPos(1, 0, -2);
		NEIGHBOR_POSITIONS_PLANT[14] = new BlockPos(2, 0, -1);
		NEIGHBOR_POSITIONS_PLANT[15] = new BlockPos(2, 0, 1);
		NEIGHBOR_POSITIONS_PLANT[16] = new BlockPos(2, 0, 2);
		NEIGHBOR_POSITIONS_PLANT[17] = new BlockPos(-2, 0, 2);
		NEIGHBOR_POSITIONS_PLANT[18] = new BlockPos(-2, 0, 1);
		NEIGHBOR_POSITIONS_PLANT[19] = new BlockPos(-2, 0, -1);

		NEIGHBOR_POSITIONS_PLANT[20] = new BlockPos(2, 0, 2);
		NEIGHBOR_POSITIONS_PLANT[21] = new BlockPos(2, 0, -2);
		NEIGHBOR_POSITIONS_PLANT[22] = new BlockPos(-2, 0, 2);
		NEIGHBOR_POSITIONS_PLANT[23] = new BlockPos(-2, 0, -2);
	}

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
		walk(context, known, context.matcher == BlockMatcher.BUSH);

		List<BlockPos> list = new ArrayList<>(known);
		list.sort(new EntityDistanceComparator(context.pos));

		if (list.size() > context.maxBlocks)
		{
			list.subList(context.maxBlocks, list.size()).clear();
		}

		return list;
	}

	private void walk(ShapeContext context, HashSet<BlockPos> known, boolean plant)
	{
		Set<BlockPos> traversed = new HashSet<>();
		Deque<BlockPos> openSet = new ArrayDeque<>();
		openSet.add(context.pos);
		traversed.add(context.pos);

		while (!openSet.isEmpty())
		{
			BlockPos ptr = openSet.pop();

			if (context.check(ptr) && known.add(ptr))
			{
				if (known.size() >= context.maxBlocks)
				{
					return;
				}

				for (BlockPos side : plant ? NEIGHBOR_POSITIONS_PLANT : NEIGHBOR_POSITIONS)
				{
					BlockPos offset = ptr.offset(side);

					if (traversed.add(offset))
					{
						openSet.add(offset);
					}
				}
			}
		}
	}
}