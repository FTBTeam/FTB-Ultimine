package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import net.minecraft.resources.ResourceLocation;

public class EscapeTunnelShape extends DiagonalTunnelShape {
	private static final ResourceLocation ID = FTBUltimineAPI.id("escape_tunnel");

	@Override
	public ResourceLocation getName() {
		return ID;
	}

	@Override
	protected int yDirection() {
		return 1;
	}
}