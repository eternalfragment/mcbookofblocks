package com.eternalfragment.mcjourneymode.config;

import com.eternalfragment.mcjourneymode.Mcjourneymode;
import net.minecraft.util.registry.Registry;

import java.util.Objects;

public class GetItemIdFromName {

    public static int getItemIdFromName(String search) {
        search = search.toLowerCase();
        int numItems = (int) Registry.ITEM.stream().count();
        for (int i = 0; i < numItems; i++) {
            String itemName = Registry.ITEM.get(i).asItem().toString();

            if (Objects.equals(itemName, search)) {
                if (itemName=="crimson_nylium"){System.out.println("CRIMSON STUFF: "+i);}
                if (i==19){
                    System.out.println("19 found||| Search: "+search+" | itemName: "+itemName);

                }
                return i;
            }
        }
        Mcjourneymode.mylogger.atWarn().log("Naming Mismatch: " + search + " ; Please Correct Config file");
        return -1;
    }
}
