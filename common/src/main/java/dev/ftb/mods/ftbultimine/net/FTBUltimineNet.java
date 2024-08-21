package dev.ftb.mods.ftbultimine.net;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import dev.ftb.mods.ftbultimine.FTBUltimine;

/**
 * @author LatvianModder
 */
public interface FTBUltimineNet {
	SimpleNetworkManager NET = SimpleNetworkManager.create(FTBUltimine.MOD_ID);

	MessageType SEND_SHAPE = NET.registerS2C("send_shape", SendShapePacket::new);
	MessageType KEY_PRESSED = NET.registerC2S("key_pressed", KeyPressedPacket::new);
	MessageType MODE_CHANGED = NET.registerC2S("mode_changed", ModeChangedPacket::new);
	MessageType SYNC_CONFIG_FROM_SERVER = NET.registerS2C("sync_config_from_server", SyncConfigFromServerPacket::new);
	MessageType SYNC_CONFIG_TO_SERVER = NET.registerC2S("sync_config_to_server", SyncConfigToServerPacket::new);
	MessageType EDIT_CONFIG = NET.registerS2C("edit_config", EditConfigPacket::new);
	MessageType SYNC_ULTIMINE_TIME = NET.registerS2C("sync_ultimine_time", SyncUltimineTimePacket::new);

	static void init() {
	}
}