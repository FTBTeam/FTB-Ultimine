package com.feed_the_beast.mods.ftbultimine.utils.forge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeMod;

public class PlatformMethodsImpl {
	public static double reach(ServerPlayer player) {
		return player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
	}
}
