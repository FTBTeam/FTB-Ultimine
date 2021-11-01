package dev.ftb.mods.ftbultimine.net;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.simple.BaseC2SMessage;
import me.shedaniel.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class KeyPressedPacket extends BaseC2SMessage {
	private final boolean pressed;

	public KeyPressedPacket(boolean p) {
		pressed = p;
	}

	public KeyPressedPacket(FriendlyByteBuf buf) {
		pressed = buf.readBoolean();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(pressed);
	}

	@Override
	public MessageType getType() {
		return FTBUltimineNet.KEY_PRESSED;
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		context.queue(() -> FTBUltimine.instance.setKeyPressed((ServerPlayer) context.getPlayer(), pressed));
	}
}