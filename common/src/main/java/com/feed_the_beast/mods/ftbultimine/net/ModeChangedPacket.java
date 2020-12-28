package com.feed_the_beast.mods.ftbultimine.net;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class ModeChangedPacket
{
	public final boolean next;

	public ModeChangedPacket(boolean n)
	{
		next = n;
	}

	public ModeChangedPacket(FriendlyByteBuf buf)
	{
		next = buf.readBoolean();
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeBoolean(next);
	}

	public void handle(Supplier<NetworkManager.PacketContext> context)
	{
		context.get().queue(() -> FTBUltimine.instance.modeChanged((ServerPlayer) context.get().getPlayer(), next));
	}
}