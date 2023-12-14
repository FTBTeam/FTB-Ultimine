package dev.ftb.mods.ftbultimine.shape;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface Shape {
	String getName();

	List<BlockPos> getBlocks(ShapeContext context);

	default BlockMatcher getTagMatcher() {
		return BlockMatcher.TAGS_MATCH_SHAPED;
	}
}