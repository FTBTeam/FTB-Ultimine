package com.feed_the_beast.mods.ftbultimine;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class BrokenItemHandler implements Consumer<ServerPlayerEntity>
{
	public boolean isBroken = false;

	@Override
	public void accept(ServerPlayerEntity player)
	{
		isBroken = true;
		player.sendBreakAnimation(EquipmentSlotType.MAINHAND);
	}
}