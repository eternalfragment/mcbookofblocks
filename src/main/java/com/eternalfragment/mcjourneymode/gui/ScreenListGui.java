package com.eternalfragment.mcjourneymode.gui;

import com.eternalfragment.mcjourneymode.Mcjourneymode;
import com.eternalfragment.mcjourneymode.config.Config;
import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Color;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Consumer;



class ObjectStats{
    private int ItemID;
    private int Researchable;
    private int Req_Amt;
    private int Give_Amt;
    private int Paid_Amt;
    private int Unlocked;
    private int Category;
    private String Group;

    //playerConfigMap: ['ItemName'][0-ITEM ID, 1-researchable,2-req_amt,3-give_amt, 4-paid_amt, 5-unocked];
    //[ID]-
    //[0]-ItemID
    //[1]-Researchable
    //[2]-req_amt
    //[3]-give_amt
    //[4]-paid_amt
    //[5]-unlocked
    //[6]-Category: 1-unlocked, 2-potential, 3-progress, 4-other
    //[7]-Group (string from MC index)

    public void setValues(int id,int res,int req,int giv,int pd,int ul,int ca,String gr){
        ItemID=id;
        Researchable=res;
        Req_Amt=req;
        Give_Amt=giv;
        Paid_Amt=pd;
        Unlocked=ul;
        Group=gr;
        Category=ca;}
    public int getItemID(){return ItemID;}
    public int getResearchable(){return Researchable;}
    public int getReq_Amt(){return Req_Amt;}
    public int getPaid_Amt(){return Paid_Amt;}
    public int getGive_Amt(){return Give_Amt;}
    public int getUnlocked(){return Unlocked;}
    public int getCategory(){return Category;}
    public String getGroup(){return Group;}
}

@Environment(EnvType.CLIENT)
public class ScreenListGui extends LightweightGuiDescription{

    // Function to sort by column
    public static void sortbyCategory(Object arr[][])
    {
        // Using built-in sort function Arrays.sort
        Arrays.sort(arr, new Comparator<Object[]>() {

            @Override
            // Compare values according to columns
            public int compare(final Object[] entry1,
                               final Object[] entry2) {

                // To sort in descending order revert
                // the '>' Operator
                //System.out.println(entry1[7]);
                //System.out.println(Arrays.toString(entry1));
                if (entry1[7]!=null) {
                    String ob1 = (String) entry1[7];
                    String ob2 = (String) entry2[7];
                    if (ob1.charAt(1) > ob2.charAt(1))
                        return 1;
                    else
                        return -1;
                }
                return 0;
            }
        });  // End of function call sort().
    }

