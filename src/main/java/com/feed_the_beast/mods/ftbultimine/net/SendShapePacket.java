package com.feed_the_beast.mods.ftbultimine.net;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class SendShapePacket
{
	private final List<BlockPos> blocks;

	public SendShapePacket(List<BlockPos> b)
	{
		blocks = b;
	}

	public SendShapePacket(PacketBuffer buf)
	{
		int s = buf.readVarInt();
		blocks = new ArrayList<>(s);

		for (int i = 0; i < s; i++)
		{
			blocks.add(buf.readBlockPos());
		}
	}

	public void write(PacketBuffer buf)
	{
		buf.writeVarInt(blocks.size());

		for (BlockPos pos : blocks)
		{
			buf.writeBlockPos(pos);
		}
	}

	public void handle(Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> FTBUltimine.instance.proxy.setShape(blocks));
		context.get().setPacketHandled(true);
	}
}