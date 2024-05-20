package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record SendShapePacket(int shapeIdx, List<BlockPos> blocks) implements CustomPacketPayload {
	public static final Type<SendShapePacket> TYPE = new Type<>(FTBUltimine.rl("send_shape_packet"));

	public static final StreamCodec<FriendlyByteBuf, SendShapePacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SendShapePacket::shapeIdx,
			BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), SendShapePacket::blocks,
			SendShapePacket::new
	);

	@Override
	public Type<SendShapePacket> type() {
		return TYPE;
	}

	public static void handle(SendShapePacket message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			FTBUltimine.instance.proxy.setShape(message.shapeIdx, message.blocks);
		});
	}
}
