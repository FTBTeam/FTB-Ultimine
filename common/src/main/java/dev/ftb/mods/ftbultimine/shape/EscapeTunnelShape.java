package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.minecraft.resources.ResourceLocation;

public class EscapeTunnelShape extends DiagonalTunnelShape {
	private static final ResourceLocation ID = FTBUltimine.id("escape_tunnel");

	@Override
	public ResourceLocation getName() {
		return ID;
	}

	@Override
	protected int yDirection() {
		return 1;
	}
}