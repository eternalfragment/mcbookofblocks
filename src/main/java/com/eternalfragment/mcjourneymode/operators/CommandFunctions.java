package com.eternalfragment.mcjourneymode.operators;

import com.eternalfragment.mcjourneymode.Mcjourneymode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Iterator;

public class CommandFunctions {
    public static void giveMjmItem(Collection<ServerPlayerEntity> toPlayer,ServerPlayerEntity cmdOriginator){
        Iterator i1 = toPlayer.iterator();
        while (i1.hasNext()){
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)i1.next();
            if (serverPlayerEntity.getInventory().count(Mcjourneymode.GUI_ITEM.asItem())==0) {
                int emptySlot=serverPlayerEntity.getInventory().getEmptySlot();
                boolean bl = serverPlayerEntity.getInventory().insertStack(Mcjourneymode.GUI_ITEM.asItem().getDefaultStack());
                if ((bl)&&(emptySlot!=-1)){
                    if (serverPlayerEntity!=cmdOriginator) {
                        cmdOriginator.sendMessage(Text.of("Gave Player \"" + serverPlayerEntity.getDisplayName().asString() + "\" JourneyMode Item"), false);
                    }
                    serverPlayerEntity.sendMessage(Text.of("You have been given the JourneyMode Item"),false);
                }else{
                    if (serverPlayerEntity!=cmdOriginator) {
                        cmdOriginator.sendMessage(Text.of("Player \"" + serverPlayerEntity.getDisplayName().asString() + "\" inventory full"), false);
                    }
                    serverPlayerEntity.sendMessage(Text.of("Attempting to give JourneyMode Item...... Inventory Full"),false);
                }
            }else{
                cmdOriginator.sendMessage(Text.of("Player \""+serverPlayerEntity.getDisplayName().asString()+"\" already has JourneyMode Item"),false);
            }
        }
    }
}
