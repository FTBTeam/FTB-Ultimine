package dev.ftb.mods.ftbultimine.integration;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftbultimine.integration.agricraft.AgriCraftCropLikeHandler;

public class IntegrationHandler {
    public static boolean ranksMod = false;

    public static void init() {
        if (Platform.isModLoaded("ftbranks")) {
            ranksMod = true;
            FTBRanksIntegration.init();
        }

        if (Platform.isModLoaded("agricraft")) {
            AgriCraftCropLikeHandler.init();
        }
    }
}
