package dev.ftb.mods.ftbultimine.api.blockselection;

import dev.ftb.mods.ftblibrary.util.OptionalBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implementations of this can be registered via {@link RegisterBlockSelectionHandlerEvent} to add custom block-selection
 * behaviour for specific blocks, e.g. where extended information such as block entity data may be important.
 * <p>
 * Note that where multiple handlers are registered, they are called in registration order, and if any handler
 * returns a non-PASS result, no subsequent handlers are called on this ultimining operation. If all handlers return
 * PASS, normal built-in logic is used to determine if the block should be included in the operation.
 */
public interface BlockSelectionHandler {
    /**
     * Perform a custom check to see if a block should be included in the current ultimining operation, based on its
     * block position and blockstate. The original block position and blockstate are also available, for comparison
     * purposes.
     * <p>
     * This method is called for every block potentially included in an Ultimine selection, based on the current
     * ultimining shape. Since this method can be called many times, care should be taken to ensure it runs quickly. In
     * particular, blockstates should be checked (and PASS promptly returned if the block is not relevant to this
     * handler) before any extended operations such as block entity querying is done.
     *
     * @param level the level
     * @param origPos the original block position
     * @param pos the current position being checked
     * @param origState the blockstate at the original block position
     * @param state the blockstate at the current position
     * @return TRUE if the block should definitely be included, FALSE if definitely not included,
     *      or PASS if this handler doesn't care
     */
    Result customSelectionCheck(Level level, BlockPos origPos, BlockPos pos, BlockState origState, BlockState state);

    enum Result {
        TRUE,
        FALSE,
        PASS;

        public static Result of(boolean b) {
            return b ? TRUE : FALSE;
        }

        public OptionalBoolean asBoolean() {
            return switch (this) {
                case TRUE -> OptionalBoolean.TRUE;
                case FALSE -> OptionalBoolean.FALSE;
                default -> OptionalBoolean.EMPTY;
            };
        }
    }
}
