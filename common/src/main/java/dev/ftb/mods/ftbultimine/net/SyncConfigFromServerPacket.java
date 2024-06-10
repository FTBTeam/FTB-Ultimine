package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncConfigFromServerPacket(SNBTCompoundTag config) implements CustomPacketPayload {
    public static final Type<SyncConfigFromServerPacket> TYPE = new Type<>(FTBUltimine.rl("sync_config_from_server_packet"));

    public static final StreamCodec<FriendlyByteBuf, SyncConfigFromServerPacket> STREAM_CODEC = StreamCodec.composite(
            SNBTCompoundTag.STREAM_CODEC, SyncConfigFromServerPacket::config,
            SyncConfigFromServerPacket::new
    );

    @Override
    public Type<SyncConfigFromServerPacket> type() {
        return TYPE;
    }

    public static void handle(SyncConfigFromServerPacket message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            FTBUltimineServerConfig.CONFIG.read(message.config);
            FTBUltimine.LOGGER.info("received server config settings");
        });
    }
}
