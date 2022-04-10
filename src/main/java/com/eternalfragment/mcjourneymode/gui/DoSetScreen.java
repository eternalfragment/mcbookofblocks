package com.eternalfragment.mcjourneymode.gui;

import com.eternalfragment.mcjourneymode.config.Config;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.function.Function;

public class DoSetScreen {
    public static void doSetScreen(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender){
        Function<PacketByteBuf, String> keyConsumer = PacketByteBuf::readString;
        Function<PacketByteBuf, Object[]> valConsumer = (PacketByteBuf pbb) -> (Object[]) ArrayUtils.toObject(pbb.readIntArray());
        Config.playerConfigMap = new HashMap<String, Object[]>(buf.readMap(keyConsumer, valConsumer));
        client.execute(() -> {
            ScreenList daScreen = new ScreenList(new ScreenListGui(Config.playerConfigMap, "",false));
            MinecraftClient.getInstance().setScreen(daScreen);
        });

    }
    public static void doSetScreenPerms(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender){
        Function<PacketByteBuf, String> keyConsumer = PacketByteBuf::readString;
        Function<PacketByteBuf, Object[]> valConsumer = (PacketByteBuf pbb) -> (Object[]) ArrayUtils.toObject(pbb.readIntArray());
        Config.playerConfigMap = new HashMap<String, Object[]>(buf.readMap(keyConsumer, valConsumer));
        client.execute(() -> {
            ScreenList daScreen = new ScreenList(new ScreenListGui(Config.playerConfigMap, "",true));
            MinecraftClient.getInstance().setScreen(daScreen);
        });
    }
}
