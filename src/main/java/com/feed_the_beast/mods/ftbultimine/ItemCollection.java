package com.feed_the_beast.mods.ftbultimine;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author LatvianModder
 */
public class ItemCollection
{
	private final List<ItemStack> items = new ArrayList<>();

	public void add(ItemStack is)
	{
		if (!is.isEmpty())
		{
			items.add(is);
		}
	}

	public void drop(World world, BlockPos pos)
	{
		if (items.isEmpty())
		{
			return;
		}

		List<ItemStack> stacks = new ArrayList<>();

		for (ItemStack stack : items)
		{
			if (!stack.isStackable())
			{
				stacks.add(stack);
				continue;
			}

			int sizeInventory = stacks.size();

			// go through the inventory and try to fill up already existing items
			for (int i = 0; i < sizeInventory; i++)
			{
				stack = insert(stacks, stack, i);

				if (stack.isEmpty())
				{
					break;
				}
			}

			if (!stack.isEmpty())
			{
				stacks.add(stack);
			}

			//Block.spawnAsEntity(world, pos, ItemHandlerHelper.insertItemStacked(handler, stack, false));
		}

		for (ItemStack stack : stacks)
		{
			Block.spawnAsEntity(world, pos, stack);
		}
	}

	private ItemStack insert(List<ItemStack> stacks, ItemStack stack, int slot)
	{
		ItemStack existing = stacks.get(slot);

		if (stack.isEmpty() || existing.isEmpty() || stack.getItem() != existing.getItem())
		{
			return stack;
		}

		if (!stack.isStackable() || !Objects.equals(stack.getTag(), existing.getTag()) || !stack.areCapsCompatible(existing))
		{
			return stack;
		}

		int limit = stack.getMaxStackSize();

		if (!existing.isEmpty())
		{
			if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
			{
				return stack;
			}

			limit -= existing.getCount();
		}

		if (limit <= 0)
		{
			return stack;
		}

		boolean reachedLimit = stack.getCount() > limit;

		if (existing.isEmpty())
		{
			stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
		}
		else
		{
			existing.grow(reachedLimit ? limit : stack.getCount());
		}

		return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
	}
}