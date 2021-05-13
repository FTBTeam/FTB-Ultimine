package dev.ftb.mods.ftbultimine.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.ftb.mods.ftbultimine.config.FTBUltimineConfig;
import me.shedaniel.autoconfig.AutoConfig;

public class FTBUltimineModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> AutoConfig.getConfigScreen(FTBUltimineConfig.class, parent).get();
	}
}
