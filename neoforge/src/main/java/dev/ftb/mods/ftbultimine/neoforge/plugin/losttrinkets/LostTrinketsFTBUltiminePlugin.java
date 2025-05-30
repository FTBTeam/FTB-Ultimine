package dev.ftb.mods.ftbultimine.neoforge.plugin.losttrinkets;

import dev.ftb.mods.ftbultimine.api.restriction.RestrictionHandler;
import net.minecraft.world.entity.player.Player;

public class LostTrinketsFTBUltiminePlugin implements RestrictionHandler {
    @Override
    public boolean canUltimine(Player player) {
        return true;
    }

//	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Item.class, FTBUltimine.MOD_ID);

//	private static RegistryObject<UltiminerTrinket> TRINKET;

//	public LostTrinketsFTBUltiminePlugin() {
//		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
//	}

//	@Override
//	public void init() {
//		if (!FTBUltimineCommonConfig.USE_TRINKET.get()) {
//			return;
//		}
//		TRINKET = ITEMS.register("ultiminer", UltiminerTrinket::new);
//	}

//	@Override
//	public boolean canUltimine(Player player) {
//		return !FTBUltimineCommonConfig.USE_TRINKET.get() || LostTrinketsAPI.getTrinkets(player).isActive(TRINKET.get());
//	}
}
