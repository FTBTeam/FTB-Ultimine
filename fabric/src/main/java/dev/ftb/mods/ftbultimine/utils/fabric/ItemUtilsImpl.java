package dev.ftb.mods.ftbultimine.utils.fabric;

import net.minecraft.world.item.ItemStack;

public class ItemUtilsImpl {
	public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
		if (!a.isEmpty() && !b.isEmpty() && a.getItem() == b.getItem()) {
			if (!a.isStackable()) {
				return false;
			} else if (a.hasTag() != b.hasTag()) {
				return false;
			} else {
				return (!a.hasTag() || a.getTag().equals(b.getTag())) && areCompatible(a, b);
			}
		} else {
			return false;
		}
	}

	public static boolean areCompatible(ItemStack a, ItemStack b) {
		return true;
	}
}
