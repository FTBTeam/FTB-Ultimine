package com.feed_the_beast.mods.ftbultimine.net;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class ModeChangedPacket
{
	public ModeChangedPacket()
	{
	}

	public ModeChangedPacket(PacketBuffer buf)
	{
	}

	public void write(PacketBuffer buf)
	{
	}

	public void handle(Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> FTBUltimine.instance.modeChanged(context.get().getSender()));
		context.get().setPacketHandled(true);
	}
}