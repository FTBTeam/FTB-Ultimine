package dev.ftb.mods.ftbultimine.utils;

import com.google.common.primitives.Ints;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class XPUtils {
    /**
     * Be warned, minecraft doesn't update player.totalExperience properly, so we have
     * to do this.
     *
     * @param player the player
     * @return player's current XP amount
     */
    public static long getPlayerXP(Player player) {
        return (long) (getExperienceForLevel(player.experienceLevel) + (player.experienceProgress * player.getXpNeededForNextLevel()));
    }

    public static void addPlayerXP(Player player, int amount) {
        long experience = getPlayerXP(player) + amount;
        player.totalExperience = Ints.saturatedCast(experience);  // can't be helped, vanilla uses 32-bit int here
        player.experienceLevel = getLevelForExperience(experience);
        long expForLevel = getExperienceForLevel(player.experienceLevel);
        player.experienceProgress = (float)(experience - expForLevel) / (float)player.getXpNeededForNextLevel();
        if (player instanceof ServerPlayer sp) {
            // force a sync to client
            sp.setExperienceLevels(sp.experienceLevel);
        }
    }

    public static long getExperienceForLevel(int level) {
        if (level == 0) return 0L;
        if (level <= 15) return sum(level, 7, 2);
        if (level <= 30) return 315 + sum(level - 15, 37, 5);
        return 1395L + sum(level - 30, 112, 9);
    }

    private static long sum(int n, int a0, int d) {
        return n * (2L * a0 + (n - 1L) * d) / 2;
    }

    public static int xpBarCap(int level) {
        if (level >= 30)
            return 112 + (level - 30) * 9;

        if (level >= 15)
            return 37 + (level - 15) * 5;

        return 7 + level * 2;
    }

    public static int getLevelForExperience(long targetXp) {
        int level = 0;
        while (true) {
            final int xpToNextLevel = xpBarCap(level);
            if (targetXp < xpToNextLevel) return level;
            level++;
            targetXp -= xpToNextLevel;
        }
    }
}
