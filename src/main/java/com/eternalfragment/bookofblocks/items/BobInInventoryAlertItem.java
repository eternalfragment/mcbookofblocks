package com.eternalfragment.bookofblocks.items;

import net.minecraft.item.Item;
//This item is only used for puting an 'alert' over items in the MJM Gui. Does not show up in creative menu
//item should be set to limit of 0, so it cannot be given to player either
public class BobInInventoryAlertItem extends Item {
    public BobInInventoryAlertItem(Settings settings) {
        super(settings);
    }
}
