package com.eternalfragment.bookofblocks.commands;

import com.eternalfragment.bookofblocks.operators.CommandFunctions;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.List;

public final class Bob_cmd_give  {
    public Bob_cmd_give(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(
                CommandManager.literal("bob").requires(source -> source.hasPermissionLevel(4))
                        .then(CommandManager.literal("give")
                                .then(CommandManager.argument("PlayerName", EntityArgumentType.players())
                                        .requires(source -> source.hasPermissionLevel(4))
                                            .executes((command)->{
                                                String playerArgs="";
                                                Collection<ServerPlayerEntity> toPlayer = EntityArgumentType.getPlayers(command, "PlayerName");
                                                playerArgs=toPlayer.toString();
                                                if (playerArgs==null){playerArgs="";}
                                                CommandFunctions.giveBobItem(toPlayer,command.getSource().getPlayer());
                                                return 0;
                                            }))
                                .requires(source -> source.hasPermissionLevel(4))
                                .executes((command)->{
                                    Collection<ServerPlayerEntity> toPlayer= List.of(command.getSource().getPlayer());
                                    CommandFunctions.giveBobItem(toPlayer,command.getSource().getPlayer());
                                    return 0;
                                })
                        ));
    }
}
