package dev.ftb.mods.ftbultimine.forge.plugin.losttrinkets;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.config.FTBUltimineCommonConfig;
import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import owmii.losttrinkets.api.LostTrinketsAPI;

public class LostTrinketsFTBUltiminePlugin implements FTBUltiminePlugin {

	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Item.class, FTBUltimine.MOD_ID);

	private static RegistryObject<UltiminerTrinket> TRINKET;

	public LostTrinketsFTBUltiminePlugin() {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	@Override
	public void init() {
		if (!FTBUltimineCommonConfig.USE_TRINKET.get()) {
			return;
		}
		TRINKET = ITEMS.register("ultiminer", UltiminerTrinket::new);
	}

	@Override
	public boolean canUltimine(Player player) {
		return !FTBUltimineCommonConfig.USE_TRINKET.get() || LostTrinketsAPI.getTrinkets(player).isActive(TRINKET.get());
	}
}
