package dev.ftb.mods.ftbultimine.neoforge;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(FTBUltimineAPI.MOD_ID)
public class FTBUltimineNeoForge {
	public FTBUltimineNeoForge() {
//		if (ModList.get().isLoaded("losttrinkets")) {
//			FTBUltiminePlugin.register(new LostTrinketsFTBUltiminePlugin());
//		}

		new FTBUltimine();

		if (FMLEnvironment.dist == Dist.CLIENT) {
			FTBUltimineNeoForgeClient.init();
		}
	}
}
