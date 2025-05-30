package dev.ftb.mods.ftbultimine.api.util;

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
	private final List<ItemStack> items = new ArrayList<>();

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

		for (ItemStack stack : items) {
			if (!stack.isStackable()) {
				stacks.add(stack);
				continue;
			}

			// go through the inventory and try to fill up already existing items
			for (int i = 0; i < stacks.size(); i++) {
				stack = insert(stacks, stack, i);
				if (stack.isEmpty()) {
					break;
				}
			}

			if (!stack.isEmpty()) {
				stacks.add(stack);
			}
		}

		for (ItemStack stack : stacks) {
			Block.popResource(level, pos, stack);
		}
	}

	private ItemStack insert(List<ItemStack> stacks, ItemStack stack, int slot) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;  // degenerate case
		}

		ItemStack existing = stacks.get(slot);

		if (existing.isEmpty()) {
			// trivial case, just put the stack in the slot
			stacks.set(slot, stack.copy());
			return ItemStack.EMPTY;
		}

		if (!stack.isStackable() || !ItemStack.isSameItemSameComponents(stack, existing)) {
			// stack doesn't fit here
			return stack;
		}

		// the slot is not empty, and is compatible with the stack to be inserted
		// - so at least some of it can be inserted
		int available = stack.getMaxStackSize() - existing.getCount();
		int toAdd = Math.min(available, stack.getCount());
		existing.grow(toAdd);

		return toAdd == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - toAdd);
	}
}