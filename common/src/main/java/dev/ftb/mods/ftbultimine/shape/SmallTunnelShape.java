package dev.ftb.mods.ftbultimine.shape;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class SmallTunnelShape implements Shape {
	@Override
	public String getName() {
		return "small_tunnel";
	}

	@Override
	public List<BlockPos> getBlocks(ShapeContext context) {
		List<BlockPos> list = new ArrayList<>(context.maxBlocks());

		for (int i = 0; i < context.maxBlocks(); i++) {
			BlockPos pos = context.pos().relative(context.face(), -i);

			if (!context.check(pos)) {
				break;
			}

			list.add(pos);
		}

		return list;
	}
}