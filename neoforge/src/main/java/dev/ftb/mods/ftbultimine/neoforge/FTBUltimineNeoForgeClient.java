package dev.ftb.mods.ftbultimine.neoforge;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = FTBUltimineAPI.MOD_ID, dist = Dist.CLIENT)
public class FTBUltimineNeoForgeClient {

    public FTBUltimineNeoForgeClient(IEventBus modBus) {
        var client = new FTBUltimineClient();

        var bus = NeoForge.EVENT_BUS;

        modBus.addListener(RegisterGuiLayersEvent.class, event ->
                event.registerAbove(VanillaGuiLayers.SUBTITLE_OVERLAY, FTBUltimineClient.GUI_OVERLAY_ID, this::renderGuiOverlay));

        bus.addListener(ClientStartedEvent.class, _ -> FTBUltimine.commonSetup(true));
        bus.addListener(ClientTickEvent.Pre.class, _ -> client.clientTick(Minecraft.getInstance()));
        bus.addListener(RenderLevelStageEvent.AfterTranslucentBlocks.class,
                event -> client.renderInGame());
        bus.addListener(InputEvent.MouseScrollingEvent.class, event -> {
            if (client.onMouseScrolled(event.getScrollDeltaX(), event.getScrollDeltaY())) {
                event.setCanceled(true);
            }
        });
        bus.addListener(InputEvent.Key.class, _ -> client.onKeyPress());
    }

    private void renderGuiOverlay(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        FTBUltimineClient.getInstance().renderGameOverlay(guiGraphics, deltaTracker);
    }
}
