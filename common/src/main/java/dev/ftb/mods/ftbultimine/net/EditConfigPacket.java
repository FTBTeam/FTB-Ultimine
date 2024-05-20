package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EditConfigPacket(boolean isClientConfig) implements CustomPacketPayload {
    public static final Type<EditConfigPacket> TYPE = new Type<>(FTBUltimine.rl("edit_config_packet"));

    public static final StreamCodec<FriendlyByteBuf, EditConfigPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EditConfigPacket::isClientConfig,
            EditConfigPacket::new
    );

    @Override
    public Type<EditConfigPacket> type() {
        return TYPE;
    }

    public static void handle(EditConfigPacket message, NetworkManager.PacketContext context) {
        context.queue(() -> FTBUltimine.instance.proxy.editConfig(message.isClientConfig));
    }
}
