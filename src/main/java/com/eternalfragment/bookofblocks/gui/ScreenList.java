package com.eternalfragment.bookofblocks.gui;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ScreenList extends CottonClientScreen {
    @Override
    public boolean shouldPause() {
        return false;
    }
    public ScreenList(GuiDescription description) {
        super(description);
    }
}
