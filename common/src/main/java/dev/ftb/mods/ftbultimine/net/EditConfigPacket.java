package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EditConfigPacket(ConfigType configType) implements CustomPacketPayload {
    public static final Type<EditConfigPacket> TYPE = new Type<>(FTBUltimine.rl("edit_config_packet"));

    public static final StreamCodec<FriendlyByteBuf, EditConfigPacket> STREAM_CODEC = StreamCodec.composite(
            NetworkHelper.enumStreamCodec(ConfigType.class), EditConfigPacket::configType,
            EditConfigPacket::new
    );

    @Override
    public Type<EditConfigPacket> type() {
        return TYPE;
    }

    public static void handle(EditConfigPacket message, NetworkManager.PacketContext context) {
        FTBUltimineClient.editConfig(context.getPlayer(), message.configType);
    }

    public enum ConfigType {
        CLIENT,
        SERVER,
        CHOOSE
    }
}
