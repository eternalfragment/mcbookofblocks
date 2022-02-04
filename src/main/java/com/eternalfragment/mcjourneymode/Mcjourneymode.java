package com.eternalfragment.mcjourneymode;


import com.eternalfragment.mcjourneymode.commands.Mjm_cmd_give;
import com.eternalfragment.mcjourneymode.config.Config;
import com.eternalfragment.mcjourneymode.gui.DoSetScreen;
import com.eternalfragment.mcjourneymode.items.GuiItem;
import com.eternalfragment.mcjourneymode.operators.invManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
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
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.json.simple.parser.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import static net.minecraft.server.command.CommandManager.literal;


public class Mcjourneymode implements ModInitializer {
    public static org.apache.logging.log4j.Logger mylogger = LogManager.getLogger();
    public static Item GUI_ITEM = new GuiItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static String worldPath = "world"; //default setting is 'world' if system is unable to detect world from settings, it will default to this
    public static final String MOD_ID = "mjm";
    public static final String modDir = "mcjourneymode\\";
    public static final String modPlayerDir = "mcjourneymode\\players\\";
    public static Identifier pay_packet=null;
    public static Identifier give_packet=null;
    public static Identifier sp_dir_packet=null;
    public static Identifier menu_populate = null;
    public static EnvType type;//holds whether mod is loaded in client or server
    @Override
    public void onInitialize() {

        mylogger.atInfo().log("Mod Booting....");
        Registry.register(Registry.ITEM, new Identifier(Mcjourneymode.MOD_ID, "menu_item"), GUI_ITEM);
        type = FabricLoader.getInstance().getEnvironmentType();

        //System.out.println("Env: " + type);
        give_packet = give_packet.tryParse("mjm:process_give");
        pay_packet = pay_packet.tryParse("mjm:process_pay");
        sp_dir_packet = sp_dir_packet.tryParse("mjm:sp_directory_find");
        menu_populate = menu_populate.tryParse("mjm:menu_populate");

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
            ServerPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
                //Register the client to detect when the player connects to the local server. When that happens, send a packet. If received by client, generate/load singleplayer config.
                PacketByteBuf data = PacketByteBufs.create();
                ServerPlayNetworking.send(handler.getPlayer(),Mcjourneymode.sp_dir_packet, data);
            });
            ClientPlayNetworking.registerGlobalReceiver(menu_populate, DoSetScreen::doSetScreen);
        }
        if (type.toString() == "SERVER"){
            CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> new Mjm_cmd_give(dispatcher));
            ServerLifecycleEvents.SERVER_STARTED.register((handler)->{
                System.out.println("Server Started");
                worldPath =  handler.getSavePath(WorldSavePath.ROOT).toString().replaceAll("\\.+$","");
                try {
                    Config.main();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        ServerPlayNetworking.registerGlobalReceiver(give_packet, (server, player, handler, buf, pktSnd) -> {
            int doInt=buf.readInt();
            ServerPlayerEntity doPlayer = handler.getPlayer();
            //SERVER -- Register give-item packet listener
            invManager.inv_giveItem(doPlayer,doInt);
        });
        ServerPlayNetworking.registerGlobalReceiver(pay_packet, (server, player, handler, buf, pktSnd) -> {
            //SERVER -- Register pay-item packet listener
            int[] getData = buf.readIntArray();
            try {
                HashMap<String, int[]> playerFile = PlayerFileManager.getPlayerFile(handler.getPlayer());
                String iName = String.valueOf(Registry.ITEM.get(getData[0]).asItem());
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
