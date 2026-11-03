package dev.ftb.mods.ftbultimine.api.util;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for collecting items dropped during a multi-block ultimining operation.
 */
public class ItemCollector {
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

	private final List<ItemStack> items = new ArrayList<>();

	public void clear() {
		items.clear();
	}

	/**
	 * Add an item to the collection.
	 *
	 * @param stack the itemstack to add
	 */
	public void add(ItemStack stack) {
		if (!stack.isEmpty()) {
			items.add(stack.copy());
		}
	}

	/**
	 * Drop all the accumulated items at the given position, merging them where possible to reduce the number of
	 * item entities created.
	 *
	 * @param level the level
	 * @param pos the position to drop items at
	 */
	public void drop(Level level, BlockPos pos) {
		if (items.isEmpty()) {
			return;
		}

		List<ItemStack> stacks = new ArrayList<>();
		// total counts of stackable items, keyed by item type + components (count is ignored by the strategy)
		Object2IntMap<ItemStack> totals = new Object2IntOpenCustomHashMap<>(TYPE_AND_COMPONENTS);

		for (ItemStack stack : items) {
			if (stack.isStackable()) {
				totals.mergeInt(stack.copyWithCount(1), stack.getCount(), Integer::sum);
			} else {
				stacks.add(stack);
			}
		}

		totals.forEach((key, remaining) -> {
			while (remaining > 0) {
				int n = Math.min(key.getMaxStackSize(), remaining);
				stacks.add(key.copyWithCount(n));
				remaining -= n;
			}
		});

		for (ItemStack stack : stacks) {
			Block.popResource(level, pos, stack);
		}
	}
}
