package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ModeChangedPacket(boolean next) implements CustomPacketPayload {
    public static final Type<ModeChangedPacket> TYPE = new Type<>(FTBUltimineAPI.id("mode_changed_packet"));

    public static final StreamCodec<FriendlyByteBuf, ModeChangedPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ModeChangedPacket::next,
            ModeChangedPacket::new
    );

    @Override
    public Type<ModeChangedPacket> type() {
        return TYPE;
    }

    public static void handle(ModeChangedPacket message, NetworkManager.PacketContext context) {
        context.queue(() -> FTBUltimine.instance.modeChanged((ServerPlayer) context.getPlayer(), message.next));
    }
}