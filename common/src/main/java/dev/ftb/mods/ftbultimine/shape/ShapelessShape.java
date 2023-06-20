package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.EntityDistanceComparator;
import net.minecraft.core.BlockPos;

import java.util.*;

/**
 * @author LatvianModder
 */
public class ShapelessShape implements Shape {
	// all blocks in 3x3x3 cube around the block
	private static final List<BlockPos> NEIGHBOR_POSITIONS = new ArrayList<>(26);
	// all blocks in 5x5 square around block's Y-level, plus blocks directly above & below
	private static final List<BlockPos> NEIGHBOR_POSITIONS_PLANT = new ArrayList<>(26);

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
		NEIGHBOR_POSITIONS_PLANT.add(new BlockPos(0, 1, 0));
		NEIGHBOR_POSITIONS_PLANT.add(new BlockPos(0, -1, 0));
	}

	@Override
	public String getName() {
		return "shapeless";
	}

	@Override
	public BlockMatcher getTagMatcher() {
		return BlockMatcher.TAGS_MATCH_SHAPELESS;
	}

	@Override
	public List<BlockPos> getBlocks(ShapeContext context) {
		HashSet<BlockPos> known = new HashSet<>();
		walk(context, known, context.matcher() == BlockMatcher.CROP_LIKE);

		List<BlockPos> list = new ArrayList<>(known);
		list.sort(new EntityDistanceComparator(context.pos()));

		if (list.size() > context.maxBlocks()) {
			list.subList(context.maxBlocks(), list.size()).clear();
		}

		return list;
	}

	private void walk(ShapeContext context, HashSet<BlockPos> known, boolean cropLike) {
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

				for (BlockPos side : cropLike ? NEIGHBOR_POSITIONS_PLANT : NEIGHBOR_POSITIONS) {
					BlockPos offset = ptr.offset(side);

					if (traversed.add(offset)) {
						openSet.add(offset);
					}
				}
			}
		}
	}
}