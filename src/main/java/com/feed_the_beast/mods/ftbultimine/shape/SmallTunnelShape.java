package com.feed_the_beast.mods.ftbultimine.shape;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class SmallTunnelShape extends Shape
{
	@Override
	public String getName()
	{
		return "small_tunnel";
	}

	@Override
	public List<BlockPos> getBlocks(ShapeContext context)
	{
		List<BlockPos> list = new ArrayList<>(context.maxBlocks);

		if (context.face.getAxis().isVertical())
		{
			for (int y = 0; y < context.maxBlocks; y++)
			{
				BlockPos p = new BlockPos(context.pos.getX(), context.pos.getY() - context.face.getYOffset() * y, context.pos.getZ());

				if (!context.check(p))
				{
					break;
				}

				list.add(p);
			}

			return list;
		}

		for (int i = 0; i < context.maxBlocks / 2; i++)
		{
			BlockPos pTop = new BlockPos(context.pos.getX() - context.face.getXOffset() * i, context.pos.getY(), context.pos.getZ() - context.face.getZOffset() * i);

			if (!context.check(pTop))
			{
				break;
			}

			list.add(pTop);

			BlockPos pBot = pTop.offset(Direction.DOWN);

			if (context.check(pBot))
			{
				list.add(pBot);
			}
		}

		return list;
	}
}