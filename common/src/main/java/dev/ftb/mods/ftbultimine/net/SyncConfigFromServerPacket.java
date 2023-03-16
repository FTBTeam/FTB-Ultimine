package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.SNBTNet;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.minecraft.network.FriendlyByteBuf;

public class SyncConfigFromServerPacket extends BaseS2CMessage {
    private final SNBTCompoundTag config;

    public SyncConfigFromServerPacket(SNBTCompoundTag config) {
        this.config = config;
    }

    public SyncConfigFromServerPacket(FriendlyByteBuf buf) {
        config = SNBTNet.readCompound(buf);
    }

    @Override
    public MessageType getType() {
        return FTBUltimineNet.SYNC_CONFIG_FROM_SERVER;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        SNBTNet.writeCompound(buf, config);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        FTBUltimineServerConfig.CONFIG.read(config);
        FTBUltimine.LOGGER.info("received server config settings");
    }
}
