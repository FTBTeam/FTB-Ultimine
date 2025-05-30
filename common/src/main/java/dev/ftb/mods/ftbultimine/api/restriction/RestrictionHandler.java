package dev.ftb.mods.ftbultimine.api.restriction;

import net.minecraft.world.entity.player.Player;

/**
 * Register implementations of this via {@link RegisterRestrictionHandlerEvent}. See that class for more information.
 */
@FunctionalInterface
public interface RestrictionHandler {
    /**
     * Is the player allowed to do ultimining at this time?
     * @param player the player being checked
     * @return false to prevent ultimining, true to allow (note that if any handlers are registered, <em>all</em> must return true for ultimining to be allowed)
     */
    boolean canUltimine(Player player);
}
