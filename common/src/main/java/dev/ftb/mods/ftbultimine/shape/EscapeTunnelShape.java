package dev.ftb.mods.ftbultimine.shape;

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