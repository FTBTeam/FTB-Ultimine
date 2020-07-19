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
	public final boolean next;

	public ModeChangedPacket(boolean n)
	{
		next = n;
	}

	public ModeChangedPacket(PacketBuffer buf)
	{
		next = buf.readBoolean();
	}

	public void write(PacketBuffer buf)
	{
		buf.writeBoolean(next);
	}

	public void handle(Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> FTBUltimine.instance.modeChanged(context.get().getSender(), next));
		context.get().setPacketHandled(true);
	}
}