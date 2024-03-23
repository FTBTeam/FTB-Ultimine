package dev.ftb.mods.ftbultimine.shape;

public class MiningTunnelShape extends DiagonalTunnelShape {
	@Override
	public String getName() {
		return "mining_tunnel";
	}

	@Override
	protected int yDirection() {
		return -1;
	}
}