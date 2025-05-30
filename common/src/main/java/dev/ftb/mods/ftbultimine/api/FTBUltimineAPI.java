package dev.ftb.mods.ftbultimine.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

public class FTBUltimineAPI {
    public static final String MOD_ID = "ftbultimine";

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
        return data.getExhaustionLevel() / 4f > data.getSaturationLevel() + data.getFoodLevel();
    }

    /**
     * Get a resource location in the FTB Ultimine namespace
     * @param path the path
     * @return the resource location
     */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
