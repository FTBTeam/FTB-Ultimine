package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.SNBTNet;
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public class SyncConfigToServerPacket extends BaseC2SMessage {
    private final SNBTCompoundTag config;

    public SyncConfigToServerPacket(SNBTCompoundTag config) {
        this.config = config;
    }

    public SyncConfigToServerPacket(FriendlyByteBuf buf) {
        config = SNBTNet.readCompound(buf);
    }

    @Override
    public MessageType getType() {
        return FTBUltimineNet.SYNC_CONFIG_TO_SERVER;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        SNBTNet.writeCompound(buf, config);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayer sp && sp.hasPermissions(2)) {
            MinecraftServer server = sp.getServer();

            FTBUltimine.LOGGER.info("Server config updated from client by player {}", sp.getName().getString());
            FTBUltimineServerConfig.CONFIG.read(config);
            FTBUltimineServerConfig.clearTagCache();

            Path file = server.getWorldPath(ConfigUtil.SERVER_CONFIG_DIR).resolve(FTBUltimineServerConfig.CONFIG.key + ".snbt");
            FTBUltimineServerConfig.CONFIG.save(file);

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!sp.getUUID().equals(player.getUUID())) {
                    new SyncConfigFromServerPacket(config).sendTo(player);
                }
            }
        }
    }
}
