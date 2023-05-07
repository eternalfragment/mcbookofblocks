package com.eternalfragment.bookofblocks.operators;

import com.eternalfragment.bookofblocks.Bookofblocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

public class CommandFunctions {
    public static void giveBobItem(Collection<ServerPlayerEntity> toPlayer,ServerPlayerEntity cmdOriginator){
        for (ServerPlayerEntity serverPlayerEntity : toPlayer) {
            if (serverPlayerEntity.getInventory().count(Bookofblocks.GUI_ITEM.asItem()) == 0) {
                int emptySlot = serverPlayerEntity.getInventory().getEmptySlot();
                boolean bl = serverPlayerEntity.getInventory().insertStack(Bookofblocks.GUI_ITEM.asItem().getDefaultStack());
                if ((bl) && (emptySlot != -1)) {
                    if (serverPlayerEntity != cmdOriginator) {
                        cmdOriginator.sendMessage(Text.translatable("bob.msg.bobitem.gavePlayer").append(" \"" + serverPlayerEntity.getDisplayName().getString() + "\" ").append(Text.translatable("bob.msg.bobitem.jmItem")), false);
                    }
                    serverPlayerEntity.sendMessage(Text.translatable("bob.msg.bobitem.youGiven"), false);
                } else {
                    if (serverPlayerEntity != cmdOriginator) {
                        cmdOriginator.sendMessage(Text.translatable("bob.msg.bobitem.Player").append(" \"" + serverPlayerEntity.getDisplayName().getString() + "\" ").append(Text.translatable("bob.msg.bobitem.invFull")), false);
                    }
                    serverPlayerEntity.sendMessage(Text.translatable("bob.msg.bobitem.invFullNotice"), false);
                }
            } else {
                cmdOriginator.sendMessage(Text.translatable("bob.msg.bobitem.Player").append(" \"" + serverPlayerEntity.getDisplayName().getString() + "\" ").append(Text.translatable("bob.msg.bobitem.already")), false);
            }
        }
    }
}
