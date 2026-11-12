package dev.ftb.mods.ftbultimine;

import com.mojang.brigadier.CommandDispatcher;
import dev.ftb.mods.ftblibrary.net.EditConfigChoicePacket;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbultimine.config.FTBUltimineClientConfig;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public class FTBUltimineCommands {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignoredCtx, Commands.CommandSelection ignoredSel) {
        dispatcher.register(Commands.literal("ftbultimine")
                .then(Commands.literal("serverconfig")
                        .requires(sourceStack -> sourceStack.isPlayer() && sourceStack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(context -> {
                            Server2PlayNetworking.send(context.getSource().getPlayerOrException(), EditConfigChoicePacket.server(FTBUltimineServerConfig.KEY));
                            return 1;
                        })
                )
                .then(Commands.literal("clientconfig")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> {
                            Server2PlayNetworking.send(context.getSource().getPlayerOrException(), EditConfigChoicePacket.client(FTBUltimineClientConfig.KEY));
                            return 1;
                        })
                )
                .then(Commands.literal("config")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> {
                            Server2PlayNetworking.send(context.getSource().getPlayerOrException(), EditConfigChoicePacket.choose(FTBUltimineClientConfig.KEY, FTBUltimineServerConfig.KEY, Component.translatable("ftbultimine.config")));
                            return 1;
                        })
                )
        );
    }
}
