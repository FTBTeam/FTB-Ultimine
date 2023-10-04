package dev.ftb.mods.ftbultimine.integration;

import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.minecraft.server.level.ServerPlayer;

public class FTBRanksIntegration {
    private static final String MAX_BLOCKS_PERM = "ftbultimine.max_blocks";

    public static int getMaxBlocks(ServerPlayer player) {
        return FTBRanksAPI.getPermissionValue(player, MAX_BLOCKS_PERM).asInteger()
                .orElse(FTBUltimineServerConfig.MAX_BLOCKS.get());
    }
}
