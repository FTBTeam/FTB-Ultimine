package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.net.SyncUltimineTimePacket.TimeType;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public record SyncConfigToServerPacket(SNBTCompoundTag config) implements CustomPacketPayload {
    public static final Type<SyncConfigToServerPacket> TYPE = new Type<>(FTBUltimine.rl("sync_config_to_server_packet"));

    public static final StreamCodec<FriendlyByteBuf, SyncConfigToServerPacket> STREAM_CODEC = StreamCodec.composite(
            SNBTCompoundTag.STREAM_CODEC, SyncConfigToServerPacket::config,
            SyncConfigToServerPacket::new
    );

    @Override
    public Type<SyncConfigToServerPacket> type() {
        return TYPE;
    }

    public static void handle(SyncConfigToServerPacket message, NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayer sp && sp.hasPermissions(Commands.LEVEL_GAMEMASTERS)) {
            context.queue(() -> {
                MinecraftServer server = sp.getServer();

                FTBUltimine.LOGGER.info("Server config updated from client by player {}", sp.getName().getString());
                FTBUltimineServerConfig.CONFIG.read(message.config);
                FTBUltimineServerConfig.clearTagCache();

                Path file = server.getWorldPath(ConfigUtil.SERVER_CONFIG_DIR).resolve(FTBUltimineServerConfig.CONFIG.key + ".snbt");
                FTBUltimineServerConfig.CONFIG.save(file);

                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (!sp.getUUID().equals(player.getUUID())) {
                        NetworkManager.sendToPlayer(player, new SyncConfigFromServerPacket(message.config));
                    }
                    NetworkManager.sendToPlayer(player, new SyncUltimineTimePacket(FTBUltimineServerConfig.getUltimineCooldown(player), TimeType.COOLDOWN));
                }
            });
        }
    }
}
