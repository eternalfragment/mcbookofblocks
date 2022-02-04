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
                return i;
            }
        }
        Mcjourneymode.mylogger.atWarn().log("Naming Mismatch: " + search + " ; Please Correct Config file");
        return -1;
    }
}
