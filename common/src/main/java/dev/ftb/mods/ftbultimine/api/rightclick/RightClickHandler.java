package dev.ftb.mods.ftbultimine.api.rightclick;

import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/// Register implementations of this interface via the [RegisterRightClickHandlerEvent] event handler.
public interface RightClickHandler {
    /// Called when a player right-clicks a block with the Ultimine key held, and other ultimining conditions such
    /// as player food level are satisfied. Handlers are called in the order in which they were registered, and
    /// when a handler returns a non-zero result, no further handlers are checked.
    ///
    /// @param shapeContext the shape context, providing data such as the player, clicked position and face, etc.
    /// @param hand         the hand the player is using
    /// @param positions    the block positions covered by the current ultimining shape
    /// @return the number of blocks actually affected by this operation (return 0 if the operation is for any reason not applicable)
    int handleRightClickBlock(ShapeContext shapeContext, InteractionHand hand, Collection<BlockPos> positions);

    /// Optional preview filter for the Ultimine block outline shown to the player. Called every tick while the
    /// Ultimine key is held. Handlers can return a filtered (or reduced) subset of `positions` to restrict the
    /// preview to only the blocks this handler would actually act on, given the player's currently held items
    /// and world state.
    ///
    /// Implementations should return `null` if this handler does not apply (e.g. the held item isn't relevant).
    /// The first handler to return a non-null result wins; subsequent handlers are not consulted for the preview.
    ///
    /// @param player    the player holding the Ultimine key
    /// @param positions the current shape positions
    /// @return a filtered list of positions, or `null` if this handler is not applicable
    default @Nullable List<BlockPos> filterPreview(ServerPlayer player, List<BlockPos> positions) {
        return null;
    }

    /// Convenience method to damage a tool item on use. Can be called by right-click handlers when processing blocks.
    ///
    /// @param player the player
    /// @param hand the hand the tool is in
    /// @return true if the tool broke on this usage, false otherwise
    @ApiStatus.NonExtendable
    default boolean hurtItemAndCheckIfBroken(ServerPlayer player, InteractionHand hand) {
        if (player.isCreative()) {
            return false;
        }
        ItemStack itemStack = player.getItemInHand(hand);
        itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        return itemStack.isEmpty();
    }
}
