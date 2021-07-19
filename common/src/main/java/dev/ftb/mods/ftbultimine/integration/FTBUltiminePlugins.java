package dev.ftb.mods.ftbultimine.integration;

import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashSet;

public class FTBUltiminePlugins {
	static final Collection<FTBUltiminePlugin> plugins = new HashSet<>();

	public static void init() {
		plugins.forEach(FTBUltiminePlugin::init);
	}

	public static boolean canUltimine(Player player) {
		for (FTBUltiminePlugin plugin : plugins) {
			if(!plugin.canUltimine(player)) {
				return false;
			}
		}
		return true;
	}
}
