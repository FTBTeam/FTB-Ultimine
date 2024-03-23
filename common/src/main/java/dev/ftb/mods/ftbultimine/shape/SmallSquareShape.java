package dev.ftb.mods.ftbultimine.shape;

public class SmallSquareShape extends LargeTunnelShape {
	@Override
	public String getName() {
		return "small_square";
	}

	@Override
	protected int maxDepth() {
		return 1;
	}
}