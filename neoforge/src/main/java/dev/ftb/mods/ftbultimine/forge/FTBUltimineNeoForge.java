package dev.ftb.mods.ftbultimine.forge;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(FTBUltimine.MOD_ID)
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
