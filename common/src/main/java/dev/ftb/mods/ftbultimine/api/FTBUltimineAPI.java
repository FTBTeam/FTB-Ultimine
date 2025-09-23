package dev.ftb.mods.ftbultimine.api;

import dev.ftb.mods.ftbultimine.api.blockselection.BlockSelectionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Optional;

public class FTBUltimineAPI {
    public static final String MOD_ID = "ftbultimine";

    private static API instance;

    /**
     * This convenience method can be used to check if a player is too exhausted food-wise to continue an ultimining
     * operation.
     * @param player the player to check
     * @return true if the player's food level is too low to carry on ultimining
     */
    public static boolean isTooExhausted(ServerPlayer player) {
        if (player.isCreative()) {
            return false;
        }
        FoodData data = player.getFoodData();
        return data.exhaustionLevel / 4f > data.getSaturationLevel() + data.getFoodLevel();
    }

    /**
     * Get a resource location in the FTB Ultimine namespace
     * @param path the path
     * @return the resource location
     */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    /**
     * Retrieve the public API instance.
     *
     * @return the API handler
     */
    public static API api() {
        return instance;
    }

    @ApiStatus.Internal
    public static void _init(API instance) {
        if (FTBUltimineAPI.instance != null) {
            throw new IllegalStateException("can't init more than once!");
        }
        FTBUltimineAPI.instance = instance;
    }

    /**
     * Top-level API. Retrieve an instance of this via {@link FTBUltimineAPI#api()}.
     */
    public interface API {
        /**
         * Get a collection of the block positions in the player's current selection.
         * @param player the player to check
         * @return an optional collection of the block positions the player has selected;
         *   {@code Optional.empty()} if the player is not currently ultimining
         */
        Optional<Collection<BlockPos>> currentBlockSelection(Player player);

        /**
         * Called by {@link dev.ftb.mods.ftbultimine.api.shape.ShapeContext#check(BlockPos)} to handle any custom
         * block equivalence checks. You should not normally need to call this directly.
         *
         * @param player    the player
         * @param origPos   the original block position
         * @param pos       the current position being checked
         * @param origState the blockstate at the original block position
         * @param state     the blockstate at the current position
         * @return TRUE if the blocks are considered equivalent by a custom check, FALSE if definitely not equivalent,
         * or PASS to defer to the result of standard block equivalence checking, i.e. no custom handler cares.
         */
        BlockSelectionHandler.Result customSelectionCheck(Player player, BlockPos origPos, BlockPos pos, BlockState origState, BlockState state);
    }
}
