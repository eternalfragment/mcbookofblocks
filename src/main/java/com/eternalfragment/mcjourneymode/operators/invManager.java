package com.eternalfragment.mcjourneymode.operators;

import com.eternalfragment.mcjourneymode.config.Config;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.function.Predicate;

public class invManager {

    public static int inv_PayItem(PlayerEntity player, int itemID, int amt){
        //System.out.println("Loading Inventory Manager...");
        //System.out.println("player contacted: "+player.getName().asString());
        PlayerInventory pI = player.getInventory();
        //System.out.println("... inventory acquired");
        Item item = Registry.ITEM.get(itemID).asItem();
        int numInInventory=pI.count(Registry.ITEM.get(itemID));
        //System.out.println("... item counted");
        if (numInInventory>=amt) {
            //System.out.println("... have more than amt");

            int numRemoved=0;
            boolean keepSearch=true;
            int invSize = pI.size();
            //System.out.println("InventorySize: "+invSize);
            ItemStack findStack = Registry.ITEM.get(itemID).getDefaultStack();
            while (keepSearch==true){
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
            System.out.println("... ERROR IN TRANSMISSION -- invalid amts");
            return 0;
        }
    }

    public static void inv_giveItem(ServerPlayerEntity player, int itemID){
        int amt=1;
        String itemName=String.valueOf(Registry.ITEM.get(itemID).asItem());

        Object[] iConfig = Config.configMap.get(itemName);
        amt=(int)iConfig[3];
        for (int i=0; i<amt; i++){
            if(!player.getInventory().insertStack(new ItemStack(Registry.ITEM.get(itemID).asItem()))){
                //Inventory full?
                break;
            }
        }
    }


}
