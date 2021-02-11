package com.feed_the_beast.mods.ftbultimine.utils.forge;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemUtilsImpl
{
	public static boolean canItemStacksStack(ItemStack a, ItemStack b)
	{
		return ItemHandlerHelper.canItemStacksStack(a, b);
	}

	public static ItemStack copyStackWithSize(ItemStack itemStack, int size)
	{
		return ItemHandlerHelper.copyStackWithSize(itemStack, size);
	}

	public static boolean areCompatible(ItemStack a, ItemStack b)
	{
		return a.areCapsCompatible(b);
	}
}
