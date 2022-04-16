package com.eternalfragment.mcjourneymode.operators;

import com.eternalfragment.mcjourneymode.Mcjourneymode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.Collection;

public class CommandFunctions {
    public static void giveMjmItem(Collection<ServerPlayerEntity> toPlayer,ServerPlayerEntity cmdOriginator){
        for (ServerPlayerEntity serverPlayerEntity : toPlayer) {
            if (serverPlayerEntity.getInventory().count(Mcjourneymode.GUI_ITEM.asItem()) == 0) {
                int emptySlot = serverPlayerEntity.getInventory().getEmptySlot();
                boolean bl = serverPlayerEntity.getInventory().insertStack(Mcjourneymode.GUI_ITEM.asItem().getDefaultStack());
                if ((bl) && (emptySlot != -1)) {
                    if (serverPlayerEntity != cmdOriginator) {
                        cmdOriginator.sendMessage(Text.of(new TranslatableText("mjm.msg.mjmitem.gavePlayer").getString() +" \"" + serverPlayerEntity.getDisplayName().asString() + "\" "+new TranslatableText("mjm.msg.mjmitem.jmItem").getString()), false);
                    }
                    serverPlayerEntity.sendMessage(Text.of(new TranslatableText("mjm.msg.mjmitem.youGiven").getString()), false);
                } else {
                    if (serverPlayerEntity != cmdOriginator) {
                        cmdOriginator.sendMessage(Text.of(new TranslatableText("mjm.msg.mjmitem.Player").getString()+" \"" + serverPlayerEntity.getDisplayName().asString() + "\" "+new TranslatableText("mjm.msg.mjmitem.invFull").getString()), false);
                    }
                    serverPlayerEntity.sendMessage(Text.of(new TranslatableText("mjm.msg.mjmitem.invFullNotice").getString()), false);
                }
            } else {
                cmdOriginator.sendMessage(Text.of(new TranslatableText("mjm.msg.mjmitem.Player").getString()+" \"" + serverPlayerEntity.getDisplayName().asString() + "\" "+new TranslatableText("mjm.msg.mjmitem.already").getString()), false);
            }
        }
    }
}
