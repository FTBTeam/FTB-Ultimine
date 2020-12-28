package com.feed_the_beast.mods.ftbultimine.net;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class KeyPressedPacket
{
	private final boolean pressed;

	public KeyPressedPacket(boolean p)
	{
		pressed = p;
	}

	public KeyPressedPacket(FriendlyByteBuf buf)
	{
		pressed = buf.readBoolean();
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeBoolean(pressed);
	}

	public void handle(Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> FTBUltimine.instance.setKeyPressed(context.get().getSender(), pressed));
		context.get().setPacketHandled(true);
	}
}