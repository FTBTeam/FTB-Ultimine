package dev.ftb.mods.ftbultimine.integration;

import net.minecraft.world.entity.player.Player;

public interface FTBUltiminePlugin {

	static FTBUltiminePlugin register(FTBUltiminePlugin plugin) {
		FTBUltiminePlugins.plugins.add(plugin);
		return plugin;
	}

	default void init() {
	}

	default boolean canUltimine(Player player) {
		return true;
	}

}
