package com.eternalfragment.bookofblocks.items;

import com.eternalfragment.bookofblocks.Bookofblocks;
import com.eternalfragment.bookofblocks.PlayerFileManager;
import com.eternalfragment.bookofblocks.config.Config;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;

public class GuiItem extends Item{
    private DefaultedList<ItemStack> items = DefaultedList.ofSize(32, ItemStack.EMPTY);

    public GuiItem(Settings settings) {
        super(settings);
    }
    public static void sendMJMRefresh(PlayerEntity user){
        PacketByteBuf data = PacketByteBufs.create();
        try {
            HashMap<String, int[]> playerMap = PlayerFileManager.generatePlayerList(user, Config.configMap);
            data.writeMap(playerMap, PacketByteBuf::writeString, PacketByteBuf::writeIntArray);
            ServerPlayerEntity sUser = (ServerPlayerEntity) user;
            if (sUser.hasPermissionLevel(Bookofblocks.permLevel)){
                ServerPlayNetworking.send(sUser, Bookofblocks.menu_populate_perms, data);
            }else{
                ServerPlayNetworking.send(sUser, Bookofblocks.menu_populate, data);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) {
            return super.use(world, user, hand);
        }
        //check if player's file is created, if no, generate the file
        try {
            PlayerFileManager.writePlayerFile(user, "__SETUP__", 0);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        sendMJMRefresh(user);
        return super.use(world, user, hand);
    }

}