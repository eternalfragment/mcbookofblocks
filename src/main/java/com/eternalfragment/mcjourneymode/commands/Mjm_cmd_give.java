package com.eternalfragment.mcjourneymode.commands;

import com.eternalfragment.mcjourneymode.operators.CommandFunctions;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.List;

public final class Mjm_cmd_give  {
    public Mjm_cmd_give(CommandDispatcher<ServerCommandSource> dispatcher){
        System.out.println("REGISTERED MJM COMMAND");
        dispatcher.register(
                CommandManager.literal("mjm").requires(source -> source.hasPermissionLevel(4))
                        .then(CommandManager.literal("give")
                                .then(CommandManager.argument("PlayerName", EntityArgumentType.players())
                                        .requires(source -> source.hasPermissionLevel(4))
                                            .executes((command)->{
                                                String playerArgs="";
                                                Collection<ServerPlayerEntity> toPlayer = EntityArgumentType.getPlayers(command, "PlayerName");
                                                playerArgs=toPlayer.toString();
                                                if (playerArgs==null){playerArgs="";}
                                                CommandFunctions.giveMjmItem(toPlayer,command.getSource().getPlayer());
                                                return 0;
                                            }))
                                .requires(source -> source.hasPermissionLevel(4))
                                .executes((command)->{
                                    Collection<ServerPlayerEntity> toPlayer= List.of(command.getSource().getPlayer());
                                    //ServerPlayerEntity cmdSource = command.getSource().getPlayer();
                                    ///assert toPlayer != null;
                                    //toPlayer.add(cmdSource);
                                    CommandFunctions.giveMjmItem(toPlayer,command.getSource().getPlayer());
                                    return 0;
                                })
                        ));
    }
}
