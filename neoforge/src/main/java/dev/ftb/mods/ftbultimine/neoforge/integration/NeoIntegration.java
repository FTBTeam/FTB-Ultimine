package dev.ftb.mods.ftbultimine.neoforge.integration;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftbultimine.api.rightclick.RegisterRightClickHandlerEvent;
import dev.ftb.mods.ftbultimine.neoforge.integration.ezcrystals.neoforge.EZCrystals;
import dev.ftb.mods.ftbultimine.neoforge.integration.framedblocks.FramedBlocksHandler;
import net.neoforged.bus.api.IEventBus;

public class NeoIntegration {
    public static void init(IEventBus modEventBus) {
        if (Platform.isModLoaded("ftbezcrystals")) {
            EZCrystals.init();
        }

        // "framed_blocks_ultimine" is an external mod doing the same thing, but the mod author has OK'd it being included here
        // https://github.com/FTBTeam/FTB-Modpack-Issues/issues/13931
        if (Platform.isModLoaded(FramedBlocksHandler.FRAMED_BLOCKS_MOD)
                && Platform.isModLoaded("framedblocks")
                && !Platform.isModLoaded("framed_blocks_ultimine"))
        {
            RegisterRightClickHandlerEvent.REGISTER.register(registry -> registry.registerHandler(FramedBlocksHandler.INSTANCE));
        }
    }
}
