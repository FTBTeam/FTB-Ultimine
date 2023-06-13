package dev.ftb.mods.ftbultimine.shape;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class EscapeTunnelShape extends DiagonalTunnelShape {
	@Override
	public String getName() {
		return "escape_tunnel";
	}

	@Override
	protected int yDirection() {
		return 1;
	}
}