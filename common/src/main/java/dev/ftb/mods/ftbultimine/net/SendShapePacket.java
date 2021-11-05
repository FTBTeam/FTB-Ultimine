package dev.ftb.mods.ftbultimine.net;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.shape.Shape;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.simple.BaseS2CMessage;
import me.shedaniel.architectury.networking.simple.MessageType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class SendShapePacket extends BaseS2CMessage {
	public static Shape current = null;

	private final Shape shape;
	private final List<BlockPos> blocks;

	public SendShapePacket(Shape s, List<BlockPos> b) {
		shape = s;
		blocks = b;
	}

	public SendShapePacket(FriendlyByteBuf buf) {
		shape = Shape.get(buf.readUtf(Short.MAX_VALUE));
		int s = buf.readVarInt();
		blocks = new ArrayList<>(s);

		for (int i = 0; i < s; i++) {
			blocks.add(buf.readBlockPos());
		}
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(shape.getName(), Short.MAX_VALUE);
		buf.writeVarInt(blocks.size());

		for (BlockPos pos : blocks) {
			buf.writeBlockPos(pos);
		}
	}

	@Override
	public MessageType getType() {
		return FTBUltimineNet.SEND_SHAPE;
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		context.queue(() -> {
			current = shape;
			FTBUltimine.instance.proxy.setShape(blocks);
		});
	}
}
