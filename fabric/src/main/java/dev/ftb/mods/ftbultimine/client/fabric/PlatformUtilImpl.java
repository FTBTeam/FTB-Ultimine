package dev.ftb.mods.ftbultimine.client.fabric;

import net.minecraft.client.KeyMapping;

public class PlatformUtilImpl {
    public static boolean doesKeybindMatch(KeyMapping keyMapping, int keyCode, int scanCode, int modifiers) {
        // TODO how can we handle key modifiers on Fabric?
        return keyMapping.matches(keyCode, scanCode);
    }
}
