package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.EntityDistanceComparator;
import net.minecraft.core.BlockPos;

import java.util.*;

/**
 * @author LatvianModder
 */
public class ShapelessShape extends Shape {
	private static final List<BlockPos> NEIGHBOR_POSITIONS = new ArrayList<>(26);
	private static final List<BlockPos> NEIGHBOR_POSITIONS_PLANT = new ArrayList<>(24);

	static {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (x != 0 || y != 0 || z != 0) NEIGHBOR_POSITIONS.add(new BlockPos(x, y, z));
				}
			}
		}
		for (int x = -2; x <= 2; x++) {
				for (int z = -2; z <= 2; z++) {
					if (x != 0 || z != 0) NEIGHBOR_POSITIONS_PLANT.add(new BlockPos(x, 0, z));
				}
		}
	}

	@Override
	public String getName() {
		return "shapeless";
	}

	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public BlockMatcher getTagMatcher() {
		return BlockMatcher.TAGS_MATCH_SHAPELESS;
	}

	@Override
	public List<BlockPos> getBlocks(ShapeContext context) {
		HashSet<BlockPos> known = new HashSet<>();
		walk(context, known, context.matcher() == BlockMatcher.BUSH);

		List<BlockPos> list = new ArrayList<>(known);
		list.sort(new EntityDistanceComparator(context.pos()));

		if (list.size() > context.maxBlocks()) {
			list.subList(context.maxBlocks(), list.size()).clear();
		}

		return list;
	}

	private void walk(ShapeContext context, HashSet<BlockPos> known, boolean plant) {
		Set<BlockPos> traversed = new HashSet<>();
		Deque<BlockPos> openSet = new ArrayDeque<>();
		openSet.add(context.pos());
		traversed.add(context.pos());

		while (!openSet.isEmpty()) {
			BlockPos ptr = openSet.pop();

			if (context.check(ptr) && known.add(ptr)) {
				if (known.size() >= context.maxBlocks()) {
					return;
				}

				for (BlockPos side : plant ? NEIGHBOR_POSITIONS_PLANT : NEIGHBOR_POSITIONS) {
					BlockPos offset = ptr.offset(side);

					if (traversed.add(offset)) {
						openSet.add(offset);
					}
				}
			}
		}
	}
}