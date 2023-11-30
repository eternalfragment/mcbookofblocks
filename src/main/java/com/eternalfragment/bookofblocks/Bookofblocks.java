package com.eternalfragment.bookofblocks;


import com.eternalfragment.bookofblocks.commands.Bob_cmd_give;
import com.eternalfragment.bookofblocks.config.Config;
import com.eternalfragment.bookofblocks.config.ConfigScreen;
import com.eternalfragment.bookofblocks.config.SingleConfigScreen;
import com.eternalfragment.bookofblocks.gui.DoSetScreen;
import com.eternalfragment.bookofblocks.items.BobInInventoryAlertItem;
import com.eternalfragment.bookofblocks.items.GuiItem;
import com.eternalfragment.bookofblocks.operators.invManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class Bookofblocks implements ModInitializer {
    public static org.apache.logging.log4j.Logger mylogger = LogManager.getLogger();
    public static Item GUI_ITEM = new GuiItem(new Item.Settings());
    public static Item InInventoryAlertItem = new BobInInventoryAlertItem(new Item.Settings().maxCount(0));
    public static String worldPath = "world"; //default setting is 'world' if system is unable to detect world from settings, it will default to this
    public static final String MOD_ID = "bob";
    public static final String modDir = "bookofblocks\\";
    public static final String modPlayerDir = "bookofblocks\\players\\";
    public static Identifier pay_packet=null;
    public static Identifier give_packet=null;
    public static Identifier give_packet_single=null;
    public static Identifier clear_packet=null;
    public static Identifier sp_dir_packet=null;
    public static Identifier menu_populate=null;
    public static Identifier menu_populate_perms=null;
    public static Identifier get_config_packet=null;
    public static Identifier send_config_req_packet=null;
    public static Identifier send_single_config_req_packet=null;
    public static Identifier send_config_packet=null;
    public static Identifier send_single_config_packet=null;
    public static Identifier get_single_config_packet=null;
    public static EnvType type;//holds whether mod is loaded in client or server
    public static int permLevel=4;

    @Override
    public void onInitialize() {

        mylogger.atInfo().log("Mod Booting....");
        Registry.register(Registries.ITEM, new Identifier(Bookofblocks.MOD_ID, "menu_item"), GUI_ITEM);
        Registry.register(Registries.ITEM, new Identifier(Bookofblocks.MOD_ID, "in_inv_alert"), InInventoryAlertItem);
        type = FabricLoader.getInstance().getEnvironmentType();
        give_packet =  give_packet.tryParse("bob:process_give");
        give_packet_single=give_packet_single.tryParse("bob:process_give_single");
        pay_packet = pay_packet.tryParse("bob:process_pay");
        clear_packet = clear_packet.tryParse("bob:process_clear");
        sp_dir_packet = sp_dir_packet.tryParse("bob:sp_directory_find");
        menu_populate = menu_populate.tryParse("bob:menu_populate");
        menu_populate_perms = menu_populate_perms.tryParse("bob:menu_populate_perms");
        get_config_packet = get_config_packet.tryParse("bob:get_config_packet");
        send_config_req_packet = send_config_req_packet.tryParse("bob:send_config_req_packet");
        send_single_config_req_packet=send_single_config_req_packet.tryParse("bob:send_single_config_req_packet");
        send_config_packet = send_config_packet.tryParse("bob:send_config_packet");
        send_single_config_packet=send_single_config_packet.tryParse("bob:send_single_config_packet");
        get_single_config_packet=get_single_config_packet.tryParse("bob:get_single_config_packet");
        AtomicBoolean payPacketStatus = new AtomicBoolean(false);

        if (Objects.equals(type.toString(), "CLIENT")){
            CommandRegistrationCallback.EVENT.register((dispatcher,registryAccess, dedicated) -> new Bob_cmd_give(dispatcher));

            ClientPlayNetworking.registerGlobalReceiver(sp_dir_packet, (client, handler, buf, pktSnd) -> {
                worldPath =  Objects.requireNonNull(client.getServer()).getSavePath(WorldSavePath.ROOT).toString().replaceAll("\\.+$","");
                try {
                    Config.main();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            ClientPlayNetworking.registerGlobalReceiver(send_config_packet, (client, handler, buf, pktSnd) -> {
                PacketByteBuf.PacketReader<String> keyConsumer = PacketByteBuf::readString;
                PacketByteBuf.PacketReader<String> valConsumer = PacketByteBuf::readString;
                HashMap<String, String> getMap = new HashMap<>(buf.readMap(keyConsumer, valConsumer));

                Config.configMap= Config.configStoO(getMap);
                ConfigScreen.callBuildScreen(client,handler,buf,pktSnd);

            });
            ClientPlayNetworking.registerGlobalReceiver(send_single_config_packet, (client, handler, buf, pktSnd) -> {
                PacketByteBuf.PacketReader<String> keyConsumer = PacketByteBuf::readString;
                PacketByteBuf.PacketReader<String> valConsumer = PacketByteBuf::readString;
                HashMap<String, String> getMap = new HashMap<>(buf.readMap(keyConsumer, valConsumer));
                HashMap<String, Object[]> single = Config.configStoO(getMap);
                String openName="";
                for (Map.Entry<String, Object[]> entry : single.entrySet()) {
                    String nameKey = entry.getKey();
                    Object[] data = entry.getValue();
                    openName=nameKey;
                    Config.configMap.put(nameKey,data);//take data sent, and update local config map
                }
                SingleConfigScreen.callBuildScreen(client,handler,buf,pktSnd,openName);
            });

            ServerPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
                //Register the client to detect when the player connects to the local server. When that happens, send a packet. If received by client, generate/load singleplayer config.

                PacketByteBuf data = PacketByteBufs.create();
                ServerPlayNetworking.send(handler.getPlayer(), Bookofblocks.sp_dir_packet, data);
            });
            ClientPlayNetworking.registerGlobalReceiver(menu_populate, DoSetScreen::doSetScreen);
            ClientPlayNetworking.registerGlobalReceiver(menu_populate_perms, DoSetScreen::doSetScreenPerms);
        }
        if (Objects.equals(type.toString(), "SERVER")){
            CommandRegistrationCallback.EVENT.register((dispatcher,registryAccess, dedicated) -> new Bob_cmd_give(dispatcher));
            ServerLifecycleEvents.SERVER_STARTED.register((handler)->{
                worldPath =  handler.getSavePath(WorldSavePath.ROOT).toString().replaceAll("\\.+$","");
                try {
                    Config.main();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        //TODO: create packet receiver for individual config. Packet should sent string that contains item name
        ServerPlayNetworking.registerGlobalReceiver(send_config_req_packet,  (server, player, handler, buf, pktSnd) -> {
            //Player has sent a request to get the config. should respond with converted data
            HashMap<String, String> transmitData = Config.configOtoS(Config.configMap);
            PacketByteBuf data = PacketByteBufs.create();
                data.writeMap(transmitData, PacketByteBuf::writeString, PacketByteBuf::writeString);
                if (player.hasPermissionLevel(permLevel)){ServerPlayNetworking.send(player, send_config_packet, data);}
                });
        ServerPlayNetworking.registerGlobalReceiver(send_single_config_req_packet,  (server, player, handler, buf, pktSnd) -> {
            //Player has sent a request to get the config. should respond with converted data

            if (player.hasPermissionLevel(permLevel)){
            String itemGet=buf.readString();
            Object[] ob = Config.configMap.get(itemGet);
            if (ob!=null) {
                HashMap<String, Object[]> sendMap = new HashMap<>();
                sendMap.put(itemGet, ob);
                HashMap<String, String> transmitData = Config.configOtoS(sendMap);
                PacketByteBuf data = PacketByteBufs.create();
                data.writeMap(transmitData, PacketByteBuf::writeString, PacketByteBuf::writeString);
                ServerPlayNetworking.send(player, send_single_config_packet, data);
                }else{
                Bookofblocks.mylogger.atError().log("Config Object Map returned null");
            }
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(clear_packet,(server,player,handler,buf,pktSnd)->{
                int itemID=buf.readInt();
                int cleared=invManager.inv_clearItem(player,itemID);
                String itemName = String.valueOf(Registries.ITEM.get(itemID).asItem());
                itemName = itemName.replaceAll("_", " ").toLowerCase();
                itemName = WordUtils.capitalizeFully(itemName);
                MutableText txtCleared = Text.translatable("bob.msg.cleared");
                MutableText txtFromInv = Text.translatable("bob.msg.fromInventory");
                player.sendMessage(txtCleared.append(" "+cleared+" "+itemName).append(txtFromInv), false);
        });

        ServerPlayNetworking.registerGlobalReceiver(get_config_packet,  (server, player, handler, buf, pktSnd) -> {
            try {
                if (player.hasPermissionLevel(permLevel)) {
                    Config.getConfigPacket(player, buf, handler);
                }
                GuiItem.sendMJMRefresh(handler.getPlayer());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(get_single_config_packet,  (server, player, handler, buf, pktSnd) -> {
            try {
                if (player.hasPermissionLevel(permLevel)) {
                    Config.getSingleConfigPacket(player, buf, handler);
                }
                GuiItem.sendMJMRefresh(handler.getPlayer());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(give_packet, (server, player, handler, buf, pktSnd) -> {
            int doInt=buf.readInt();
            ServerPlayerEntity doPlayer = handler.getPlayer();
            //SERVER -- Register give-item packet listener
            invManager.inv_giveItem(doPlayer,doInt,true);
        });
        ServerPlayNetworking.registerGlobalReceiver(give_packet_single, (server, player, handler, buf, pktSnd) -> {
            int doInt=buf.readInt();
            ServerPlayerEntity doPlayer = handler.getPlayer();
            //SERVER -- Register give-item packet listener
            invManager.inv_giveItem(doPlayer,doInt,false);
        });
        ServerPlayNetworking.registerGlobalReceiver(pay_packet, (server, player, handler, buf, pktSnd) -> {
            //SERVER -- Register pay-item packet listener
            int[] getData = buf.readIntArray();
            try {
                HashMap<String, int[]> playerFile = PlayerFileManager.getPlayerFile(handler.getPlayer());
                String iName = String.valueOf(Registries.ITEM.get(getData[0]).asItem());
                assert playerFile != null;
                int[] iDetails = playerFile.get(iName);
                //System.out.println("Item name: " + iName);
                Object[] configInfo = Config.configMap.get(iName);
                //iDetails[0] -- unlocked
                //iDetails[1] -- amt paid
                System.out.println("Pack Rec'd");
                if (payPacketStatus.get() == false)
                {
                    payPacketStatus.set(true);
                    if (configInfo != null) {
                        //if the config has the item
                        //System.out.println("config has item");
                        if (((int) configInfo[1] == 1) || ((int) configInfo[1] == 3) || ((int) configInfo[1] == 4)) {
                            //if the config says its researchable
                            //System.out.println("Item is researchable");
                            if (iDetails != null) {
                                //System.out.println("iDetails: " + iDetails[0] + " " + iDetails[1] + " ");
                                if (iDetails[0] == 0) {
                                    //System.out.println("Player still needs it: " + iDetails[1]);
                                    //if the item still needs researched
                                    if (iDetails[1] < (int) configInfo[2]) {
                                        //if player file has contributed LESS than required unlock amt
                                        //System.out.println("configinfo2: " + configInfo[2]);
                                        int maxPayAmt = (int) configInfo[2] - iDetails[1];
                                        int payAmt = Math.min(maxPayAmt, getData[1]);
                                        int dedAmt = invManager.inv_PayItem(handler.getPlayer(), getData[0], payAmt);
                                        //System.out.println("item: " + iName + "  dedAmt: " + dedAmt + "  payAmt: " + payAmt);
                                        PlayerFileManager.writePlayerFile(handler.getPlayer(), iName, dedAmt);
                                    } else {
                                        PlayerFileManager.writePlayerFile(handler.getPlayer(), iName, 0);//do a write to the player file with a 0 count, to trip the update for unlock
                                    }
                                } else {
                                    Bookofblocks.mylogger.atError().log("User attempting to send data to server when item already unlocked -- SEND REFRESH SIGNAL");
                                }
                            } else {
                                //System.out.println("Item not in system: ");
                                int maxPayAmt = (int) configInfo[2];
                                int payAmt = Math.min(maxPayAmt, getData[1]);
                                int dedAmt = invManager.inv_PayItem(handler.getPlayer(), getData[0], payAmt);
                                //System.out.println("item: " + iName + "  dedAmt: " + dedAmt + "  payAmt: " + payAmt);
                                PlayerFileManager.writePlayerFile(handler.getPlayer(), iName, dedAmt);
                            }
                        } else {
                            Bookofblocks.mylogger.atError().log("Item not able to be researched -- illegal packet send. -- SEND REFRESH SIGNAL");
                        }
                    } else {
                        Bookofblocks.mylogger.atError().log("Item not listed in config file -- illegal packet send. -- SEND REFRESH SIGNAL");
                    }
                    payPacketStatus.set(false);
            } else{
                    Bookofblocks.mylogger.atError().log("Multiple Pay packets received. Cancelling subsequent packets");
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    GuiItem.sendMJMRefresh(handler.getPlayer());
                }
            }, 100);
        });

        mylogger.atInfo().log("Mod Initialized");

    }
}