    public ScreenListGui(HashMap<String, Object[]> data, String searchDefault,boolean perms) {
        EnvType type = FabricLoader.getInstance().getEnvironmentType();
        if (type.toString() == "CLIENT") {

            class contentGenerator {
                static void generatePanels(HashMap<String, Object[]> newData, String FilterTxt, WScrollPanel[] wrapContents, WGridPanel root, GuiDescription passThis, boolean perms) {
                    //TODO: establish mod's general config passing to this method for proper generation? or.. pass to parent method to generate at window open
                    root.remove(wrapContents[0]);
                    int jmItemSize = 20;
                    Config.playerConfigMap = newData;
                    String filterCheck = FilterTxt.replaceAll(" ", "").toLowerCase();
                    if (!(filterCheck.length() > 0)) {
                        FilterTxt = "";
                    }//if filter is only spaces, ignore
                    String filter = FilterTxt.replaceAll(" ", "_").toLowerCase();
                    BackgroundPainter contents = BackgroundPainter.SLOT;
                    BackgroundPainter itemSlot = BackgroundPainter.VANILLA;
                    PlayerEntity player = MinecraftClient.getInstance().player;
                    MinecraftClient mc = MinecraftClient.getInstance();
                    double scale = mc.getWindow().getScaleFactor();
                    int windowWidth = mc.getWindow().getWidth();
                    int windowHeight = mc.getWindow().getHeight();
                    double wfloor = Math.floor((windowWidth * 0.60) / scale);
                    double hfloor = Math.floor((windowHeight * 0.75) / scale);
                    int rootWidth = (int) wfloor;
                    int rootHeight = (int) hfloor;
                    root.setSize(rootWidth, rootHeight);
                    root.setInsets(Insets.ROOT_PANEL);
                    WGridPanel myTallPanel = new WGridPanel(jmItemSize);
                    WScrollPanel rem_scrollPanel = new WScrollPanel(myTallPanel);
                    int scrollW = (root.getWidth() / 18) - 2;
                    int scrollH = (root.getHeight() / 18) - 2;
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
                        int maxItems = 999;
                        int generatedItems = 0;
                        playerUnlockMap = newData;

                        //Array of containers for objects in the gui

                        WJMItem[] jmItem = new WJMItem[numItems];
                        WJMItemAlert[] alertIcon=new WJMItemAlert[numItems];
                        WGridPanel[] jmItemSlot = new WGridPanel[numItems];
                        WGridPanel panelUnlocked = new WGridPanel(jmItemSize);
                        WGridPanel panelProgress = new WGridPanel(jmItemSize);
                        WGridPanel panelPotential = new WGridPanel(jmItemSize);
                        WGridPanel panelAll = new WGridPanel(jmItemSize);
                        WLabel lblUnlocked=new WLabel(Text.of("-"+new TranslatableText("mjm.gui.lbl.noDisplay").getString()+"-"));
                        lblUnlocked.setColor(textColor_RED.toRgb());
                        panelUnlocked.add(lblUnlocked,0,0);
                        WLabel lblProgress=new WLabel(Text.of("-"+new TranslatableText("mjm.gui.lbl.noDisplay").getString()+"-"));
                        lblProgress.setColor(textColor_RED.toRgb());
                        panelProgress.add(lblProgress,0,0);
                        WLabel lblPotential=new WLabel(Text.of("-"+new TranslatableText("mjm.gui.lbl.noDisplay").getString()+"-"));
                        lblPotential.setColor(textColor_RED.toRgb());
                        panelPotential.add(lblPotential,0,0);
                        WLabel lblAll=new WLabel(Text.of("-"+new TranslatableText("mjm.gui.lbl.noDisplay").getString()+"-"));
                        lblAll.setColor(textColor_RED.toRgb());
                        panelAll.add(lblAll,0,0);
                        int panelMaxW = (int) Math.floor((scrollW * 18) / 20) - 2;
                        int[] rowTracker;
                        int it = 0;
                        int currentRow = 0;
                        int currentCol = 0;
                        int maxWidth = rem_scrollPanel.getWidth() * jmItemSize;
                        Object[][] ALLData = new Object[numItems][6];
                        Object[][] unlockedData = new Object[numItems][6];//items already unlocked
                        Object[][] potentialData = new Object[numItems][6];//Items in inventory & able to unlock
                        Object[][] progressData = new Object[numItems][6];//Player has started to unlock
                        int ud = 0;
                        int pd = 0;
                        int prd = 0;
                        int ttd = 0;
                        int ad = 0;

                        int cd=0;
                        ObjectStats[] allObjects= new ObjectStats[numItems];
                        //[ID]-
                        //[0]-ItemID
                        //[1]-Researchable
                        //[2]-req_amt
                        //[3]-give_amt
                        //[4]-paid_amt
                        //[5]-unlocked
                        //[6]-Grouping: 1-unlocked, 2-potential, 3-progress, 4-other
                        //[7]-Category (string from MC index)

                        for (String name : playerUnlockMap.keySet()) {
                            Object[] itemData = playerUnlockMap.get(name);
                            //if the data has been transmitted with an injected 'not researchable' for any reason, abort
                            if ((Integer) itemData[1] > 0) {
                                //0-ID, 1-Unlockable, 2-Req amt, 3-Give amt, 4-Player Unlocked, 5-Paid Amt, 6-Research Percentage
                                //TODO: Add an int tracker that reports the % done of an objective, and add to the progress if its in progress
                                //ScoreboardObjective thisObjective = player.getScoreboard().getObjective((String) itemConfig[4]);
                                //Copy the item data to master list
                               // System.out.println("Data: "+itemData[0]+" | "+itemData[1]+" | "+itemData[2]+" | "+itemData[3]+" | "+itemData[4]+" | "+itemData[5]+" | ");
                                allObjects[cd]=new ObjectStats();

                                ItemGroup grp = (Registry.ITEM.get((int) itemData[0]).asItem().getGroup());
                                String categoryName= "Misc";
                                if (grp!=null){
                                    categoryName = grp.getName();
                                }
                                int catVal=0;
                                //if the item is unlocked, add to unlocked array
                                if ((int) itemData[4] == 1) {
                                    catVal=1;
                                    ud++;
                                    ttd++;
                                } else if ((player.getInventory().count(Registry.ITEM.get((int) itemData[0]).asItem()) > 0) && (((int) itemData[1] == 1) || ((int) itemData[1] == 3) || ((int) itemData[1] == 4))) {
                                    //if the player has the item in their inventory, and the item is required in the research status
                                    catVal=2;
                                    pd++;
                                    ttd++;
                                } else if (((int) itemData[5] > 0) || ((int) itemData[6] > 0)) {
                                    //if player has started the process of unlocking add to list
                                    catVal=3;
                                    prd++;
                                    ttd++;
                                } else {
                                    catVal=4;
                                    ad++;
                                    ttd++;
                                }
                                allObjects[cd].setValues((int)itemData[0],(int)itemData[1],(int)itemData[2],(int)itemData[3],(int)itemData[5],(int)itemData[4],catVal,categoryName);
                            cd++;
                            }
                        }
                        int _pd = 0;
                        int _prd = 0;
                        int _ud = 0;
                        int _ad = 0;
                        int numToGen = ttd;

                        //Take the number of loaded for each type, and set the size of the main container
                        int uPanelRows = 1;
                        int pPanelRows = 1;
                        int prPanelRows = 1;
                        int aPanelRows = 1;
                        //Unlocked Panel
                        if (ud > 0) {
                            uPanelRows = Math.max((int) Math.ceil(ud / (panelMaxW - 1)), 1);
                            panelUnlocked.setSize(panelMaxW - 1, uPanelRows);

                        } else {
                            //TODO: Print details about nothing being unlocked
                        }
                        //Potential Panel
                        if (pd > 0) {
                            pPanelRows = Math.max((int) Math.ceil(pd / (panelMaxW - 1)), 1);
                            panelPotential.setSize(panelMaxW - 1, pPanelRows);

                        } else {
                            //TODO: Print details about nothing being in inventory
                        }
                        //Progress Panel
                        if (prd > 0) {
                            prPanelRows = Math.max((int) Math.ceil(prd / (panelMaxW - 1)), 1);
                            panelProgress.setSize(panelMaxW - 1, prPanelRows);

                        } else {
                            //TODO: Print details about nothing being in inventory
                        }
                        if (ad > 0) {
                            aPanelRows = Math.max((int) Math.ceil(ad / (panelMaxW - 1)), 1);
                            panelAll.setSize(panelMaxW - 1, aPanelRows);
                        } else {

                        }

                        int panelVOffset = 1;
                        Insets itemInset = new Insets(2, 2);
                        int uDisplayed = 0;
                        int pDisplayed = 0;
                        int prDisplayed = 0;
                        int aDisplayed = 0;
                        Arrays.sort(allObjects,Comparator.comparing(ObjectStats::getItemID).thenComparing(ObjectStats::getGroup));
                        for (int ccd = 0; ccd < numToGen; ccd++) {
                            Object[] itemData = new Object[6];
                            int itemType = 0;//1-unlocked, 2-potential, 3-progress, 4-random

                            ObjectStats thisObject = allObjects[ccd];
                            itemType=thisObject.getCategory();
                            boolean showItem = true;
                            String name = "" + Registry.ITEM.get(thisObject.getItemID()).asItem().getName();

                            if (thisObject.getItemID() == 0) {
                                showItem = false;
                            }
                            if (thisObject.getResearchable() == 0) {
                                showItem = false;
                            }

                            String itemName = String.valueOf(Registry.ITEM.get(thisObject.getItemID()).asItem());
                            itemName = itemName.replaceAll("_", " ").toLowerCase();
                            itemName = WordUtils.capitalizeFully(itemName);

                            if ((!filter.equals(""))) {
                                //if the filter is not empty
                                //if the name of the object contains the filter
                                showItem = itemName.contains(filter);
                            }

                            if (showItem) {
                                generatedItems++;
                                int itemCount = player.getInventory().count(Registry.ITEM.get(thisObject.getItemID()).asItem());
                                jmItemSlot[it] = new WGridPanel();
                                jmItemSlot[it].setBackgroundPainter(contents);
                                jmItemSlot[it].setInsets(itemInset);

                                int row = 0;
                                int col = 0;

                                String finalItemName = itemName;
                                //Object[] finalItemData = itemData;
                                //String finalFilterTxt = FilterTxt;
                                switch (itemType) {
                                    case 1:
                                        //Unlocked panel
                                        panelUnlocked.remove(lblUnlocked);
                                        jmItem[it] = new WJMItem(Registry.ITEM.get(thisObject.getItemID()).asItem().getDefaultStack()) {
                                            @Environment(EnvType.CLIENT)
                                            @Override
                                            public void addTooltip(TooltipBuilder tooltip) {
                                                tooltip.add(Text.of(finalItemName));
                                                tooltip.add(Text.of(new TranslatableText("mjm.gui.tooltip.lcgive").getString()+" | "+new TranslatableText("mjm.gui.tooltip.lcsgive").getString()+" "+thisObject.getGive_Amt()));
                                                tooltip.add(new TranslatableText("mjm.gui.tooltip.mcclear"));
                                            }
                                        };
                                        jmItem[it].setOnClick(() -> {
                                            if (Screen.hasShiftDown()) {
                                                int itemID = thisObject.getItemID();
                                                PacketByteBuf data = PacketByteBufs.create();
                                                data.writeInt(itemID);
                                                ClientPlayNetworking.send(Mcjourneymode.give_packet, data);
                                            } else {
                                                int itemID = thisObject.getItemID();
                                                PacketByteBuf data = PacketByteBufs.create();
                                                data.writeInt(itemID);
                                                ClientPlayNetworking.send(Mcjourneymode.give_packet_single, data);
                                            }
                                        });
                                        jmItem[it].setOnRightClick(() -> {
                                            if (perms) {
                                                PacketByteBuf clickData = PacketByteBufs.create();
                                                clickData.writeString(String.valueOf(Registry.ITEM.get(thisObject.getItemID()).asItem()));
                                                ClientPlayNetworking.send(Mcjourneymode.send_single_config_req_packet, clickData);
                                            }
                                        });
                                        jmItem[it].setOnMiddleClick(()->{
                                            int itemID = thisObject.getItemID();
                                            PacketByteBuf data = PacketByteBufs.create();
                                            data.writeInt(itemID);
                                            ClientPlayNetworking.send(Mcjourneymode.clear_packet, data);
                                        });

                                        if (itemCount>0) {
                                            alertIcon[it]=new WJMItemAlert(Mcjourneymode.InInventoryAlertItem.getDefaultStack());
                                            jmItemSlot[it].add(alertIcon[it],0,0);}
                                        jmItemSlot[it].add(jmItem[it], 0, 0);

                                        row = (int) Math.floor(uDisplayed / (panelMaxW));
                                        col = uDisplayed - ((panelMaxW) * row);
                                        panelUnlocked.add(jmItemSlot[it], col, row);
                                        uDisplayed++;
                                        break;
                                    case 2:
                                        //Potential panel
                                        panelPotential.remove(lblPotential);
                                        jmItem[it] = new WJMItem(Registry.ITEM.get(thisObject.getItemID()).asItem().getDefaultStack()) {
                                            @Environment(EnvType.CLIENT)
                                            @Override
                                            public void addTooltip(TooltipBuilder tooltip) {
                                                tooltip.add(Text.of(finalItemName));
                                                tooltip.add(Text.of(thisObject.getPaid_Amt() + "/" + thisObject.getReq_Amt()));
                                            }
                                        };

                                        jmItem[it].setOnClick(() -> {
                                            mc.execute(() -> {
                                                ScreenList daScreen = new ScreenList(new ScreenSingleGui(Config.playerConfigMap, "",perms,String.valueOf(Registry.ITEM.get(thisObject.getItemID()).asItem()),passThis));
                                                MinecraftClient.getInstance().setScreen(daScreen);
                                            });

                                        });
                                        jmItem[it].setOnRightClick(() -> {
                                            if (perms) {
                                                PacketByteBuf clickData = PacketByteBufs.create();
                                                clickData.writeString(String.valueOf(Registry.ITEM.get(thisObject.getItemID()).asItem()));
                                                ClientPlayNetworking.send(Mcjourneymode.send_single_config_req_packet, clickData);
                                            }
                                        });
                                        jmItem[it].setOnMiddleClick(()->{});
                                        jmItemSlot[it].add(jmItem[it], 0, 0);
                                        row = (int) Math.floor(pDisplayed / (panelMaxW));
                                        col = pDisplayed - ((panelMaxW) * row);
                                        panelPotential.add(jmItemSlot[it], col, row);
                                        pDisplayed++;
                                        break;
                                    case 3:
                                        //Progress panel
                                        panelProgress.remove(lblProgress);
                                        jmItem[it] = new WJMItem(Registry.ITEM.get(thisObject.getItemID()).asItem().getDefaultStack()) {
                                            @Environment(EnvType.CLIENT)
                                            @Override
                                            public void addTooltip(TooltipBuilder tooltip) {
                                                tooltip.add(Text.of(finalItemName));
                                                tooltip.add(Text.of(thisObject.getPaid_Amt() + "/" + thisObject.getReq_Amt()));
                                            }
                                        };
                                        jmItem[it].setOnClick(() -> {
                                            mc.execute(() -> {
                                                ScreenList daScreen = new ScreenList(new ScreenSingleGui(Config.playerConfigMap, "",perms,String.valueOf(Registry.ITEM.get(thisObject.getItemID()).asItem()),passThis));
                                                MinecraftClient.getInstance().setScreen(daScreen);
                                            });

                                        });
                                        jmItem[it].setOnRightClick(() -> {
                                            if (perms) {
                                                PacketByteBuf clickData = PacketByteBufs.create();
                                                clickData.writeString(String.valueOf(Registry.ITEM.get(thisObject.getItemID()).asItem()));
                                                ClientPlayNetworking.send(Mcjourneymode.send_single_config_req_packet, clickData);
                                            }
                                        });
                                        jmItem[it].setOnMiddleClick(()->{});
                                        jmItemSlot[it].add(jmItem[it], 0, 0);
                                        row = (int) Math.floor(prDisplayed / (panelMaxW));
                                        col = prDisplayed - ((panelMaxW) * row);
                                        ;
                                        panelProgress.add(jmItemSlot[it], col, row);
                                        prDisplayed++;
                                        break;
                                    case 4:
                                        //All panel
                                        panelAll.remove(lblAll);
                                        jmItem[it] = new WJMItem(Registry.ITEM.get(thisObject.getItemID()).asItem().getDefaultStack()) {
                                            @Environment(EnvType.CLIENT)
                                            @Override
                                            public void addTooltip(TooltipBuilder tooltip) {
                                                tooltip.add(Text.of(finalItemName));
                                                tooltip.add(Text.of(thisObject.getPaid_Amt() + "/" + thisObject.getReq_Amt()));
                                            }
                                        };
                                        jmItem[it].setOnClick(() -> {
                                            mc.execute(() -> {
                                                ScreenList daScreen = new ScreenList(new ScreenSingleGui(Config.playerConfigMap, "",perms,String.valueOf(Registry.ITEM.get(thisObject.getItemID()).asItem()),passThis));
                                                MinecraftClient.getInstance().setScreen(daScreen);
                                            });

                                        });
                                        jmItem[it].setOnRightClick(() -> {
                                            if (perms) {
                                                PacketByteBuf clickData = PacketByteBufs.create();
                                                clickData.writeString(String.valueOf(Registry.ITEM.get(thisObject.getItemID()).asItem()));
                                                ClientPlayNetworking.send(Mcjourneymode.send_single_config_req_packet, clickData);
                                            }
                                        });
                                        jmItem[it].setOnMiddleClick(()->{});
                                        jmItemSlot[it].add(jmItem[it], 0, 0);
                                        row = (int) Math.floor(aDisplayed / (panelMaxW));
                                        col = aDisplayed - ((panelMaxW) * row);
                                        ;
                                        panelAll.add(jmItemSlot[it], col, row);
                                        aDisplayed++;
                                        break;
                                }
                            }


                        }
                        int finalPanelMaxW = panelMaxW;

                        //panelUnlocked.setBackgroundPainter(contents);
                        WLabel uLbl = new WLabel(new TranslatableText("mjm.gui.lbl.title.ul"));
                        uLbl.setVerticalAlignment(VerticalAlignment.BOTTOM);
                        int uRowstoShow = Math.max((int) Math.ceil(uDisplayed / (panelMaxW - 1)), 1);
                        myTallPanel.add(uLbl, 1, panelVOffset - 1);
                        myTallPanel.add(panelUnlocked, 1, panelVOffset, finalPanelMaxW, uRowstoShow);
                        int uSize = Math.max(ud / finalPanelMaxW + ((ud % finalPanelMaxW == 0) ? 0 : 1), 1);

                        //panelPotential.setBackgroundPainter(contents);
                        WLabel pLbl = new WLabel(new TranslatableText("mjm.gui.lbl.title.ii"));
                        pLbl.setVerticalAlignment(VerticalAlignment.BOTTOM);
                        int pRowstoShow = Math.max((int) Math.ceil(pDisplayed / (panelMaxW - 1)), 1);
                        myTallPanel.add(pLbl, 1, panelVOffset + uSize);
                        myTallPanel.add(panelPotential, 1, panelVOffset + uSize + 1, finalPanelMaxW, pRowstoShow);
                        int pSize = Math.max(pd / finalPanelMaxW + ((pd % finalPanelMaxW == 0) ? 0 : 1), 1);


                        //panelProgress.setBackgroundPainter(contents);
                        WLabel prLbl = new WLabel(new TranslatableText("mjm.gui.lbl.title.ip"));
                        prLbl.setVerticalAlignment(VerticalAlignment.BOTTOM);
                        int prRowstoShow = Math.max((int) Math.ceil(prDisplayed / (panelMaxW - 1)), 1);
                        myTallPanel.add(prLbl, 1, panelVOffset + uSize + pSize + 1);
                        myTallPanel.add(panelProgress, 1, panelVOffset + uSize + pSize + 2, finalPanelMaxW, prRowstoShow);
                        int prSize = Math.max(prd / finalPanelMaxW + ((prd % finalPanelMaxW == 0) ? 0 : 1), 1);

                        //panelAll.setBackgroundPainter(contents);
                        WLabel aLbl = new WLabel(new TranslatableText("mjm.gui.lbl.title.o"));
                        aLbl.setVerticalAlignment(VerticalAlignment.BOTTOM);
                        int aRowstoShow = Math.max((int) Math.ceil(aDisplayed / (panelMaxW - 1)), 1);
                        myTallPanel.add(aLbl, 1, panelVOffset + uSize + pSize + prSize + 2);
                        myTallPanel.add(panelAll, 1, panelVOffset + uSize + pSize + prSize + 3, finalPanelMaxW, aRowstoShow);
                        if (generatedItems == 0) {
                            //this means nothing was displayed due to filter settings
                            WLabel lblEmpty = new WLabel("-" + new TranslatableText("mjm.gui.lbl.search").getString() + "-");
                            lblEmpty.setColor(textColor_RED.toRgb());
                            myTallPanel.add(lblEmpty, 0, 0);
                        }
                    } else {
                        //this means nothing was loaded. means admin disabled all items, or glitch with config.
                        WLabel lblEmpty = new WLabel(new TranslatableText("mjm.gui.lbl.noneAvail"));
                        lblEmpty.setColor(textColor_RED.toRgb());
                        myTallPanel.add(lblEmpty, 1, 1);
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
            WTextField searchBar = new WTextField(new TranslatableText("mjm.gui.lbl.title.search"));
            searchBar.setText(searchDefault);
            int windowWidth = mc.getWindow().getWidth();
            int windowHeight = mc.getWindow().getHeight();
            double wfloor = Math.floor((windowWidth*0.60) / scale);
            double hfloor = Math.floor((windowHeight*0.75) / scale);
            int rootWidth = (int) wfloor;
            int rootHeight = (int) hfloor;
            root.setSize(rootWidth, rootHeight);
            root.setInsets(Insets.ROOT_PANEL);
            int searchScale = (root.getWidth() / 18) - 10;

            Consumer<String> searchConsumer = new Consumer<String>() {
                @Override
                public void accept(String s) {
                    String filterText = searchBar.getText();
                    contentGenerator.generatePanels(data, filterText, wrapContents, root, thisDescription,perms);
                }
            };
            searchBar.setChangedListener(searchConsumer);
            root.add(searchBar, 1, 1, searchScale, 1);

            if (perms) {
                WButton configButton = new WButton(new LiteralText("⚙"));
                configButton.setOnClick(() -> {
                    PacketByteBuf clickData = PacketByteBufs.create();
                    ClientPlayNetworking.send(Mcjourneymode.send_config_req_packet, clickData);
                });
                root.add(configButton, (root.getWidth() / 18) - 2, 0, 1, 1);
            }
        contentGenerator.generatePanels(data, searchDefault, wrapContents, root, thisDescription,perms);
        setRootPanel(root);
        root.validate(this);
    }
    }
}


