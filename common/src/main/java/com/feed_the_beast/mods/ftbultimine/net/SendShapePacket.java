package com.feed_the_beast.mods.ftbultimine.net;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import com.feed_the_beast.mods.ftbultimine.FTBUltimineConfig;
import com.feed_the_beast.mods.ftbultimine.shape.Shape;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class SendShapePacket
{
	public static Shape current = null;

	private final Shape shape;
	private final List<BlockPos> blocks;

	public SendShapePacket(Shape s, List<BlockPos> b)
	{
		shape = s;
		blocks = b;
	}

	public SendShapePacket(FriendlyByteBuf buf)
	{
		shape = Shape.get(buf.readUtf(Short.MAX_VALUE));
		int s = buf.readVarInt();
		blocks = new ArrayList<>(s);

		for (int i = 0; i < s; i++)
		{
			blocks.add(buf.readBlockPos());
		}
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeUtf(shape.getName(), Short.MAX_VALUE);
		buf.writeVarInt(blocks.size());

		for (BlockPos pos : blocks)
		{
			buf.writeBlockPos(pos);
		}
	}

	public void handle(Supplier<NetworkManager.PacketContext> context)
	{
		context.get().queue(() -> {
			current = shape;

			if (FTBUltimineConfig.get().renderOutline)
			{
				FTBUltimine.instance.proxy.setShape(blocks);
			}
		});
	}
}
