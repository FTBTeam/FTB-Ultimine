package dev.ftb.mods.ftbultimine.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccess {
    @Accessor("lastSentExp")
    void ftbUltimine$setLastSentExp(int lastSent);
}
