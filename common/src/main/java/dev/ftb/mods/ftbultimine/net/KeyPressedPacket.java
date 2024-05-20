package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record KeyPressedPacket(boolean pressed) implements CustomPacketPayload {
    public static final Type<KeyPressedPacket> TYPE = new Type<>(FTBUltimine.rl("key_pressed_packet"));

    public static final StreamCodec<FriendlyByteBuf, KeyPressedPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, KeyPressedPacket::pressed,
            KeyPressedPacket::new
    );

    @Override
    public Type<KeyPressedPacket> type() {
        return TYPE;
    }

    public static void handle(KeyPressedPacket message, NetworkManager.PacketContext context) {
        context.queue(() -> FTBUltimine.instance.setKeyPressed((ServerPlayer) context.getPlayer(), message.pressed));
    }
}