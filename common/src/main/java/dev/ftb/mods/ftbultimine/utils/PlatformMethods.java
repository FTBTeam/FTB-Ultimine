package dev.ftb.mods.ftbultimine.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.level.ServerPlayer;

public class PlatformMethods {
	@ExpectPlatform
	public static double reach(ServerPlayer player) {
		throw new AssertionError();
	}
}
