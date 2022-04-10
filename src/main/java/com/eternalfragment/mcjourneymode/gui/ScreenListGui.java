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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.text.WordUtils;

import java.util.HashMap;
import java.util.function.Consumer;
@Environment(EnvType.CLIENT)

public class ScreenListGui extends LightweightGuiDescription{
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
                        WGridPanel[] jmItemSlot = new WGridPanel[numItems];
                        WGridPanel panelUnlocked = new WGridPanel(jmItemSize);
                        WGridPanel panelProgress = new WGridPanel(jmItemSize);
                        WGridPanel panelPotential = new WGridPanel(jmItemSize);
                        WGridPanel panelAll = new WGridPanel(jmItemSize);
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
                        for (String name : playerUnlockMap.keySet()) {
                            Object[] itemData = playerUnlockMap.get(name);
                            //if the data has been transmitted with an injected 'not researchable' for any reason, abort
                            if ((Integer) itemData[1] > 0) {
                                //0-ID, 1-Unlockable, 2-Req amt, 3-Give amt, 4-Player Unlocked, 5-Paid Amt, 6-Research Percentage
                                //TODO: Add an int tracker that reports the % done of an objective, and add to the progress if its in progress
                                //ScoreboardObjective thisObjective = player.getScoreboard().getObjective((String) itemConfig[4]);
                                //if the item is unlocked, add to unlocked array
                                if ((int) itemData[4] == 1) {
                                    unlockedData[ud] = itemData;
                                    ud++;
                                    ttd++;
                                } else if ((player.getInventory().count(Registry.ITEM.get((int) itemData[0]).asItem()) > 0) && (((int) itemData[1] == 1) || ((int) itemData[1] == 3) || ((int) itemData[1] == 4))) {
                                    //if the player has the item in their inventory, and the item is required in the research status
                                    potentialData[pd] = itemData;
                                    pd++;
                                    ttd++;
                                } else if (((int) itemData[5] > 0) || ((int) itemData[6] > 0)) {
                                    //if player has started the process of unlocking add to list
                                    progressData[prd] = itemData;
                                    prd++;
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
                        int numToGen = ttd;
                       /* if ((!filter.equals(""))) {
                            //if the filter is 'on' load through all the entires
                            numToGen = ttd;
                        } else {
                            numToGen = pd + prd + ud + ad;
                        }*/

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


                        WLabel testLbl = new WLabel("Max Row: " + panelMaxW + "; U Rows: " + uPanelRows);


                        int panelVOffset = 1;
                        Insets itemInset = new Insets(2, 2);


                        int uDisplayed = 0;
                        int pDisplayed = 0;
                        int prDisplayed = 0;
                        int aDisplayed = 0;
                        for (int ccd = 0; ccd < numToGen; ccd++) {
                            Object[] itemData = new Object[6];
                            int itemType = 0;//1-unlocked, 2-potential, 3-progress, 4-random
                            boolean ranItem = true;
                            if (_ud < ud) {
                                //if the number of generated unlockables is less than the number of available
                                ranItem = false;
                                itemData = unlockedData[_ud];
                                itemType = 1;
                                _ud++;
                            } else if (_pd < pd) {
                                ranItem = false;
                                itemData = potentialData[_pd];
                                itemType = 2;
                                _pd++;
                            } else if (_prd < prd) {
                                ranItem = false;
                                itemData = progressData[_prd];
                                itemType = 3;
                                _prd++;
                            }
                            if ((ccd >= (ud + prd + pd)) || ((ud + prd + pd) == 0)) {
                                //if the counter is beyond the 3 sorts, or the 3 sorts are empty, set the data to all the data (used for text-search)
                                ranItem = false;
                                itemData = ALLData[_ad];
                                itemType = 4;
                                _ad++;
                            }
                            boolean showItem = false;
                            String name = "" + Registry.ITEM.get((int) itemData[0]).asItem().getName();

                            if (!ranItem) {
                                //if its not a random item, show it by default
                                showItem = true;
                            }
                            if ((int) itemData[0] == 0) {
                                showItem = false;
                            }
                            if ((int) itemData[1] == 0) {
                                showItem = false;
                            }

                            String itemName = String.valueOf(Registry.ITEM.get((int) itemData[0]).asItem());
                            itemName = itemName.replaceAll("_", " ").toLowerCase();
                            itemName = WordUtils.capitalizeFully(itemName);

                            if ((!filter.equals(""))) {
                                //if the filter is not empty
                                //if the name of the object contains the filter
                                showItem = itemName.contains(filter);
                            }

                            if (showItem) {
                                generatedItems++;
                                int itemCount = player.getInventory().count(Registry.ITEM.get((int) itemData[0]).asItem());
                                jmItemSlot[it] = new WGridPanel();
                                jmItemSlot[it].setBackgroundPainter(contents);
                                jmItemSlot[it].setInsets(itemInset);

                                int row = 0;
                                int col = 0;

                                String finalItemName = itemName;
                                Object[] finalItemData = itemData;
                                String finalFilterTxt = FilterTxt;
                                switch (itemType) {
                                    case 1:
                                        //Unlocked panel

                                        jmItem[it] = new WJMItem(Registry.ITEM.get((int) finalItemData[0]).asItem().getDefaultStack()) {
                                            @Environment(EnvType.CLIENT)
                                            @Override
                                            public void addTooltip(TooltipBuilder tooltip) {
                                                tooltip.add(Text.of(finalItemName));
                                                tooltip.add(Text.of("Click to give | Shift+Click for Stack"));
                                            }
                                        };
                                        jmItem[it].setOnClick(() -> {
                                            if (Screen.hasShiftDown()) {
                                                int itemID = Integer.parseInt(finalItemData[0].toString());
                                                PacketByteBuf data = PacketByteBufs.create();
                                                data.writeInt(itemID);
                                                ClientPlayNetworking.send(Mcjourneymode.give_packet, data);
                                            } else {
                                                int itemID = Integer.parseInt(finalItemData[0].toString());
                                                PacketByteBuf data = PacketByteBufs.create();
                                                data.writeInt(itemID);
                                                ClientPlayNetworking.send(Mcjourneymode.give_packet_single, data);
                                            }
                                        });
                                        jmItem[it].setOnRightClick(() -> {
                                            if (perms) {
                                                PacketByteBuf clickData = PacketByteBufs.create();
                                                clickData.writeString(String.valueOf(Registry.ITEM.get((int) finalItemData[0]).asItem()));
                                                ClientPlayNetworking.send(Mcjourneymode.send_single_config_req_packet, clickData);
                                            }
                                        });
                                        jmItem[it].setOnMiddleClick(()->{
                                            int itemID = Integer.parseInt(finalItemData[0].toString());
                                            PacketByteBuf data = PacketByteBufs.create();
                                            data.writeInt(itemID);
                                            ClientPlayNetworking.send(Mcjourneymode.clear_packet, data);
                                        });
                                        jmItemSlot[it].add(jmItem[it], 0, 0);
                                        row = (int) Math.floor(uDisplayed / (panelMaxW));
                                        col = uDisplayed - ((panelMaxW) * row);
                                        panelUnlocked.add(jmItemSlot[it], col, row);

                                        uDisplayed++;
                                        break;
                                    case 2:
                                        //Potential panel
                                        jmItem[it] = new WJMItem(Registry.ITEM.get((int) finalItemData[0]).asItem().getDefaultStack()) {
                                            @Environment(EnvType.CLIENT)
                                            @Override
                                            public void addTooltip(TooltipBuilder tooltip) {
                                                tooltip.add(Text.of(finalItemName));
                                                tooltip.add(Text.of(finalItemData[5] + "/" + finalItemData[2]));
                                            }
                                        };

                                        jmItem[it].setOnClick(() -> {
                                            mc.execute(() -> {
                                                ScreenList daScreen = new ScreenList(new ScreenSingleGui(Config.playerConfigMap, "",false,String.valueOf(Registry.ITEM.get((int) finalItemData[0]).asItem()),passThis));
                                                MinecraftClient.getInstance().setScreen(daScreen);
                                            });

                                        });
                                        jmItem[it].setOnRightClick(() -> {
                                            if (perms) {
                                                PacketByteBuf clickData = PacketByteBufs.create();
                                                clickData.writeString(String.valueOf(Registry.ITEM.get((int) finalItemData[0]).asItem()));
                                                ClientPlayNetworking.send(Mcjourneymode.send_single_config_req_packet, clickData);
                                            }
                                        });
                                        jmItemSlot[it].add(jmItem[it], 0, 0);
                                        row = (int) Math.floor(pDisplayed / (panelMaxW));
                                        col = pDisplayed - ((panelMaxW) * row);
                                        panelPotential.add(jmItemSlot[it], col, row);
                                        pDisplayed++;
                                        break;
                                    case 3:
                                        //Progress panel
                                        jmItem[it] = new WJMItem(Registry.ITEM.get((int) finalItemData[0]).asItem().getDefaultStack()) {
                                            @Environment(EnvType.CLIENT)
                                            @Override
                                            public void addTooltip(TooltipBuilder tooltip) {
                                                tooltip.add(Text.of(finalItemName));
                                                tooltip.add(Text.of(finalItemData[5] + "/" + finalItemData[2]));
                                            }
                                        };
                                        jmItem[it].setOnClick(() -> {
                                            mc.execute(() -> {
                                                ScreenList daScreen = new ScreenList(new ScreenSingleGui(Config.playerConfigMap, "",false,String.valueOf(Registry.ITEM.get((int) finalItemData[0]).asItem()),passThis));
                                                MinecraftClient.getInstance().setScreen(daScreen);
                                            });

                                        });
                                        jmItem[it].setOnRightClick(() -> {
                                            if (perms) {
                                                PacketByteBuf clickData = PacketByteBufs.create();
                                                clickData.writeString(String.valueOf(Registry.ITEM.get((int) finalItemData[0]).asItem()));
                                                ClientPlayNetworking.send(Mcjourneymode.send_single_config_req_packet, clickData);
                                            }
                                        });
                                        jmItemSlot[it].add(jmItem[it], 0, 0);
                                        row = (int) Math.floor(prDisplayed / (panelMaxW));
                                        col = prDisplayed - ((panelMaxW) * row);
                                        ;
                                        panelProgress.add(jmItemSlot[it], col, row);
                                        prDisplayed++;
                                        break;
                                    case 4:
                                        //All panel
                                        jmItem[it] = new WJMItem(Registry.ITEM.get((int) finalItemData[0]).asItem().getDefaultStack()) {
                                            @Environment(EnvType.CLIENT)
                                            @Override
                                            public void addTooltip(TooltipBuilder tooltip) {
                                                tooltip.add(Text.of(finalItemName));
                                                tooltip.add(Text.of(finalItemData[5] + "/" + finalItemData[2]));
                                            }
                                        };
                                        jmItem[it].setOnClick(() -> {
                                            mc.execute(() -> {
                                                ScreenList daScreen = new ScreenList(new ScreenSingleGui(Config.playerConfigMap, "",false,String.valueOf(Registry.ITEM.get((int) finalItemData[0]).asItem()),passThis));
                                                MinecraftClient.getInstance().setScreen(daScreen);
                                            });

                                        });
                                        jmItem[it].setOnRightClick(() -> {
                                            if (perms) {
                                                PacketByteBuf clickData = PacketByteBufs.create();
                                                clickData.writeString(String.valueOf(Registry.ITEM.get((int) finalItemData[0]).asItem()));
                                                ClientPlayNetworking.send(Mcjourneymode.send_single_config_req_packet, clickData);
                                            }
                                        });
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

                        panelUnlocked.setBackgroundPainter(contents);
                        WLabel uLbl = new WLabel("Unlocked Items");
                        uLbl.setVerticalAlignment(VerticalAlignment.BOTTOM);
                        int uRowstoShow = Math.max((int) Math.ceil(uDisplayed / (panelMaxW - 1)), 1);
                        myTallPanel.add(uLbl, 1, panelVOffset - 1);
                        myTallPanel.add(panelUnlocked, 1, panelVOffset, finalPanelMaxW, uRowstoShow);
                        int uSize = Math.max(ud / finalPanelMaxW + ((ud % finalPanelMaxW == 0) ? 0 : 1), 1);

                        panelPotential.setBackgroundPainter(contents);
                        WLabel pLbl = new WLabel("In Inventory");
                        pLbl.setVerticalAlignment(VerticalAlignment.BOTTOM);
                        int pRowstoShow = Math.max((int) Math.ceil(pDisplayed / (panelMaxW - 1)), 1);
                        myTallPanel.add(pLbl, 1, panelVOffset + uSize);
                        myTallPanel.add(panelPotential, 1, panelVOffset + uSize + 1, finalPanelMaxW, pRowstoShow);
                        int pSize = Math.max(pd / finalPanelMaxW + ((pd % finalPanelMaxW == 0) ? 0 : 1), 1);


                        panelProgress.setBackgroundPainter(contents);
                        WLabel prLbl = new WLabel("In Progress");
                        prLbl.setVerticalAlignment(VerticalAlignment.BOTTOM);
                        int prRowstoShow = Math.max((int) Math.ceil(prDisplayed / (panelMaxW - 1)), 1);
                        myTallPanel.add(prLbl, 1, panelVOffset + uSize + pSize + 1);
                        myTallPanel.add(panelProgress, 1, panelVOffset + uSize + pSize + 2, finalPanelMaxW, prRowstoShow);
                        int prSize = Math.max(prd / finalPanelMaxW + ((prd % finalPanelMaxW == 0) ? 0 : 1), 1);

                        panelAll.setBackgroundPainter(contents);
                        WLabel aLbl = new WLabel("Other");
                        aLbl.setVerticalAlignment(VerticalAlignment.BOTTOM);
                        int aRowstoShow = Math.max((int) Math.ceil(aDisplayed / (panelMaxW - 1)), 1);
                        myTallPanel.add(aLbl, 1, panelVOffset + uSize + pSize + prSize + 2);
                        myTallPanel.add(panelAll, 1, panelVOffset + uSize + pSize + prSize + 3, finalPanelMaxW, aRowstoShow);
                        if (generatedItems == 0) {
                            //this means nothing was displayed due to filter settings
                            WLabel lblEmpty = new WLabel("-" + new TranslatableText("mjm.gui.lbl.search").getString() + "-");
                            lblEmpty.setColor(textColor_RED.toRgb());
                            myTallPanel.add(lblEmpty, 1, 1);
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
            WTextField searchBar = new WTextField(Text.of("Search"));
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
