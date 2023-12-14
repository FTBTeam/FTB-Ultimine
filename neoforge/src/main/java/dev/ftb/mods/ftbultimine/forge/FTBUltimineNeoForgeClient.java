package dev.ftb.mods.ftbultimine.forge;

import dev.ftb.mods.ftbultimine.event.LevelRenderLastEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

class FTBUltimineNeoForgeClient {
    static void init() {
        NeoForge.EVENT_BUS.<RenderLevelStageEvent>addListener(event -> {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                LevelRenderLastEvent.EVENT.invoker().onRenderLast(event.getPoseStack());
            }
        });
    }
}
