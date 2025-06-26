package dev.ftb.mods.ftbultimine.utils.fabric;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.server.level.ServerPlayer;

public class PlatformMethodsImpl {
	public static double reach(ServerPlayer player) {
		return ReachEntityAttributes.getReachDistance(player, player.isCreative() ? 5.0 : 4.5);
	}
}