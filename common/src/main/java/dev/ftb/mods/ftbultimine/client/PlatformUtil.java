package dev.ftb.mods.ftbultimine.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.ItemStack;

public class PlatformUtil {
    @ExpectPlatform
    public static boolean doesKeybindMatch(KeyMapping keyMapping, int keyCode, int scanCode, int modifiers) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canAxeStrip(ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canTillSoil(ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canFlattenPath(ItemStack stack) {
        throw new AssertionError();
    }

    public static boolean doesKeybindMatch(KeyMapping keyMapping, Key key) {
        return doesKeybindMatch(keyMapping, key.keyCode, key.scanCode, key.modifiers.modifiers);
    }
}
