package dev.ftb.mods.ftbultimine.integration.ezcrystals.neoforge;

import dev.ftb.mods.ftbultimine.api.rightclick.RegisterRightClickHandlerEvent;

public class EZCrystalsImpl {
    public static void init() {
        RegisterRightClickHandlerEvent.REGISTER.register(d -> d.registerHandler(EZCrystalsHandler.INSTANCE));
    }
}
