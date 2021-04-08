package com.feed_the_beast.mods.ftbultimine.shape;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class EscapeTunnelShape extends Shape {
	@Override
	public String getName() {
		return "escape_tunnel";
	}

	@Override
	public List<BlockPos> getBlocks(ShapeContext context) {
		if (context.face.getAxis().isVertical()) {
			context.face = context.player.getDirection().getOpposite();
		}

		List<BlockPos> list = new ArrayList<>(context.maxBlocks);

		for (int i = 0; i < context.maxBlocks; i++) {
			BlockPos pTop = new BlockPos(context.pos.getX() - context.face.getStepX() * i, context.pos.getY() + i, context.pos.getZ() - context.face.getStepZ() * i);

			if (!context.check(pTop)) {
				break;
			}

			list.add(pTop);
		}

		return list;
	}
}