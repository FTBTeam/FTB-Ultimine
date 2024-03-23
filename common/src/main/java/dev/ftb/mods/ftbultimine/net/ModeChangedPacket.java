package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ModeChangedPacket extends BaseC2SMessage {
	public final boolean next;

	public ModeChangedPacket(boolean n) {
		next = n;
	}

	public ModeChangedPacket(FriendlyByteBuf buf) {
		next = buf.readBoolean();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(next);
	}

	@Override
	public MessageType getType() {
		return FTBUltimineNet.MODE_CHANGED;
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		context.queue(() -> FTBUltimine.instance.modeChanged((ServerPlayer) context.getPlayer(), next));
	}
}