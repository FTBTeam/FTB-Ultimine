package com.feed_the_beast.mods.ftbultimine.net;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class FTBUltimineNet
{
	public static SimpleChannel MAIN;

	public static void init()
	{
		Predicate<String> validator = v -> "1".equals(v) || NetworkRegistry.ABSENT.equals(v) || NetworkRegistry.ACCEPTVANILLA.equals(v);

		MAIN = NetworkRegistry.ChannelBuilder
				.named(new ResourceLocation("ftbultimine", "main"))
				.clientAcceptedVersions(validator)
				.serverAcceptedVersions(validator)
				.networkProtocolVersion(() -> "1")
				.simpleChannel();

		MAIN.registerMessage(1, SendShapePacket.class, SendShapePacket::write, SendShapePacket::new, SendShapePacket::handle);
		MAIN.registerMessage(2, KeyPressedPacket.class, KeyPressedPacket::write, KeyPressedPacket::new, KeyPressedPacket::handle);
		MAIN.registerMessage(3, ModeChangedPacket.class, ModeChangedPacket::write, ModeChangedPacket::new, ModeChangedPacket::handle);
	}
}