package dev.ftb.mods.ftbultimine.neoforge.integration;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftbultimine.neoforge.integration.ezcrystals.neoforge.EZCrystals;
import net.neoforged.bus.api.IEventBus;

public class NeoIntegration {
    public static void init(IEventBus modEventBus) {
        if (Platform.isModLoaded("ftbezcrystals")) {
            EZCrystals.init();
        }
    }
}
