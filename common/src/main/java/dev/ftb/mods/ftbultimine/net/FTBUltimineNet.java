package dev.ftb.mods.ftbultimine.net;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;

public class FTBUltimineNet {
	public static void init() {
		NetworkHelper.registerS2C(SendShapePacket.TYPE, SendShapePacket.STREAM_CODEC, SendShapePacket::handle);
		NetworkHelper.registerS2C(EditConfigPacket.TYPE, EditConfigPacket.STREAM_CODEC, EditConfigPacket::handle);
		NetworkHelper.registerS2C(SyncUltimineTimePacket.TYPE, SyncUltimineTimePacket.STREAM_CODEC, SyncUltimineTimePacket::handle);

		NetworkHelper.registerC2S(KeyPressedPacket.TYPE, KeyPressedPacket.STREAM_CODEC, KeyPressedPacket::handle);
		NetworkHelper.registerC2S(ModeChangedPacket.TYPE, ModeChangedPacket.STREAM_CODEC, ModeChangedPacket::handle);
	}
}