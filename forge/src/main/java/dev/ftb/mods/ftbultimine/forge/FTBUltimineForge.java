package dev.ftb.mods.ftbultimine.forge;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.event.LevelRenderLastEvent;
import dev.ftb.mods.ftbultimine.forge.plugin.losttrinkets.LostTrinketsFTBUltiminePlugin;
import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod(FTBUltimine.MOD_ID)
public class FTBUltimineForge {

	public FTBUltimineForge() {
		if (ModList.get().isLoaded("losttrinkets")) {
			FTBUltiminePlugin.register(new LostTrinketsFTBUltiminePlugin());
		}

		new FTBUltimine();

		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> FTBUltimineForgeClient::init);
	}

	private static class FTBUltimineForgeClient {
		static void init() {
			MinecraftForge.EVENT_BUS.<RenderWorldLastEvent>addListener(event -> {
				LevelRenderLastEvent.EVENT.invoker().onRenderLast(event.getMatrixStack());
			});
		}
	}

}
