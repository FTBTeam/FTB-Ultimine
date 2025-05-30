package dev.ftb.mods.ftbultimine.integration.ezcrystals;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class EZCrystals {
    @ExpectPlatform
    public static void init() {
        // NeoForge-only mod
        throw new AssertionError();
    }
}
