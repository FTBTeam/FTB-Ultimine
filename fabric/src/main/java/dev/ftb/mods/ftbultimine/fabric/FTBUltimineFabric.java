package dev.ftb.mods.ftbultimine.fabric;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.event.LevelRenderLastEvent;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class FTBUltimineFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		new FTBUltimine();
		EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
			WorldRenderEvents.AFTER_TRANSLUCENT.register((ctx) -> LevelRenderLastEvent.EVENT.invoker().onRenderLast(ctx.matrixStack()));
		});
	}
}
