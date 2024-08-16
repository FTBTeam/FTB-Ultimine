package dev.ftb.mods.ftbultimine;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbultimine.net.EditConfigPacket;
import dev.ftb.mods.ftbultimine.net.EditConfigPacket.ConfigType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class FTBUltimineCommands {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("ftbultimine")
                .then(Commands.literal("serverconfig")
                        .requires(sourceStack -> sourceStack.isPlayer() && sourceStack.hasPermission(2))
                        .executes(context -> {
                            NetworkManager.sendToPlayer(context.getSource().getPlayerOrException(), new EditConfigPacket(ConfigType.SERVER));
                            return 1;
                        })
                )
                .then(Commands.literal("clientconfig")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> {
                            NetworkManager.sendToPlayer(context.getSource().getPlayerOrException(), new EditConfigPacket(ConfigType.CLIENT));
                            return 1;
                        })
                )
                .then(Commands.literal("config")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> {
                            NetworkManager.sendToPlayer(context.getSource().getPlayerOrException(), new EditConfigPacket(ConfigType.CHOOSE));
                            return 1;
                        })
                )
        );
    }
}
