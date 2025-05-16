package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbultimine.CooldownTracker;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncUltimineTimePacket(long when, TimeType timetype) implements CustomPacketPayload {
    public static final Type<SyncUltimineTimePacket> TYPE = new Type<>(FTBUltimine.id("sync_ultimine_time_packet"));

    public static final StreamCodec<FriendlyByteBuf, SyncUltimineTimePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, SyncUltimineTimePacket::when,
            NetworkHelper.enumStreamCodec(TimeType.class), SyncUltimineTimePacket::timetype,
            SyncUltimineTimePacket::new
    );

    @Override
    public Type<SyncUltimineTimePacket> type() {
        return TYPE;
    }

    public static void handle(SyncUltimineTimePacket message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            switch (message.timetype) {
                case LAST_USED -> CooldownTracker.setLastUltimineTime(FTBUltimineClient.getClientPlayer(), message.when);
                case COOLDOWN -> CooldownTracker.setClientCooldownTime(message.when);
            }
        });
    }

    public enum TimeType {
        COOLDOWN,
        LAST_USED
    }
}
