package dev.ftb.mods.ftbultimine.neoforge;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import dev.ftb.mods.ftbultimine.registry.ModAttributes;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@Mod(FTBUltimineAPI.MOD_ID)
public class FTBUltimineNeoForge {
	public FTBUltimineNeoForge(IEventBus modEventBus) {
//		if (ModList.get().isLoaded("losttrinkets")) {
//			FTBUltiminePlugin.register(new LostTrinketsFTBUltiminePlugin());
//		}

		new FTBUltimine();

		modEventBus.addListener(this::addPlayerAttributes);
	}

	private void addPlayerAttributes(EntityAttributeModificationEvent event) {
		event.add(EntityType.PLAYER, ModAttributes.FixedHolder.MAX_BLOCKS_MODIFIER.get());
		event.add(EntityType.PLAYER, ModAttributes.FixedHolder.COOLDOWN_MODIFIER.get());
		event.add(EntityType.PLAYER, ModAttributes.FixedHolder.EXHAUSTION_MODIFIER.get());
		event.add(EntityType.PLAYER, ModAttributes.FixedHolder.EXPERIENCE_MODIFIER.get());
	}
}
