package dev.ftb.mods.ftbultimine.integration.acceldecay;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public enum LogBreakTracker {
    INSTANCE;

    private final Object2LongMap<UUID> lastLogBroken = new Object2LongOpenHashMap<>();

    public void playerBrokeLog(Player player) {
        lastLogBroken.put(player.getUUID(), Util.getMillis());
    }

    public boolean playerRecentlyBrokeLog(Player player, long deltaMS) {
        return Util.getMillis() - lastLogBroken.getOrDefault(player.getUUID(), 0L) > deltaMS;
    }
}
