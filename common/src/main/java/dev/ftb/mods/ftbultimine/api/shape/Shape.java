package dev.ftb.mods.ftbultimine.api.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface Shape {
	/**
	 * {@return a unique identifier for this shape, in your mod's namespace}
	 */
	ResourceLocation getName();

	/**
	 * Compute the actual blocks which should be broken in this ultimining operation.
	 * @param context the shape context, which provides information about this operation
	 * @return a list of block positions to attempt to break
	 */
	List<BlockPos> getBlocks(ShapeContext context);

	/**
	 * Most shapes have a specific layout, e.g. 3x3, or 3x3 tunnel. If this is a "shapeless" shape, i.e. the actual
	 * blocks matched depend on the block being broken, override this to return true.
	 *
	 * @return true if the effective blocks chosen depend on the initial broken block type, false if the shape never changes
	 */
	default boolean isIndeterminateShape() {
		return false;
	}

	@ApiStatus.NonExtendable
	default MutableComponent getDisplayName() {
		return Component.translatable("ftbultimine.shape." + getName().toLanguageKey());
	}

	@ApiStatus.NonExtendable
	default boolean isDefault() {
		return false;
	}
}