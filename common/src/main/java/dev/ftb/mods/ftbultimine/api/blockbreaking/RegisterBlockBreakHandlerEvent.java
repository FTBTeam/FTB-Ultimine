package dev.ftb.mods.ftbultimine.api.blockbreaking;

import java.util.function.Consumer;

/// Fired on server startup to register a custom block-breaking handler. See also: [BlockBreakHandler]
///
/// Corresponding platform-native events to listen to:
/// * `FTBUltimineEvent.RegisterBlockBreakHandler` (NeoForge)
/// * `FTBUltimineEvents.REGISTER_BLOCK_BREAK_HANDLER` (Fabric)
@FunctionalInterface
public interface RegisterBlockBreakHandlerEvent extends Consumer<RegisterBlockBreakHandlerEvent.Data> {
    record Data(Consumer<BlockBreakHandler> consumer) {
        /// Register a new handler.
        ///
        /// @param handler the handler to register
        public void registerHandler(BlockBreakHandler handler) {
            consumer.accept(handler);
        }
    }
}
