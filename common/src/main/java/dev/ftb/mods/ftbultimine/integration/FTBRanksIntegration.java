package dev.ftb.mods.ftbultimine.integration;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.event.RankEvent;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.net.SyncUltimineTimePacket;
import dev.ftb.mods.ftbultimine.net.SyncUltimineTimePacket.TimeType;
import net.minecraft.server.level.ServerPlayer;

public class FTBRanksIntegration {
    private static final String MAX_BLOCKS_PERM = "ftbultimine.max_blocks";
    private static final String COOLDOWN_PERM = "ftbultimine.ultimine_cooldown";
    private static final String EXPERIENCE_COST_PERM = "ftbultimine.experience_per_block";
    private static final String EXHAUSTION_MULTIPLIER_PERM = "ftbultimine.exhaustion_per_block";

    public static void init() {
        RankEvent.ADD_PLAYER.register(FTBRanksIntegration::updatePlayer);
        RankEvent.REMOVE_PLAYER.register(FTBRanksIntegration::updatePlayer);
        RankEvent.PERMISSION_CHANGED.register(FTBRanksIntegration::updateAllPlayers);
        RankEvent.RELOADED.register(FTBRanksIntegration::updateAllPlayers);
        RankEvent.CONDITION_CHANGED.register(FTBRanksIntegration::updateAllPlayers);

        FTBUltimine.LOGGER.info("FTB Ranks detected, listening for ranks events");
    }

    private static void updatePlayer(RankEvent.Player event) {
        ServerPlayer sp = event.getManager().getServer().getPlayerList().getPlayer(event.getPlayer().getId());
        if (sp != null) {
            NetworkManager.sendToPlayer(sp, new SyncUltimineTimePacket(FTBUltimineServerConfig.getUltimineCooldown(sp), TimeType.COOLDOWN));
        }
    }

    private static void updateAllPlayers(RankEvent event) {
        event.getManager().getServer().getPlayerList().getPlayers().forEach(sp -> {
            NetworkManager.sendToPlayer(sp, new SyncUltimineTimePacket(FTBUltimineServerConfig.getUltimineCooldown(sp), TimeType.COOLDOWN));
        });
    }

    public static int getMaxBlocks(ServerPlayer player) {
        return FTBRanksAPI.getPermissionValue(player, MAX_BLOCKS_PERM).asInteger()
                .orElse(FTBUltimineServerConfig.MAX_BLOCKS.get());
    }

    public static long getUltimineCooldown(ServerPlayer player) {
        return FTBRanksAPI.getPermissionValue(player, COOLDOWN_PERM).asLong()
                .orElse(FTBUltimineServerConfig.ULTIMINE_COOLDOWN.get());
    }

    public static double getExperiencePerBlock(ServerPlayer player) {
        return FTBRanksAPI.getPermissionValue(player, EXPERIENCE_COST_PERM).asDouble()
                .orElse(FTBUltimineServerConfig.EXPERIENCE_PER_BLOCK.get());
    }

    public static double getExhaustionPerBlock(ServerPlayer player) {
        return FTBRanksAPI.getPermissionValue(player, EXHAUSTION_MULTIPLIER_PERM).asDouble()
                .orElse(FTBUltimineServerConfig.EXHAUSTION_PER_BLOCK.get());
    }
}
