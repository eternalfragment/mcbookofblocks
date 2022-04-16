package com.eternalfragment.mcjourneymode;


import com.eternalfragment.mcjourneymode.commands.Mjm_cmd_give;
import com.eternalfragment.mcjourneymode.config.Config;
import com.eternalfragment.mcjourneymode.config.ConfigScreen;
import com.eternalfragment.mcjourneymode.config.SingleConfigScreen;
import com.eternalfragment.mcjourneymode.gui.DoSetScreen;
import com.eternalfragment.mcjourneymode.items.GuiItem;
import com.eternalfragment.mcjourneymode.items.MjmInInventoryAlertItem;
import com.eternalfragment.mcjourneymode.operators.invManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;


public class Mcjourneymode implements ModInitializer {
    public static org.apache.logging.log4j.Logger mylogger = LogManager.getLogger();
    public static Item GUI_ITEM = new GuiItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static Item InInventoryAlertItem = new MjmInInventoryAlertItem(new Item.Settings().maxCount(0));
    public static String worldPath = "world"; //default setting is 'world' if system is unable to detect world from settings, it will default to this
    public static final String MOD_ID = "mjm";
    public static final String modDir = "mcjourneymode\\";
    public static final String modPlayerDir = "mcjourneymode\\players\\";
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
        Registry.register(Registry.ITEM, new Identifier(Mcjourneymode.MOD_ID, "menu_item"), GUI_ITEM);
        Registry.register(Registry.ITEM, new Identifier(Mcjourneymode.MOD_ID, "in_inv_alert"), InInventoryAlertItem);
        type = FabricLoader.getInstance().getEnvironmentType();
        give_packet =  give_packet.tryParse("mjm:process_give");
        give_packet_single=give_packet_single.tryParse("mjm:process_give_single");
        pay_packet = pay_packet.tryParse("mjm:process_pay");
        clear_packet = clear_packet.tryParse("mjm:process_clear");
        sp_dir_packet = sp_dir_packet.tryParse("mjm:sp_directory_find");
        menu_populate = menu_populate.tryParse("mjm:menu_populate");
        menu_populate_perms = menu_populate_perms.tryParse("mjm:menu_populate_perms");
        get_config_packet = get_config_packet.tryParse("mjm:get_config_packet");
        send_config_req_packet = send_config_req_packet.tryParse("mjm:send_config_req_packet");
        send_single_config_req_packet=send_single_config_req_packet.tryParse("mjm:send_single_config_req_packet");
        send_config_packet = send_config_packet.tryParse("mjm:send_config_packet");
        send_single_config_packet=send_single_config_packet.tryParse("mjm:send_single_config_packet");
        get_single_config_packet=get_single_config_packet.tryParse("mjm:get_single_config_packet");

        if (Objects.equals(type.toString(), "CLIENT")){

            CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> new Mjm_cmd_give(dispatcher));

            ClientPlayNetworking.registerGlobalReceiver(sp_dir_packet, (client, handler, buf, pktSnd) -> {
                worldPath =  Objects.requireNonNull(client.getServer()).getSavePath(WorldSavePath.ROOT).toString().replaceAll("\\.+$","");
                try {
                    Config.main();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            ClientPlayNetworking.registerGlobalReceiver(send_config_packet, (client, handler, buf, pktSnd) -> {
                Function<PacketByteBuf, String> keyConsumer = PacketByteBuf::readString;
                Function<PacketByteBuf, String> valConsumer = PacketByteBuf::readString;
                HashMap<String, String> getMap = new HashMap<>(buf.readMap(keyConsumer, valConsumer));

                Config.configMap= Config.configStoO(getMap);
                ConfigScreen.callBuildScreen(client,handler,buf,pktSnd);

            });
            ClientPlayNetworking.registerGlobalReceiver(send_single_config_packet, (client, handler, buf, pktSnd) -> {
                Function<PacketByteBuf, String> keyConsumer = PacketByteBuf::readString;
                Function<PacketByteBuf, String> valConsumer = PacketByteBuf::readString;
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
                ServerPlayNetworking.send(handler.getPlayer(),Mcjourneymode.sp_dir_packet, data);
            });
            ClientPlayNetworking.registerGlobalReceiver(menu_populate, DoSetScreen::doSetScreen);
            ClientPlayNetworking.registerGlobalReceiver(menu_populate_perms, DoSetScreen::doSetScreenPerms);
        }
        if (Objects.equals(type.toString(), "SERVER")){
            CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> new Mjm_cmd_give(dispatcher));
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
                }else{System.out.println("OB NULL");}
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(clear_packet,(server,player,handler,buf,pktSnd)->{
                int itemID=buf.readInt();
                int cleared=invManager.inv_clearItem(player,itemID);
                String itemName = String.valueOf(Registry.ITEM.get(itemID).asItem());
                itemName = itemName.replaceAll("_", " ").toLowerCase();
                itemName = WordUtils.capitalizeFully(itemName);
                player.sendMessage(Text.of(new TranslatableText("mjm.msg.cleared").toString()+" "+cleared+" "+itemName+new TranslatableText("mjm.msg.fromInventory").toString()), false);
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
                String iName = String.valueOf(Registry.ITEM.get(getData[0]).asItem());
                assert playerFile != null;
                int[] iDetails = playerFile.get(iName);
                Object[] configInfo = Config.configMap.get(iName);
                //iDetails[0] -- unlocked
                //iDetails[1] -- amt paid
                if (configInfo != null) {
                    //if the config has the item
                    if (((int)configInfo[1] == 1)||((int)configInfo[1] == 3)||((int)configInfo[1] == 4)) {
                        //if the config says its researchable
                        if (iDetails != null) {
                            if (iDetails[0] == 0) {
                                //if the item still needs researched
                                if (iDetails[1] < (int)configInfo[2]) {
                                    //if player file has contributed LESS than required unlock amt
                                    int maxPayAmt = (int)configInfo[2] - iDetails[1];
                                    int payAmt = Math.min(maxPayAmt, getData[1]);
                                    int dedAmt = invManager.inv_PayItem(handler.getPlayer(), getData[0], payAmt);
                                    PlayerFileManager.writePlayerFile(handler.getPlayer(), iName, dedAmt);
                                } else {
                                    PlayerFileManager.writePlayerFile(handler.getPlayer(), iName, 0);//do a write to the player file with a 0 count, to trip the update for unlock
                                }
                            } else {
                                Mcjourneymode.mylogger.atError().log("User attempting to send data to server when item already unlocked -- SEND REFRESH SIGNAL");
                            }
                        } else {
                            int maxPayAmt = (int)configInfo[2];
                            int payAmt = Math.min(maxPayAmt, getData[1]);
                            int dedAmt = invManager.inv_PayItem(handler.getPlayer(), getData[0], payAmt);
                            PlayerFileManager.writePlayerFile(handler.getPlayer(), iName, dedAmt);
                        }
                    } else {
                        Mcjourneymode.mylogger.atError().log("Item not able to be researched -- illegal packet send. -- SEND REFRESH SIGNAL");
                    }
                } else {
                    Mcjourneymode.mylogger.atError().log("Item not listed in config file -- illegal packet send. -- SEND REFRESH SIGNAL");
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
