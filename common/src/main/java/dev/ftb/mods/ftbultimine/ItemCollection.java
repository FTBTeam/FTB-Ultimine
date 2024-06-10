package dev.ftb.mods.ftbultimine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class ItemCollection {
	private final List<ItemStack> items = new ArrayList<>();

	public void add(ItemStack is) {
		if (!is.isEmpty()) {
			items.add(is.copy());
		}
	}

	public void drop(Level world, BlockPos pos) {
		if (items.isEmpty()) {
			return;
		}

		List<ItemStack> stacks = new ArrayList<>();

		for (ItemStack stack : items) {
			if (!stack.isStackable()) {
				stacks.add(stack);
				continue;
			}

			int sizeInventory = stacks.size();

			// go through the inventory and try to fill up already existing items
			for (int i = 0; i < sizeInventory; i++) {
				stack = insert(stacks, stack, i);

				if (stack.isEmpty()) {
					break;
				}
			}

			if (!stack.isEmpty()) {
				stacks.add(stack);
			}

			//Block.spawnAsEntity(world, pos, ItemHandlerHelper.insertItemStacked(handler, stack, false));
		}

		for (ItemStack stack : stacks) {
			Block.popResource(world, pos, stack);
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

//		if (!existing.isEmpty()) {
//			if (!ItemUtils.canItemStacksStack(stack, existing)) {
//				return stack;
//			}
//
//			limit -= existing.getCount();
//		}
//
//		if (limit <= 0) {
//			return stack;
//		}
//
//		boolean reachedLimit = stack.getCount() > limit;
//
//		if (existing.isEmpty()) {
//			stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
//		} else {
//			existing.grow(reachedLimit ? limit : stack.getCount());
//		}
//
//		return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
	}
}