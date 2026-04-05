package dev.ftb.mods.ftbultimine.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.event.LevelRenderLastEvent;
import dev.ftb.mods.ftbultimine.registry.ModAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FTBUltimine.MOD_ID)
public class FTBUltimineForge {

	public FTBUltimineForge(FMLJavaModLoadingContext context) {
		EventBuses.registerModEventBus(FTBUltimine.MOD_ID, context.getModEventBus());
//		if (ModList.get().isLoaded("losttrinkets")) {
//			FTBUltiminePlugin.register(new LostTrinketsFTBUltiminePlugin());
//		}

		new FTBUltimine();

		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> FTBUltimineForgeClient::init);

		context.getModEventBus().addListener(this::addPlayerAttributes);
	}

	private void addPlayerAttributes(EntityAttributeModificationEvent event) {
		event.add(EntityType.PLAYER, ModAttributes.MAX_BLOCKS_MODIFIER.get());
		event.add(EntityType.PLAYER, ModAttributes.COOLDOWN_MODIFIER.get());
		event.add(EntityType.PLAYER, ModAttributes.EXHAUSTION_MODIFIER.get());
		event.add(EntityType.PLAYER, ModAttributes.EXPERIENCE_MODIFIER.get());
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
