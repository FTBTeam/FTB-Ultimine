package dev.ftb.mods.ftbultimine.integration;

import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftbultimine.api.neoforge.FTBUltimineEvent;
import dev.ftb.mods.ftbultimine.integration.ezcrystals.neoforge.EZCrystals;
import dev.ftb.mods.ftbultimine.integration.framedblocks.FramedBlocksRightClickHandler;
import dev.ftb.mods.ftbultimine.integration.ftbranks.neoforge.FTBRanksNeoIntegration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class NeoIntegration {
    public static void init(IEventBus bus) {
        if (Platform.get().isModLoaded("ftbranks")) {
            FTBRanksNeoIntegration.init(bus);
        }

        if (Platform.get().isModLoaded("ftbezcrystals")) {
            EZCrystals.init(bus);
        }

        // "framed_blocks_ultimine" is an external mod doing the same thing, but the mod author has OK'd it being included here
        // https://github.com/FTBTeam/FTB-Modpack-Issues/issues/13931
        if (Platform.get().isModLoaded(FramedBlocksRightClickHandler.FRAMED_BLOCKS_MOD)
                && Platform.get().isModLoaded("framedblocks")
                && !Platform.get().isModLoaded("framed_blocks_ultimine"))
        {
            NeoForge.EVENT_BUS.addListener(FTBUltimineEvent.RegisterRightClickHandler.class,
                    event -> event.getEventData().registerHandler(FramedBlocksRightClickHandler.INSTANCE)
            );
        }
    }
}
