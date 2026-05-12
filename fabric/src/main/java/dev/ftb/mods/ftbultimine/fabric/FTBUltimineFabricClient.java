package dev.ftb.mods.ftbultimine.fabric;

import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.event.LevelRenderLastEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class FTBUltimineFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new FTBUltimineClient();

        WorldRenderEvents.AFTER_TRANSLUCENT.register((ctx) -> LevelRenderLastEvent.EVENT.invoker().onRenderLast(ctx.matrixStack()));
    }
}
