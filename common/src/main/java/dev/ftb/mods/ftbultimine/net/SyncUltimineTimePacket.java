package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbultimine.CooldownTracker;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import net.minecraft.network.FriendlyByteBuf;

public class SyncUltimineTimePacket extends BaseS2CMessage {

    private final long when;
    private final TimeType timetype;

    public SyncUltimineTimePacket(FriendlyByteBuf buf) {
        this.when = buf.readLong();
        this.timetype = buf.readEnum(TimeType.class);
    }

    public SyncUltimineTimePacket(long when, TimeType timetype) {
        this.when = when;
        this.timetype = timetype;
    }

    @Override
    public MessageType getType() {
        return FTBUltimineNet.SYNC_ULTIMINE_TIME;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(when);
        buf.writeEnum(timetype);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        switch (timetype) {
            case LAST_USED -> CooldownTracker.setLastUltimineTime(FTBUltimineClient.getClientPlayer(), when);
            case COOLDOWN -> CooldownTracker.setClientCooldownTime(when);
        }
    }

    public enum TimeType {
        COOLDOWN,
        LAST_USED
    }
}
