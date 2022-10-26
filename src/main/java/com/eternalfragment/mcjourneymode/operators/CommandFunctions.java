package com.eternalfragment.mcjourneymode.operators;

import com.eternalfragment.mcjourneymode.Mcjourneymode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

public class CommandFunctions {
    public static void giveMjmItem(Collection<ServerPlayerEntity> toPlayer,ServerPlayerEntity cmdOriginator){
        for (ServerPlayerEntity serverPlayerEntity : toPlayer) {
            if (serverPlayerEntity.getInventory().count(Mcjourneymode.GUI_ITEM.asItem()) == 0) {
                int emptySlot = serverPlayerEntity.getInventory().getEmptySlot();
                boolean bl = serverPlayerEntity.getInventory().insertStack(Mcjourneymode.GUI_ITEM.asItem().getDefaultStack());
                if ((bl) && (emptySlot != -1)) {
                    if (serverPlayerEntity != cmdOriginator) {
                        cmdOriginator.sendMessage(Text.translatable("mjm.msg.mjmitem.gavePlayer").append(" \"" + serverPlayerEntity.getDisplayName() + "\" ").append(Text.translatable("mjm.msg.mjmitem.jmItem")), false);
                    }
                    serverPlayerEntity.sendMessage(Text.translatable("mjm.msg.mjmitem.youGiven"), false);
                } else {
                    if (serverPlayerEntity != cmdOriginator) {
                        cmdOriginator.sendMessage(Text.translatable("mjm.msg.mjmitem.Player").append(" \"" + serverPlayerEntity.getDisplayName() + "\" ").append(Text.translatable("mjm.msg.mjmitem.invFull").getString()), false);
                    }
                    serverPlayerEntity.sendMessage(Text.translatable("mjm.msg.mjmitem.invFullNotice"), false);
                }
            } else {
                cmdOriginator.sendMessage(Text.translatable("mjm.msg.mjmitem.Player").append(" \"" + serverPlayerEntity.getDisplayName() + "\" ").append(Text.translatable("mjm.msg.mjmitem.already").getString()), false);
            }
        }
    }
}
