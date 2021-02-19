package com.feed_the_beast.mods.ftbultimine.shape;

import net.minecraft.core.BlockPos;

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

		for (int i = 0; i < context.maxBlocks; i++)
		{
			BlockPos p = new BlockPos(context.pos.getX() - context.face.getStepX() * i, context.pos.getY() - context.face.getStepY() * i, context.pos.getZ() - context.face.getStepZ() * i);

			if (!context.check(p))
			{
				break;
			}

			list.add(p);
		}

		return list;
	}
}