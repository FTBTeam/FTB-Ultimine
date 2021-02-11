package com.feed_the_beast.mods.ftbultimine.fabric;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import com.feed_the_beast.mods.ftbultimine.event.LevelRenderLastEvent;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class FTBUltimineFabric implements ModInitializer
{
	@Override
	public void onInitialize()
	{
		new FTBUltimine();
		EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
			WorldRenderEvents.AFTER_TRANSLUCENT.register((ctx) -> LevelRenderLastEvent.EVENT.invoker().onRenderLast(ctx.matrixStack()));
		});
	}
}
