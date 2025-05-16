package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class SmallTunnelShape implements Shape {
	private static final ResourceLocation ID = FTBUltimine.id("small_tunnel");

	@Override
	public ResourceLocation getName() {
		return ID;
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