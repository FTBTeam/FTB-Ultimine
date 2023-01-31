package dev.ftb.mods.ftbultimine.forge;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.event.LevelRenderLastEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(FTBUltimine.MOD_ID)
public class FTBUltimineForge {

	public FTBUltimineForge() {
//		if (ModList.get().isLoaded("losttrinkets")) {
//			FTBUltiminePlugin.register(new LostTrinketsFTBUltiminePlugin());
//		}

		new FTBUltimine();

		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> FTBUltimineForgeClient::init);
	}

	private static class FTBUltimineForgeClient {
		static void init() {
			MinecraftForge.EVENT_BUS.<RenderLevelStageEvent>addListener(event -> {
				if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
					LevelRenderLastEvent.EVENT.invoker().onRenderLast(event.getPoseStack());
				}
			});
		}
	}
}
