package dev.ftb.mods.ftbultimine;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class BrokenItemHandler implements Consumer<ServerPlayer> {
	public boolean isBroken = false;

	@Override
	public void accept(ServerPlayer player) {
		isBroken = true;
		player.broadcastBreakEvent(EquipmentSlot.MAINHAND);
	}
}