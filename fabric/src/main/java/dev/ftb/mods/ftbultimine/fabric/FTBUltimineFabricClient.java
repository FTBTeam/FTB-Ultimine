package dev.ftb.mods.ftbultimine.fabric;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class FTBUltimineFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FTBUltimineClient client = new FTBUltimineClient();

        ClientLifecycleEvents.CLIENT_STARTED.register(_ -> FTBUltimine.commonSetup(true));
        ClientTickEvents.START_CLIENT_TICK.register(client::clientTick);

        HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, FTBUltimineClient.GUI_OVERLAY_ID, client::renderGameOverlay);
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> client.renderInGame());

        // see KeyboardHandlerMixin and MouseHandlerMixin for key/mouse "events"
    }
}
