package com.feed_the_beast.mods.ftbultimine;

import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class BrokenItemHandler implements Consumer<ServerPlayer>
{
	public boolean isBroken = false;

	@Override
	public void accept(ServerPlayer player)
	{
		isBroken = true;
		player.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
	}
}