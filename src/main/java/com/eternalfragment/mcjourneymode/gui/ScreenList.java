package com.eternalfragment.mcjourneymode.gui;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ScreenList extends CottonClientScreen {
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    public ScreenList(GuiDescription description) {
        super(description);
    }
}
