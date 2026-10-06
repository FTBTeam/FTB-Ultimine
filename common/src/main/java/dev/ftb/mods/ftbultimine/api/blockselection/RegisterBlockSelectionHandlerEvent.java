package dev.ftb.mods.ftbultimine.api.blockselection;

import java.util.function.Consumer;

/// Fired on server startup to register a custom block-selection handler. See also: [BlockSelectionHandler]
///
/// Corresponding platform-native events to listen to:
/// * `FTBUltimineEvent.RegisterBlockSelectionHandler` (NeoForge)
/// * `FTBUltimineEvents.REGISTER_BLOCK_SELECTION_HANDLER` (Fabric)
@FunctionalInterface
public interface RegisterBlockSelectionHandlerEvent extends Consumer<RegisterBlockSelectionHandlerEvent.Data> {
    record Data(Consumer<BlockSelectionHandler> consumer) {
        /// Register a new handler.
        ///
        /// @param handler the handler to register
        public void registerHandler(BlockSelectionHandler handler) {
            consumer.accept(handler);
        }
    }
}
