package dev.ftb.mods.ftbultimine.utils.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForgeMod;

public class PlatformMethodsImpl {
	public static double reach(ServerPlayer player) {
		return player.getAttributeValue(NeoForgeMod.BLOCK_REACH.value());
	}
}
