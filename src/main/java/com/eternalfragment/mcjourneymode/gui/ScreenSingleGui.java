package com.eternalfragment.mcjourneymode.gui;

import com.eternalfragment.mcjourneymode.Mcjourneymode;
import com.eternalfragment.mcjourneymode.config.Config;
import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.Color;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.text.WordUtils;

import java.util.HashMap;
import java.util.Objects;

@Environment(EnvType.CLIENT)

public class ScreenSingleGui extends LightweightGuiDescription{
    public ScreenSingleGui(HashMap<String, Object[]> data, String searchDefault,boolean perms,String singleName,GuiDescription passThis) {
        EnvType type = FabricLoader.getInstance().getEnvironmentType();
        if (Objects.equals(type.toString(), "CLIENT")) {
            int cellSize=18;
            MinecraftClient mc = MinecraftClient.getInstance();
            PlayerEntity player = MinecraftClient.getInstance().player;
            BackgroundPainter contents = BackgroundPainter.SLOT;
            BackgroundPainter itemSlot = BackgroundPainter.VANILLA;
            Color.RGB textColor_RED = new Color.RGB(153, 3, 3);
            Color.RGB textColor_ORANGE = new Color.RGB(209, 120, 0);
            Color.RGB textColor_GREEN = new Color.RGB(38, 173, 0);
            GuiDescription thisDescription = this;
            double scale = mc.getWindow().getScaleFactor();
            WPlainPanel root = new WPlainPanel();
            WTextField searchBar = new WTextField(Text.of("Search"));
            searchBar.setText(searchDefault);
            int windowWidth = mc.getWindow().getWidth();
            int windowHeight = mc.getWindow().getHeight();
            double wfloor = Math.floor((windowWidth*0.50) / scale);
            double hfloor = Math.floor((windowHeight*0.50) / scale);
            int rootWidth = (int) wfloor;
            int rootHeight = (int) hfloor;
            //root.setSize(rootWidth, rootHeight);
            root.setInsets(Insets.ROOT_PANEL);
            //if loading the config screen for a SINGLE item
            int numItems = data.size();
            if (numItems != 0) {
                Object[] itemData = data.get(singleName);
                //NEW ITEM
                assert player != null;
                int itemCount = player.getInventory().count(Registry.ITEM.get((int)itemData[0]).asItem());
                WPlainPanel item;
                WIconScaling scalingIcon;
                WLabel itemNameLabel;
                WLabel itemIDLabel;
                if ((int)itemData[4] == 1) {
                    //If the item is unlocked, do not show any details here. this screen is ONLY for unlocking items
                }else{
                    //Generate
                    int neededCount = (int) itemData[2] - (int) itemData[5];
                    item = new WPlainPanel();
                    item.setBackgroundPainter(itemSlot);
                    /* Item Image */
                    Item regItem = Registry.ITEM.get((int) itemData[0]).asItem();
                    if (regItem.toString() == null) {
                        regItem = Registry.ITEM.get(1).asItem();
                    }
                    scalingIcon = new WIconScaling(new ItemStack(regItem));
                    scalingIcon.setSize(48, 48);
                    /* Item Name */
                    String itemName = String.valueOf(Registry.ITEM.get((int) itemData[0]).asItem());
                    itemName = itemName.replaceAll("_", " ").toLowerCase();
                    itemName = WordUtils.capitalizeFully(itemName);
                    itemNameLabel = new WLabel(itemName);
                    itemIDLabel = new WLabel(itemName);
                    int lblLen = itemName.length();
                    Identifier barBg = new Identifier(Mcjourneymode.MOD_ID, "prog_bar_bg.png");
                    Identifier barProg = new Identifier(Mcjourneymode.MOD_ID, "prog_bar_2.png");
                    PropertyDelegate thisProp = new PropertyDelegate() {
                        public int[] values = new int[2];

                        @Override
                        public int get(int index) {
                            return values[index];
                        }

                        @Override
                        public void set(int index, int value) {
                            values[index] = value;
                        }

                        @Override
                        public int size() {
                            return values.length;
                        }
                    };
                    WBar achProg = null;
                    if ((Integer) itemData[6] != -1) {
                        //if the data had a verified achievement to track on the server
                        thisProp.set(0, (Integer) itemData[6]);
                        thisProp.set(1, 100);
                        achProg = new WBar(barBg, barProg, 0, 1, WBar.Direction.UP);
                        achProg.setProperties(thisProp);
                        achProg.withTooltip(new TranslatableText("mjm.gui.tooltip.scbProg").getString()+": " + itemData[6] + "%");
                    }
                    //int totalWidth = Math.max(180, scalingIcon[it].getWidth() + itemNameLabel[it].getWidth() + 40);
                    int totalWidth = Math.max(200, scalingIcon.getWidth() + (lblLen * 6));
                    int calcWidth = scalingIcon.getWidth() + (lblLen * 6);

                    /* Cost Details */
                    String progressString = itemData[5] + "/" + itemData[2];
                    int progStrLen = progressString.length() * 6;
                    WLabel progressLabel = new WLabel(progressString);
                    if ((int)itemData[2]!=0){
                        if (((int) itemData[5] / (int) itemData[2]) < 0.5) {
                            progressLabel.setColor(textColor_RED.toRgb());
                        } else {
                            progressLabel.setColor(textColor_ORANGE.toRgb());
                        }}
                    progressLabel.setSize(progStrLen, 12);
                    /* Current Amt in inventory */
                    String containStr = String.valueOf("(" + itemCount + ")");
                    int containStrLen = containStr.length() * 6;
                    WLabel containLabel = new WLabel(containStr);
                    containLabel.setSize(containStrLen, 12);
                    if (itemCount >= neededCount) {
                        containLabel.setColor(textColor_GREEN.toRgb());
                    } else {
                        containLabel.setColor(textColor_RED.toRgb());
                    }
                    /* Slider for payment amt */
                    WLabeledSlider paySlider = null;
                    WLabel noItems = null;
                    if (itemCount != 0) {
                        paySlider = new WLabeledSlider(0, Math.min(neededCount, itemCount), Axis.HORIZONTAL, Text.of("amt"));
                        paySlider.setSize(totalWidth - 64 - 36 - 12, 12);
                        paySlider.setLabelUpdater(value -> new LiteralText("amt: " + value));
                    } else {
                        noItems = new WLabel("-"+new TranslatableText("mjm.gui.lbl.none").getString()+"-");
                    }
                    /*Button for payment submit*/
                    WButton butpay = new WButton(new TranslatableText("mjm.gui.but.pay"));
                    /* Add modules to the panel */
                    item.add(scalingIcon, 6, 6, 48, 48);
                    item.add(itemNameLabel, 56, 5, lblLen * 6, 12);
                    if ((Integer) itemData[6] != -1) {
                        item.add(achProg, 3, 3, 6, 48);
                    }
                    if ((int) itemData[1] != 2)
                    {
                        item.add(progressLabel, 56, 15, progressLabel.getWidth(), 12);
                        item.add(containLabel, progressLabel.getX() + progressLabel.getWidth() + 8, 15);
                        if (itemCount != 0) {
                            item.add(paySlider, 56, 28, paySlider.getWidth(), 12);
                            item.add(butpay, paySlider.getX() + paySlider.getWidth() + 6, 28, 36, 12);
                        } else {
                            item.add(noItems, 56, 28);
                        }
                    }
                    else{
                        item.add(new WLabel("-"+new TranslatableText("mjm.gui.lbl.scbOnly").getString()+"-"), 56, 28);
                    }
                    itemIDLabel.setText(Text.of(String.valueOf(itemData[0])));
                    WLabeledSlider finalPaySlider = paySlider;
                    butpay.setOnClick(() -> {
                        // This code runs on the client when you click the button.
                        assert finalPaySlider != null;
                        int valueTransmit = finalPaySlider.getValue();
                        int[] dataArr = new int[2];
                        dataArr[0] = Integer.parseInt((itemIDLabel.getText().asString()));
                        dataArr[1] = valueTransmit;
                        int itemID = Integer.parseInt(itemIDLabel.getText().asString());
                        Object[] plOb = data.get(String.valueOf(Registry.ITEM.get(itemID).asItem()));
                        plOb[3] = (int)plOb[3] - dataArr[0];
                        data.put(String.valueOf(Registry.ITEM.get(itemID).asItem()), plOb);
                        PacketByteBuf dataclick = PacketByteBufs.create();
                        dataclick.writeIntArray(dataArr);
                        ClientPlayNetworking.send(Mcjourneymode.pay_packet, dataclick);
                    });
                    root.setSize(totalWidth+18,126);
                    WLabel payLbl=new WLabel("Pay Items to Unlock");
                    payLbl.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    root.add(payLbl, cellSize, cellSize,root.getWidth(),18);

                    WButton butBack=new WButton(Text.of("BACK"));
                    butBack.setOnClick(()-> mc.execute(() -> {
                        ScreenList daScreen = new ScreenList(new ScreenListGui(Config.playerConfigMap, "",false));
                        MinecraftClient.getInstance().setScreen(daScreen);
                    }));
                    root.add(butBack,0,0,32,16);
                    item.setSize(totalWidth,66);
                    root.add(item, cellSize, 2*cellSize,totalWidth,66);

                }





            }else{
                WLabel lblEmpty = new WLabel(new TranslatableText("mjm.gui.lbl.noneAvail"));
                lblEmpty.setColor(textColor_RED.toRgb());
                root.add(lblEmpty, 1, 1);
                root.setSize(2,4);
            }

            root.validate(passThis);













            if (perms) {
                WButton configButton = new WButton(new LiteralText("⚙"));
                configButton.setOnClick(() -> {
                    PacketByteBuf clickData = PacketByteBufs.create();
                    ClientPlayNetworking.send(Mcjourneymode.send_config_req_packet, clickData);
                });
                root.add(configButton, (root.getWidth() / 18) - 2, 1, 1, 1);
            }
            setRootPanel(root);
            root.validate(this);
        }
    }
}
