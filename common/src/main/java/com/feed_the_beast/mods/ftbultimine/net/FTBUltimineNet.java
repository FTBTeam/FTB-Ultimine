package com.feed_the_beast.mods.ftbultimine.net;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import me.shedaniel.architectury.networking.NetworkChannel;
import me.shedaniel.architectury.networking.NetworkManager;

/**
 * @author LatvianModder
 */
public class FTBUltimineNet
{
	public static NetworkChannel MAIN;

	public static void init()
	{
		MAIN = NetworkChannel.create(FTBUltimine.id("main"));

		MAIN.register(NetworkManager.Side.S2C, SendShapePacket.class, SendShapePacket::write, SendShapePacket::new, SendShapePacket::handle);
		MAIN.register(NetworkManager.Side.C2S, KeyPressedPacket.class, KeyPressedPacket::write, KeyPressedPacket::new, KeyPressedPacket::handle);
		MAIN.register(NetworkManager.Side.C2S, ModeChangedPacket.class, ModeChangedPacket::write, ModeChangedPacket::new, ModeChangedPacket::handle);
	}
}