package dev.ftb.mods.ftbultimine;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.function.Consumer;

public class BrokenItemHandler implements Runnable {
	private final ServerPlayer player;
	public boolean isBroken = false;

	public BrokenItemHandler(ServerPlayer player) {
		this.player = player;
	}

    @Override
	public void run() {
		isBroken = true;
		player.broadcastBreakEvent(EquipmentSlot.MAINHAND);
	}
}