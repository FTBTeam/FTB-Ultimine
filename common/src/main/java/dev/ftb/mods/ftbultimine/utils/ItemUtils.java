package dev.ftb.mods.ftbultimine.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.ItemStack;

public class ItemUtils {
	@ExpectPlatform
	public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static ItemStack copyStackWithSize(ItemStack itemStack, int size) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static boolean areCompatible(ItemStack a, ItemStack b) {
		throw new AssertionError();
	}
}
