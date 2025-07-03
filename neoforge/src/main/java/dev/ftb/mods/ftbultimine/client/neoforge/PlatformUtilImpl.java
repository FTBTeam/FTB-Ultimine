package dev.ftb.mods.ftbultimine.client.neoforge;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;
import org.lwjgl.glfw.GLFW;

public class PlatformUtilImpl {
    public static boolean doesKeybindMatch(KeyMapping keyMapping, int keyCode, int scanCode, int modifiers) {
        if (keyMapping.matches(keyCode, scanCode)) {
            return switch (keyMapping.getKeyModifier()) {
                case NONE -> modifiers == 0;
                case SHIFT ->  (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
                case CONTROL ->  (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
                case ALT ->  (modifiers & GLFW.GLFW_MOD_ALT) != 0;
            };
        }
        return false;
    }

    public static boolean canAxeStrip(ItemStack stack) {
        return stack.getItem().canPerformAction(stack, ItemAbilities.AXE_STRIP);
    }

    public static boolean canTillSoil(ItemStack stack) {
        return stack.getItem().canPerformAction(stack, ItemAbilities.HOE_TILL);
    }

    public static boolean canFlattenPath(ItemStack stack) {
        return stack.getItem().canPerformAction(stack, ItemAbilities.SHOVEL_FLATTEN);
    }
}
