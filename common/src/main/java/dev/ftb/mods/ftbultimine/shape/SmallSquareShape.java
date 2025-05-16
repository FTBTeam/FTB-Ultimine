package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.minecraft.resources.ResourceLocation;

public class SmallSquareShape extends LargeTunnelShape {
	private static final ResourceLocation ID = FTBUltimine.id("small_square");

	@Override
	public ResourceLocation getName() {
		return ID;
	}

	@Override
	protected int maxDepth() {
		return 1;
	}
}