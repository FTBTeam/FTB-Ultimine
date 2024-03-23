package dev.ftb.mods.ftbultimine.utils.neoforge;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class ItemUtilsImpl {
	public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
		return ItemHandlerHelper.canItemStacksStack(a, b);
	}

	public static boolean areCompatible(ItemStack a, ItemStack b) {
		return a.areAttachmentsCompatible(b);
	}
}
