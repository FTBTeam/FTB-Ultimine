package dev.ftb.mods.ftbultimine.neoforge.integration.ezcrystals.neoforge;

import dev.ftb.mods.ftbultimine.api.rightclick.RegisterRightClickHandlerEvent;

public class EZCrystals {
    public static void init() {
        RegisterRightClickHandlerEvent.REGISTER.register(d -> d.registerHandler(EZCrystalsHandler.INSTANCE));
    }
}
