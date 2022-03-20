package com.eternalfragment.mcjourneymode.gui;

import com.eternalfragment.mcjourneymode.Mcjourneymode;
import com.eternalfragment.mcjourneymode.config.Config;
import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.util.TriState;
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
import java.util.function.Consumer;
@Environment(EnvType.CLIENT)
public class ScreenListGui extends LightweightGuiDescription {
    public ScreenListGui(HashMap<String, Object[]> data, String searchDefault,boolean perms) {
        EnvType type = FabricLoader.getInstance().getEnvironmentType();
        if (type.toString() == "CLIENT") {

            class contentGenerator {
                static final int cellHpadding = 15;
                static final int cellVpadding = 8;
                public static Identifier pay_packet = null;
                public static Identifier give_packet = null;

                static void generatePanels(HashMap<String, Object[]> newData, boolean unlocked, String FilterTxt, WScrollPanel[] wrapContents, WGridPanel root, GuiDescription passThis, boolean perms) {
                    //TODO: establish mod's general config passing to this method for proper generation? or.. pass to parent method to generate at window open
                    root.remove(wrapContents[0]);
                    Config.playerConfigMap = newData;
                    String filterCheck = FilterTxt.replaceAll(" ", "").toLowerCase();
                    if (!(filterCheck.length() > 0)) {
                        FilterTxt = "";
                    }//if filter is only spaces, ignore
                    String filter = FilterTxt.replaceAll(" ", "_").toLowerCase();
                    BackgroundPainter contents = BackgroundPainter.SLOT;
                    BackgroundPainter itemSlot = BackgroundPainter.VANILLA;
                    PlayerEntity player = MinecraftClient.getInstance().player;
                    WPlainPanel myTallPanel = new WPlainPanel();
                    WScrollPanel rem_scrollPanel = new WScrollPanel(myTallPanel);
                    int scrollW = (root.getWidth() / 18) - 2;
                    int scrollH = (root.getHeight() / 18) - 4;
                    rem_scrollPanel.setSize(scrollW, scrollH);
                    rem_scrollPanel.setScrollingHorizontally(TriState.FALSE);
                    rem_scrollPanel.setScrollingVertically(TriState.TRUE);
                    rem_scrollPanel.setBackgroundPainter(contents);
                    Color.RGB textColor_RED = new Color.RGB(153, 3, 3);
                    Color.RGB textColor_ORANGE = new Color.RGB(209, 120, 0);
                    Color.RGB textColor_GREEN = new Color.RGB(38, 173, 0);
                    HashMap<String, Object[]> playerUnlockMap;

                    int numItems = newData.size();
                    if (numItems != 0) {
                        int maxItems = 50;
                        int generatedItems = 0;
                        playerUnlockMap = newData;
                        WPlainPanel[] item = new WPlainPanel[numItems];
                        WIconScaling[] scalingIcon = new WIconScaling[numItems];
                        WLabel[] itemNameLabel = new WLabel[numItems];
                        WLabel[] itemIDLabel = new WLabel[numItems];
                        WLabel[] progressLabel = new WLabel[numItems];
                        WLabel[] containLabel = new WLabel[numItems];
                        WLabeledSlider[] paySlider = new WLabeledSlider[numItems];
                        WButton[] butpay = new WButton[numItems];
                        WLabel[] noItems = new WLabel[numItems];
                        WBar[] achProg=new WBar[numItems];
                        WButton[] butEdit = new WButton[numItems];
                        PropertyDelegate[] thisProp= new PropertyDelegate[numItems];

                        int[] rowTracker;
                        int it = 0;
                        int currentRow = 0;
                        int currentCol = 0;
                        int maxWidth = rem_scrollPanel.getWidth() * 18;
                        Object[][] ALLData = new Object[numItems][6];
                        Object[][] unlockedData = new Object[numItems][6];
                        Object[][] potentialData = new Object[numItems][6];
                        Object[][] progressData = new Object[numItems][6];
                        int ud = 0;
                        int pd = 0;
                        int prd = 0;
                        int ttd = 0;
                        int ad = 0;
                        for (String name : playerUnlockMap.keySet()) {
                            Object[] itemData = playerUnlockMap.get(name);
                            //if the data has been transmitted with an injected 'not researchable' for any reason, abort
                            if ((Integer)itemData[1]>0){
                                //0-ID, 1-Unlockable, 2-Req amt, 3-Give amt, 4-Player Unlocked, 5-Paid Amt, 6-Research Percentage
                                //TODO: Add an int tracker that reports the % done of an objective, and add to the progress if its in progress
                                //ScoreboardObjective thisObjective = player.getScoreboard().getObjective((String) itemConfig[4]);
                                //if the item is unlocked, add to unlocked array
                                if ((int) itemData[4] == 1) {
                                    unlockedData[ud] = itemData;
                                    ud++;
                                    ttd++;
                                } else if (((int) itemData[5] > 0) || ((int) itemData[6] > 0)) {
                                    //if player has started the process of unlocking add to list
                                    progressData[prd] = itemData;
                                    prd++;
                                    ttd++;
                                } else if ((player.getInventory().count(Registry.ITEM.get((int) itemData[0]).asItem()) > 0)&&(((int)itemData[1]==1)||((int)itemData[1]==3)||((int)itemData[1]==4))) {
                                    //if the player has the item in their inventory, and the item is required in the research status
                                    potentialData[pd] = itemData;
                                    pd++;
                                    ttd++;
                                } else {
                                    ALLData[ad] = itemData;
                                    ad++;
                                    ttd++;
                                }
                            }
                        }
                        int _pd = 0;
                        int _prd = 0;
                        int _ud = 0;
                        int _ad = 0;
                        int numToGen = 0;
                        if ((!filter.equals(""))) {
                            //if the filter is 'on' load through all the entires
                            numToGen = ttd;
                        } else {
                            numToGen = pd + prd + ud;
                        }
                        for (int ccd = 0; ccd < numToGen; ccd++) {
                            Object[] itemData = new Object[6];
                            boolean ranItem = true;
                            if (_ud < ud) {
                                //if the number of generated unlockables is less than the number of available
                                ranItem = false;
                                itemData = unlockedData[_ud];
                                _ud++;
                            } else if (_prd < prd) {
                                ranItem = false;
                                itemData = progressData[_prd];
                                _prd++;
                            } else if (_pd < pd) {
                                ranItem = false;
                                itemData = potentialData[_pd];
                                _pd++;
                            }
                            if ((ccd >= (ud + prd + pd)) || ((ud + prd + pd) == 0)) {
                                //if the counter is beyond the 3 sorts, or the 3 sorts are empty, set the data to all the data (used for text-search)
                                ranItem = true;
                                itemData = ALLData[_ad];
                                _ad++;
                            }
                            boolean showItem = false;
                            String name = "" + Registry.ITEM.get((int)itemData[0]).asItem().getName();
                            if (!ranItem) {
                                //if its not a random item, show it by default
                                showItem = true;
                            }
                            if (unlocked) {
                                //if unlocked filter is set, and this object is unlocked
                                //if the item isn't unlocked, do not show when filter set
                                showItem = (int) itemData[4] == 1;
                            }
                            if ((int)itemData[0] == 0) {
                                showItem = false;
                            }
                            if ((int)itemData[1] == 0) {
                                showItem = false;
                            }
                            if ((!filter.equals(""))) {
                                //if the filter is not empty
                                //if the name of the object contains the filter
                                showItem = name.contains(filter);
                            }
                            if (generatedItems >= maxItems) {
                                //if the number of items generated is already more than the max limit, do not display any more
                                showItem = false;
                            }
                            if (showItem) {
                                generatedItems++;
                                //NEW ITEM
                                int itemCount = player.getInventory().count(Registry.ITEM.get((int)itemData[0]).asItem());
                                if ((int)itemData[4] == 1) {
                                    //ITEM IS UNLOCEKD
                                    item[it] = new WPlainPanel();
                                    item[it].setBackgroundPainter(itemSlot);
                                    /* Item Image */
                                    Item regItem = Registry.ITEM.get((int)itemData[0]).asItem();
                                    if (regItem.toString() == null) {
                                        regItem = Registry.ITEM.get(1).asItem();
                                    }
                                    scalingIcon[it] = new WIconScaling(new ItemStack(regItem));
                                    scalingIcon[it].setSize(48, 48);
                                    /* Item Name */
                                    String itemName = String.valueOf(Registry.ITEM.get((int)itemData[0]).asItem());
                                    itemName = itemName.replaceAll("_", " ").toLowerCase();
                                    itemName = WordUtils.capitalizeFully(itemName);
                                    itemNameLabel[it] = new WLabel(itemName);
                                    itemIDLabel[it] = new WLabel(itemName);
                                    int lblLen = itemName.length();
                                    int totalWidth = Math.max(160, scalingIcon[it].getWidth() + (lblLen * 6));
                                    /*Button for Giving Item*/
                                    butpay[it] = new WButton(new TranslatableText("mjm.gui.but.get"));
                                    /* Add modules to the panel */
                                    item[it].add(scalingIcon[it], 6, 6, 48, 48);
                                    item[it].add(itemNameLabel[it], 56, 5, lblLen * 6, 12);
                                    item[it].add(butpay[it], 56, 28, totalWidth - 70, 12);
                                    itemIDLabel[it].setText(Text.of(String.valueOf(itemData[0])));
                                    int holdMe = it;
                                    butpay[it].setOnClick(() -> {
                                        // This code runs on the client when you click the button.
                                        int itemID = Integer.parseInt(itemIDLabel[holdMe].getText().asString());
                                        PacketByteBuf data = PacketByteBufs.create();
                                        data.writeInt(itemID);
                                        ClientPlayNetworking.send(Mcjourneymode.give_packet, data);
                                    });
                                    if (perms){
                                        butEdit[it]=new WButton(new LiteralText("⚙"));
                                        Object[] finalItemData = itemData;
                                        butEdit[it].setOnClick(()->{
                                            PacketByteBuf clickData = PacketByteBufs.create();
                                            clickData.writeString(String.valueOf(Registry.ITEM.get((int) finalItemData[0]).asItem()));
                                            ClientPlayNetworking.send(Mcjourneymode.send_single_config_req_packet, clickData);
                                        });
                                        item[it].add(butEdit[it],totalWidth-18,3,12,8);
                                    }

                                    item[it].setSize(totalWidth, 60);
                                    int currentWidth = 0;
                                    int lastItemW = 0;
                                    for (int j = 0; j < it; j++) {
                                        if (item[j].getY() > (currentRow * (60 + cellVpadding))) {
                                            currentWidth = currentWidth + item[j].getWidth() + cellHpadding;
                                            lastItemW = item[j].getWidth();
                                        }
                                    }
                                    if ((currentWidth + item[it].getWidth() + cellHpadding) > maxWidth) {
                                        //if adding this item will be wider than the max width of the panel, change row and width
                                        currentWidth = 0;
                                        currentRow++;
                                    }
                                    myTallPanel.add(item[it], currentWidth + cellHpadding, (currentRow * (60 + cellVpadding)) + 1, totalWidth, item[it].getHeight());
                                    it++;
                                } else {
                                    //Generate
                                    int neededCount = (int) itemData[2] - (int) itemData[5];
                                    item[it] = new WPlainPanel();
                                    item[it].setBackgroundPainter(itemSlot);
                                    /* Item Image */
                                    Item regItem = Registry.ITEM.get((int) itemData[0]).asItem();
                                    if (regItem.toString() == null) {
                                        regItem = Registry.ITEM.get(1).asItem();
                                    }
                                    scalingIcon[it] = new WIconScaling(new ItemStack(regItem));
                                    scalingIcon[it].setSize(48, 48);
                                    /* Item Name */
                                    String itemName = String.valueOf(Registry.ITEM.get((int) itemData[0]).asItem());
                                    itemName = itemName.replaceAll("_", " ").toLowerCase();
                                    itemName = WordUtils.capitalizeFully(itemName);
                                    itemNameLabel[it] = new WLabel(itemName);
                                    itemIDLabel[it] = new WLabel(itemName);
                                    int lblLen = itemName.length();
                                    Identifier barBg = new Identifier(Mcjourneymode.MOD_ID, "prog_bar_bg.png");
                                    Identifier barProg = new Identifier(Mcjourneymode.MOD_ID, "prog_bar_2.png");
                                    thisProp[it] = new PropertyDelegate() {
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
                                    if ((Integer) itemData[6] != -1) {
                                        //if the data had a verified achievement to track on the server
                                        thisProp[it].set(0, (Integer) itemData[6]);
                                        thisProp[it].set(1, 100);
                                        achProg[it] = new WBar(barBg, barProg, 0, 1, WBar.Direction.UP);
                                        achProg[it].setProperties(thisProp[it]);
                                        achProg[it].withTooltip(new TranslatableText("mjm.gui.tooltip.scbProg").getString()+": " + itemData[6] + "%");
                                    }
                                    //int totalWidth = Math.max(180, scalingIcon[it].getWidth() + itemNameLabel[it].getWidth() + 40);
                                    int totalWidth = Math.max(160, scalingIcon[it].getWidth() + (lblLen * 6));
                                    int calcWidth = scalingIcon[it].getWidth() + (lblLen * 6);

                                    /* Cost Details */
                                    String progressString = itemData[5] + "/" + itemData[2];
                                    int progStrLen = progressString.length() * 6;
                                    progressLabel[it] = new WLabel(progressString);
                                    if ((int)itemData[2]!=0){
                                    if (((int) itemData[5] / (int) itemData[2]) < 0.5) {
                                        progressLabel[it].setColor(textColor_RED.toRgb());
                                    } else {
                                        progressLabel[it].setColor(textColor_ORANGE.toRgb());
                                    }}
                                    progressLabel[it].setSize(progStrLen, 12);
                                    /* Current Amt in inventory */
                                    String containStr = String.valueOf("(" + itemCount + ")");
                                    int containStrLen = containStr.length() * 6;
                                    containLabel[it] = new WLabel(containStr);
                                    containLabel[it].setSize(containStrLen, 12);
                                    if (itemCount >= neededCount) {
                                        containLabel[it].setColor(textColor_GREEN.toRgb());
                                    } else {
                                        containLabel[it].setColor(textColor_RED.toRgb());
                                    }
                                    /* Slider for payment amt */
                                    if (itemCount != 0) {
                                        paySlider[it] = new WLabeledSlider(0, Math.min(neededCount, itemCount), Axis.HORIZONTAL, Text.of("amt"));
                                        paySlider[it].setSize(totalWidth - 64 - 36 - 12, 12);
                                        paySlider[it].setLabelUpdater(value -> new LiteralText("amt: " + value));
                                    } else {
                                        noItems[it] = new WLabel("-"+new TranslatableText("mjm.gui.lbl.none").getString()+"-");
                                    }
                                    /*Button for payment submit*/
                                    butpay[it] = new WButton(new TranslatableText("mjm.gui.but.pay"));
                                    /* Add modules to the panel */
                                    item[it].add(scalingIcon[it], 6, 6, 48, 48);
                                    item[it].add(itemNameLabel[it], 56, 5, lblLen * 6, 12);
                                    if ((Integer) itemData[6] != -1) {
                                        item[it].add(achProg[it], 3, 3, 6, 48);
                                    }
                                    if ((int) itemData[1] != 2)
                                    {
                                        item[it].add(progressLabel[it], 56, 15, progressLabel[it].getWidth(), 12);
                                        item[it].add(containLabel[it], progressLabel[it].getX() + progressLabel[it].getWidth() + 8, 15);
                                        if (itemCount != 0) {
                                            item[it].add(paySlider[it], 56, 28, paySlider[it].getWidth(), 12);
                                            item[it].add(butpay[it], paySlider[it].getX() + paySlider[it].getWidth() + 6, 28, 36, 12);
                                        } else {
                                            item[it].add(noItems[it], 56, 28);
                                        }
                                    }
                                    else{
                                        item[it].add(new WLabel("-"+new TranslatableText("mjm.gui.lbl.scbOnly").getString()+"-"), 56, 28);
                                    }
                                    itemIDLabel[it].setText(Text.of(String.valueOf(itemData[0])));
                                    int holdMe = it;
                                    butpay[it].setOnClick(() -> {
                                        // This code runs on the client when you click the button.
                                        int valueTransmit = paySlider[holdMe].getValue();
                                        int[] dataArr = new int[2];
                                        dataArr[0] = Integer.parseInt((itemIDLabel[holdMe].getText().asString()));
                                        dataArr[1] = valueTransmit;
                                        int itemID = Integer.parseInt(itemIDLabel[holdMe].getText().asString());
                                        Object[] plOb = playerUnlockMap.get(String.valueOf(Registry.ITEM.get(itemID).asItem()));
                                        plOb[3] = (int)plOb[3] - dataArr[0];
                                        playerUnlockMap.put(String.valueOf(Registry.ITEM.get(itemID).asItem()), plOb);
                                        PacketByteBuf data = PacketByteBufs.create();
                                        data.writeIntArray(dataArr);
                                        ClientPlayNetworking.send(Mcjourneymode.pay_packet, data);
                                    });
                                    if (perms){
                                        butEdit[it]=new WButton(new LiteralText("⚙"));
                                        Object[] finalItemData1 = itemData;
                                        butEdit[it].setOnClick(()->{
                                            PacketByteBuf clickData = PacketByteBufs.create();
                                            clickData.writeString(String.valueOf(Registry.ITEM.get((int) finalItemData1[0]).asItem()));
                                            ClientPlayNetworking.send(Mcjourneymode.send_single_config_req_packet, clickData);
                                        });
                                        item[it].add(butEdit[it],totalWidth-18,3,12,8);
                                    }
                                    item[it].setSize(totalWidth, 60);
                                    int currentWidth = 0;
                                    int lastItemW = 0;
                                    for (int j = 0; j < it; j++) {
                                        if (item[j].getY() > (currentRow * (60 + cellVpadding))) {
                                            currentWidth = currentWidth + item[j].getWidth() + cellHpadding;
                                            lastItemW = item[j].getWidth();
                                        }
                                    }
                                    if ((currentWidth + item[it].getWidth() + cellHpadding) > maxWidth) {
                                        //if adding this item will be wider than the max width of the panel, change row and width
                                        currentWidth = 0;
                                        currentRow++;
                                    }
                                    myTallPanel.add(item[it], currentWidth + cellHpadding, (currentRow * (60 + cellVpadding)) + 1, totalWidth, item[it].getHeight());
                                    it++;
                                }
                            }
                        }
                        if (generatedItems == 0) {
                            //this means nothing was displayed due to filter settings
                            WLabel lblEmpty = new WLabel("-"+new TranslatableText("mjm.gui.lbl.search").getString()+"-");
                            lblEmpty.setColor(textColor_RED.toRgb());
                            myTallPanel.add(lblEmpty, 25, 25);
                        }
                    } else {
                        //this means nothing was loaded. means admin disabled all items, or glitch with config.
                        WLabel lblEmpty = new WLabel(new TranslatableText("mjm.gui.lbl.noneAvail"));
                        lblEmpty.setColor(textColor_RED.toRgb());
                        myTallPanel.add(lblEmpty, 25, 25);
                    }
                    wrapContents[0] = rem_scrollPanel;//flag new panel to be deleted at next refresh
                    root.add(rem_scrollPanel, 1, 3, scrollW, scrollH);
                    root.validate(passThis);
                }
            }
        MinecraftClient mc = MinecraftClient.getInstance();
        GuiDescription thisDescription = this;
        WScrollPanel[] wrapContents = new WScrollPanel[1];
        double scale = mc.getWindow().getScaleFactor();
        WGridPanel root = new WGridPanel();
        WGridPanel myTallPanel = new WGridPanel();
        WTextField searchBar = new WTextField(Text.of("Search"));
        searchBar.setText(searchDefault);
        int windowWidth = mc.getWindow().getWidth();
        int windowHeight = mc.getWindow().getHeight();
        double wfloor = Math.floor((windowWidth*0.60) / scale);
        double hfloor = Math.floor((windowHeight*0.75) / scale);
        int rootWidth = (int) wfloor;
        int rootHeight = (int) hfloor;
        root.setSize(rootWidth, rootHeight);
        int searchScale = (root.getWidth() / 18) - 10;
        WScrollPanel scrollPanel = new WScrollPanel(myTallPanel);
        WToggleButton toggleButton = new WToggleButton(new LiteralText("Only Unlocked"));
        toggleButton.setOnToggle(on -> {
            String filterText = searchBar.getText();
            // This code runs on the client when you toggle the button.
            boolean onlyUnlocked = toggleButton.getToggle();
            contentGenerator.generatePanels(data, toggleButton.getToggle(), filterText, wrapContents, root, thisDescription,perms);
        });
        Consumer<String> searchConsumer = new Consumer<String>() {
            @Override
            public void accept(String s) {
                String filterText = searchBar.getText();
                boolean onlyUnlocked = toggleButton.getToggle();
                contentGenerator.generatePanels(data, toggleButton.getToggle(), filterText, wrapContents, root, thisDescription,perms);
            }
        };
        searchBar.setChangedListener(searchConsumer);
        root.add(searchBar, 1, 1, searchScale, 1);
        int searchButtonX = (int) Math.floor((searchBar.getX() + searchBar.getWidth()) / 18) + 1;
        root.add(toggleButton, searchButtonX, 1, 2, 1);
        if (perms) {
            WButton configButton = new WButton(new LiteralText("⚙"));
            configButton.setOnClick(() -> {
                PacketByteBuf clickData = PacketByteBufs.create();
                ClientPlayNetworking.send(Mcjourneymode.send_config_req_packet, clickData);
            });
            root.add(configButton, (root.getWidth() / 18) - 2, 1, 1, 1);
        }
        contentGenerator.generatePanels(data, toggleButton.getToggle(), searchDefault, wrapContents, root, thisDescription,perms);
        setRootPanel(root);
        root.validate(this);
    }
    }
}
