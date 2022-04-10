package com.eternalfragment.mcjourneymode.operators;

import com.eternalfragment.mcjourneymode.Mcjourneymode;
import com.eternalfragment.mcjourneymode.config.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

public class invManager {

    public static int inv_PayItem(PlayerEntity player, int itemID, int amt){
        PlayerInventory pI = player.getInventory();
        Item item = Registry.ITEM.get(itemID).asItem();
        int numInInventory=pI.count(Registry.ITEM.get(itemID));
        if (numInInventory>=amt) {
            int numRemoved=0;
            boolean keepSearch=true;
            int invSize = pI.size();
            ItemStack findStack = Registry.ITEM.get(itemID).getDefaultStack();
            while (keepSearch){
                if (numRemoved<amt){
                    int amtLeftRemove=amt-numRemoved;
                    int itemIndex = pI.indexOf(findStack);
                    if (itemIndex >=0){
                    int numInStack = pI.getStack(itemIndex).getCount();
                    int removeAmt=Math.min(numInStack,amtLeftRemove);
                    pI.removeStack(itemIndex,removeAmt);
                    numRemoved=numRemoved+removeAmt;
                    }else{
                        keepSearch=false;
                    }
                }else{
                    keepSearch=false;
                }
            }
            return numRemoved;
        }else {
            Mcjourneymode.mylogger.atError().log("[PayItem]... ERROR IN TRANSMISSION -- invalid amts");
            return 0;
        }
    }
    public static void inv_giveItem(ServerPlayerEntity player, int itemID, boolean fullStack){
        int amt=1;
        String itemName=String.valueOf(Registry.ITEM.get(itemID).asItem());
        Object[] iConfig = Config.configMap.get(itemName);
        if (fullStack) {amt=(int)iConfig[3];}
        for (int i=0; i<amt; i++){
            if(!player.getInventory().insertStack(new ItemStack(Registry.ITEM.get(itemID).asItem()))){
                //Inventory full?
                break;
            }
        }
    }
    public static int inv_clearItem(ServerPlayerEntity player,int itemID){
        ItemStack findStack = Registry.ITEM.get(itemID).getDefaultStack();
        boolean keepSearch = true;
        int numRemoved=0;
        int numInInventory=player.getInventory().count(Registry.ITEM.get(itemID));
        while (keepSearch){
            if (numRemoved<numInInventory){
                int amtLeftRemove=numInInventory-numRemoved;
                int itemIndex = player.getInventory().indexOf(findStack);
                if (itemIndex >=0){
                    int numInStack = player.getInventory().getStack(itemIndex).getCount();
                    int removeAmt=Math.min(numInStack,amtLeftRemove);
                    player.getInventory().removeStack(itemIndex,removeAmt);
                    numRemoved=numRemoved+removeAmt;
                }else{
                    keepSearch=false;
                }
            }else{
                keepSearch=false;
            }
        }
        return numRemoved;

    }

}
