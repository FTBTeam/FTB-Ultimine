package com.feed_the_beast.mods.ftbultimine.net;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class KeyPressedPacket {
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

	public void handle(Supplier<NetworkManager.PacketContext> context) {
		context.get().queue(() -> FTBUltimine.instance.setKeyPressed((ServerPlayer) context.get().getPlayer(), pressed));
	}
}