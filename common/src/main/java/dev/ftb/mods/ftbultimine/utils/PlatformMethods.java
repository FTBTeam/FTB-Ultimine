package dev.ftb.mods.ftbultimine.utils;

import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.minecraft.server.level.ServerPlayer;

public class PlatformMethods {
	@ExpectPlatform
	public static double reach(ServerPlayer player) {
		throw new AssertionError();
	}
}
