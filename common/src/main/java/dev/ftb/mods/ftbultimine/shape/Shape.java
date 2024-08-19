package dev.ftb.mods.ftbultimine.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public interface Shape {
	String getName();

	List<BlockPos> getBlocks(ShapeContext context);

	default BlockMatcher getTagMatcher() {
		return BlockMatcher.TAGS_MATCH_SHAPED;
	}

	default MutableComponent getDisplayName() {
		return Component.translatable("ftbultimine.shape." + getName());
	}
}