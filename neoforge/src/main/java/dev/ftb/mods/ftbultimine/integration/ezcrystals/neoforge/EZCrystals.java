package dev.ftb.mods.ftbultimine.integration.ezcrystals.neoforge;

import dev.ftb.mods.ftbultimine.api.neoforge.FTBUltimineEvent;
import net.neoforged.bus.api.IEventBus;

public class EZCrystals {
    public static void init(IEventBus bus) {
        //bus.addListener(FTBUltimineEvent.RegisterRightClickHandler.class, event -> event.getEventData().registerHandler(EZCrystalsHandler.INSTANCE));
    }
}
