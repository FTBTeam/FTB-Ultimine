package dev.ftb.mods.ftbultimine.utils;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/// Utility class for collecting items dropped during a multi-block ultimining operation.
public class ItemCollector implements Consumer<ItemStack> {
	// same semantics as vanilla's ItemStackLinkedSet.TYPE_AND_TAG, which isn't accessible on all platforms
	private static final Hash.Strategy<ItemStack> TYPE_AND_COMPONENTS = new Hash.Strategy<>() {
		@Override
		public int hashCode(ItemStack stack) {
			return ItemStack.hashItemAndComponents(stack);
		}

		@Override
		public boolean equals(ItemStack a, ItemStack b) {
			return a == b || a != null && b != null && a.isEmpty() == b.isEmpty() && ItemStack.isSameItemSameComponents(a, b);
		}
	};

	private final Object2IntMap<ItemStack> items = new Object2IntOpenCustomHashMap<>(TYPE_AND_COMPONENTS);

	public void clear() {
		items.clear();
	}

	/// Add an item to the collection.
	///
	/// @param stack the itemstack to add
	@Override
	public void accept(ItemStack stack) {
		if (!stack.isEmpty()) {
			items.mergeInt(stack.copyWithCount(1), stack.getCount(), Integer::sum);
		}
	}

	/// Drop all the accumulated items at the given position, merging them where possible to reduce the number of
	/// item entities created.
	///
	/// @param level the level
	/// @param pos the position to drop items at
	public void drop(Level level, BlockPos pos) {
		if (items.isEmpty()) {
			return;
		}

		List<ItemStack> stacks = new ArrayList<>();
		items.forEach((stack, remaining) -> {
			while (remaining > 0) {
				int n = Math.min(stack.getMaxStackSize(), remaining);
				stacks.add(stack.copyWithCount(n));
				remaining -= n;
			}
		});

		for (ItemStack stack : stacks) {
			Block.popResource(level, pos, stack);
		}
	}
}