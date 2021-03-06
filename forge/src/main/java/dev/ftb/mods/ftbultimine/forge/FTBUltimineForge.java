package dev.ftb.mods.ftbultimine.forge;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.config.FTBUltimineConfig;
import dev.ftb.mods.ftbultimine.event.LevelRenderLastEvent;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(FTBUltimine.MOD_ID)
public class FTBUltimineForge {

	public FTBUltimineForge() {
		new FTBUltimine();

		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> FTBUltimineForgeClient::init);
	}

	private static class FTBUltimineForgeClient {
		static void init() {
			MinecraftForge.EVENT_BUS.<RenderWorldLastEvent>addListener(event -> {
				LevelRenderLastEvent.EVENT.invoker().onRenderLast(event.getMatrixStack());
			});

			ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (client, parent) -> {
				return AutoConfig.getConfigScreen(FTBUltimineConfig.class, parent).get();
			});
		}
	}

}
