package com.feed_the_beast.mods.ftbultimine.net;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import me.shedaniel.architectury.networking.NetworkChannel;

/**
 * @author LatvianModder
 */
public class FTBUltimineNet {
	public static NetworkChannel MAIN;

	public static void init() {
		MAIN = NetworkChannel.create(FTBUltimine.id("main"));

		MAIN.register(SendShapePacket.class, SendShapePacket::write, SendShapePacket::new, SendShapePacket::handle);
		MAIN.register(KeyPressedPacket.class, KeyPressedPacket::write, KeyPressedPacket::new, KeyPressedPacket::handle);
		MAIN.register(ModeChangedPacket.class, ModeChangedPacket::write, ModeChangedPacket::new, ModeChangedPacket::handle);
	}
}