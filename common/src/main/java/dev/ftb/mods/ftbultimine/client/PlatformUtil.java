package dev.ftb.mods.ftbultimine.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import net.minecraft.client.KeyMapping;

public class PlatformUtil {
    @ExpectPlatform
    public static boolean doesKeybindMatch(KeyMapping keyMapping, int keyCode, int scanCode, int modifiers) {
        throw new AssertionError();
    }

    public static boolean doesKeybindMatch(KeyMapping keyMapping, Key key) {
        return doesKeybindMatch(keyMapping, key.keyCode, key.scanCode, key.modifiers.modifiers);
    }
}
