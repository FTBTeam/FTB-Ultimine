package dev.ftb.mods.ftbultimine.net;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import me.shedaniel.architectury.networking.simple.MessageType;
import me.shedaniel.architectury.networking.simple.SimpleNetworkManager;

/**
 * @author LatvianModder
 */
public interface FTBUltimineNet {
	SimpleNetworkManager NET = SimpleNetworkManager.create(FTBUltimine.MOD_ID);

	MessageType SEND_SHAPE = NET.registerS2C("send_shape", SendShapePacket::new);
	MessageType KEY_PRESSED = NET.registerC2S("key_pressed", KeyPressedPacket::new);
	MessageType MODE_CHANGED = NET.registerC2S("mode_changed", ModeChangedPacket::new);

	static void init() {
	}
}