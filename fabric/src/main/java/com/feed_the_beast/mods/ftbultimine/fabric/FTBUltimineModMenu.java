package com.feed_the_beast.mods.ftbultimine.fabric;

import com.feed_the_beast.mods.ftbultimine.FTBUltimineConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class FTBUltimineModMenu implements ModMenuApi
{
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return parent -> AutoConfig.getConfigScreen(FTBUltimineConfig.class, parent).get();
	}
}
