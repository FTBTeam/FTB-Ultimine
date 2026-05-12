package dev.ftb.mods.ftbultimine.fabric;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.fabricmc.api.ModInitializer;

public class FTBUltimineFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		new FTBUltimine();
	}
}
