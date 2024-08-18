package dev.ftb.mods.ftbultimine;

import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.net.SyncUltimineTimePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownTracker {
    private static final CooldownTracker clientInstance = new CooldownTracker();
    private static final CooldownTracker serverInstance = new CooldownTracker();

    private final Map<UUID,Long> lastUltimineTime = new HashMap<>();

    // client can't just look at the server config (even though it's sync'd) since we could be using Ranks...
    // so this get sync'd when the server config changes, or when we get a Ranks event indicating a change
    private static long ultimineCooldownClient;

    public static long getLastUltimineTime(Player player) {
        CooldownTracker instance = player.level.isClientSide ? clientInstance : serverInstance;
        return instance.lastUltimineTime.getOrDefault(player.getUUID(), 0L);
    }

    public static void setLastUltimineTime(Player player, long when) {
        CooldownTracker instance = player.level.isClientSide ? clientInstance : serverInstance;
        instance.lastUltimineTime.put(player.getUUID(), when);
        if (player instanceof ServerPlayer sp) {
            new SyncUltimineTimePacket(when, SyncUltimineTimePacket.TimeType.LAST_USED).sendTo(sp);
        }
    }

    /**
     * Get the remaining cooldown time for the player as a proportion of total time
     * @param player the player to check
     * @return a value in the range 0f -> 1f
     */
    public static float getCooldownRemaining(Player player) {
        long coolDown = getUltimineCooldown(player);
        if (coolDown == 0L) {
            return 1f;
        }
        long tickDelta = (System.currentTimeMillis() - getLastUltimineTime(player)) / 50;  // ms -> ticks
        return Mth.clamp((float)tickDelta / coolDown, 0f, 1f);
    }

    public static boolean isOnCooldown(Player player) {
        long coolDown = getUltimineCooldown(player);
        if (coolDown == 0L) {
            return false;
        }
        long coolDownMs = coolDown * 50;  // ticks -> ms
        return coolDownMs > 0L && System.currentTimeMillis() - getLastUltimineTime(player) < coolDownMs;
    }

    private static long getUltimineCooldown(Player player) {
        return player instanceof ServerPlayer sp ?
                FTBUltimineServerConfig.getUltimineCooldown(sp) :
                ultimineCooldownClient;
    }

    public static void setClientCooldownTime(long cooldown) {
        ultimineCooldownClient = cooldown;
    }
}
