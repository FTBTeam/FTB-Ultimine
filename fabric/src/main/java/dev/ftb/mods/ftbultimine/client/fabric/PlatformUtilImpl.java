package dev.ftb.mods.ftbultimine.client.fabric;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;

public class PlatformUtilImpl {
    public static boolean doesKeybindMatch(KeyMapping keyMapping, int keyCode, int scanCode, int modifiers) {
        // TODO how can we handle key modifiers on Fabric?
        return keyMapping.matches(keyCode, scanCode);
    }

    public static boolean canAxeStrip(ItemStack stack) {
        return stack.getItem() instanceof AxeItem;
    }

    public static boolean canTillSoil(ItemStack stack) {
        return stack.getItem() instanceof HoeItem;
    }

    public static boolean canFlattenPath(ItemStack stack) {
        return stack.getItem() instanceof ShovelItem;
    }
}
