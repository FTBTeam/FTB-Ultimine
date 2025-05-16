package dev.ftb.mods.ftbultimine.integration.agricraft;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.crop.RegisterCropLikeEvent;

public class AgricraftSetup {
    public static void registerHandler(RegisterCropLikeEvent.Dispatcher dispatcher) {
        dispatcher.registerHandler(AgriCraftCropLikeHandler.INSTANCE);
        FTBUltimine.LOGGER.info("Agricraft detected, registered Agricraft crop harvest handler");
    }
}
