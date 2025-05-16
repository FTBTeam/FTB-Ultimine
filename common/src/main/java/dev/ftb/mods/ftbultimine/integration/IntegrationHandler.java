package dev.ftb.mods.ftbultimine.integration;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftbultimine.api.crop.RegisterCropLikeEvent;
import dev.ftb.mods.ftbultimine.integration.agricraft.AgricraftSetup;
import dev.ftb.mods.ftbultimine.integration.ranks.FTBRanksIntegration;

public class IntegrationHandler {
    public static boolean ranksMod = false;

    public static void init() {
        if (Platform.isModLoaded("ftbranks")) {
            ranksMod = true;
            FTBRanksIntegration.init();
        }

        // TODO investigate if this is really desired behaviour
//        if (Platform.isModLoaded("farmersdelight")) {
//            // budding FD crops are actually bushes, not crops,
//            // but we want them to be treated as a crop for ultimine purposes
//            CropLikeTypeMatcher.registerType(ResourceLocation.fromNamespaceAndPath("farmersdelight", "budding_tomatoes"), CropLikeTypeMatcher.Type.CROP);
//        }

        if (Platform.isModLoaded("agricraft")) {
            RegisterCropLikeEvent.REGISTER.register(AgricraftSetup::registerHandler);
        }
    }
}
