package dev.ftb.mods.ftbultimine;

import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import dev.ftb.mods.ftbultimine.api.blockselection.BlockSelectionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.Optional;

public enum FTBUltimineAPIImpl implements FTBUltimineAPI.API {
    INSTANCE;

    @Override
    public Optional<Collection<BlockPos>> currentBlockSelection(Player player) {
        return Optional.ofNullable(FTBUltimine.instance.getOrCreatePlayerData(player).cachedPositions());
    }

    @Override
    public BlockSelectionHandler.Result customSelectionCheck(Player player, BlockPos origPos, BlockPos pos, BlockState origState, BlockState state) {
        for (var h : BlockSelectionRegistry.INSTANCE.getHandlers()) {
            var res = h.customSelectionCheck(player, origPos, pos, origState, state);
            if (res != BlockSelectionHandler.Result.PASS) {
                return res;
            }
        }
        return BlockSelectionHandler.Result.PASS;
    }
}