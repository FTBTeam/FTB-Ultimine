package dev.ftb.mods.ftbultimine.forge.plugin.losttrinkets;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
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

	private static final BooleanValue useTrinket = FTBUltimineCommonConfig.CONFIG.getBoolean("use_trinket", false)
			.comment("(This only works if the mod 'Lost Trinkets' is installed!)",
					"Adds a custom 'Ultiminer' trinket players will need to activate to be able to use Ultimine",
					"Make sure you disable the 'Octopick' trinket if this is enabled!");

	public LostTrinketsFTBUltiminePlugin() {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	@Override
	public void init() {
		if (!useTrinket.get()) {
			return;
		}
		TRINKET = ITEMS.register("ultiminer", UltiminerTrinket::new);
	}

	@Override
	public boolean canUltimine(Player player) {
		return !useTrinket.get() || LostTrinketsAPI.getTrinkets(player).isActive(TRINKET.get());
	}
}
