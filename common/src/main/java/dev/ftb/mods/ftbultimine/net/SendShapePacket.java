package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class SendShapePacket extends BaseS2CMessage {
	private final int shapeIdx;
	private final List<BlockPos> blocks;

	public SendShapePacket(int idx, List<BlockPos> b) {
		shapeIdx = idx;
		blocks = b;
	}

	public SendShapePacket(FriendlyByteBuf buf) {
		shapeIdx = buf.readVarInt();
		int s = buf.readVarInt();
		blocks = new ArrayList<>(s);

		for (int i = 0; i < s; i++) {
			blocks.add(buf.readBlockPos());
		}
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(shapeIdx);
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
			FTBUltimine.instance.proxy.setShape(shapeIdx, blocks);
		});
	}
}
