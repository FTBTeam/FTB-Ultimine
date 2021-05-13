package dev.ftb.mods.ftbultimine.utils;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftbultimine.mixin.KeyAccessor;
import net.minecraft.client.KeyMapping;

public class AccessUtil {
	public static InputConstants.Key getKey(KeyMapping mapping) {
		return ((KeyAccessor) mapping).getKey();
	}
}
