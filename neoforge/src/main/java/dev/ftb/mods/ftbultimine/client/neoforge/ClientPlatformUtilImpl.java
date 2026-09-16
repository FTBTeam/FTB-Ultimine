package dev.ftb.mods.ftbultimine.client.neoforge;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import org.jspecify.annotations.Nullable;
import org.lwjgl.sdl.SDLKeycode;

// arch expect
@SuppressWarnings("unused")
public class ClientPlatformUtilImpl {
    public static boolean doesKeybindMatch(@Nullable KeyMapping keyMapping, KeyEvent event) {
        if (keyMapping.matches(event)) {
            return switch (keyMapping.getKeyModifier()) {
                case NONE -> event.modifiers() == 0;
                case SHIFT ->  (event.modifiers() & SDLKeycode.SDL_KMOD_SHIFT) != 0;
                case CONTROL, CONTROL_OR_COMMAND ->  (event.modifiers() & SDLKeycode.SDL_KMOD_CTRL) != 0;
                case ALT ->  (event.modifiers() & SDLKeycode.SDL_KMOD_ALT) != 0;
            };
        }
        return false;
    }
}
