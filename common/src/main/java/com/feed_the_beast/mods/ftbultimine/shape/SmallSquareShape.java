package com.feed_the_beast.mods.ftbultimine.shape;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class SmallSquareShape extends Shape {
	@Override
	public String getName() {
		return "small_square";
	}

	@Override
	public List<BlockPos> getBlocks(ShapeContext context) {
		List<BlockPos> list = new ArrayList<>(9);
		list.add(context.pos);

		for (int a = -1; a <= 1; a++) {
			for (int b = -1; b <= 1; b++) {
				if (a == 0 && b == 0) {
					continue;
				}

				BlockPos p = null;

				switch (context.face.getAxis()) {
					case X:
						p = context.pos.offset(0, a, b);
						break;
					case Y:
						p = context.pos.offset(a, 0, b);
						break;
					case Z:
						p = context.pos.offset(a, b, 0);
						break;
				}

				if (context.check(p)) {
					list.add(p);
				}
			}
		}

		return list;
	}
}