package com.eternalfragment.mcjourneymode.config;

import com.eternalfragment.mcjourneymode.Mcjourneymode;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SingleConfigScreen {
    public static void callBuildScreen(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender,String openName){
        //this method is in place to ensure the call to build the menu screen only calls from the client and not the server instance -- primarily in singleplayer
        client.execute(() -> {
            buildScreen(openName);
        });
    }

    public static void buildScreen(String itemName) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(MinecraftClient.getInstance().currentScreen)
                .setTitle(new TranslatableText("mjm.config.title"));
        HashMap<String, Object[]> localConfigMap=Config.configMap;
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        AtomicReference<String> addResearchable= new AtomicReference();
        AtomicReference<Integer> addReqAmt= new AtomicReference();
        AtomicReference<Integer> addGiveAmt= new AtomicReference();
        AtomicReference<String> addScbObj= new AtomicReference();
        AtomicReference<Integer> addScbAmt= new AtomicReference();
        Iterable<String> resSelections =new Iterable<String>() {
            @NotNull
            @Override
            public Iterator<String> iterator() {
                List<String> ulchoices= new ArrayList<String>();
                ulchoices.add("0 - "+new TranslatableText("mjm.config.menu.research.remove").getString());
                ulchoices.add("1 - "+new TranslatableText("mjm.config.menu.research.pay").getString());
                ulchoices.add("2 - "+new TranslatableText("mjm.config.menu.research.score").getString());
                ulchoices.add("3 - "+new TranslatableText("mjm.config.menu.research.payascore").getString());
                ulchoices.add("4 - "+new TranslatableText("mjm.config.menu.research.payoscore").getString());

                Iterator<String> ulIterator = ulchoices.iterator();
                return ulIterator;
            }
        };
        Object[] ob = localConfigMap.get(itemName);
        ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("mjm.config.menu.titleGeneral"));

        String cleanObject = itemName;
        cleanObject=cleanObject.replace("_"," ");
        cleanObject= WordUtils.capitalizeFully(cleanObject);
        general.addEntry(entryBuilder.startTextDescription(Text.of("Edit Item: "+cleanObject)).build());
        //Researchable Option
        general.addEntry(entryBuilder.startStringDropdownMenu(new TranslatableText("mjm.config.menu.researchable"), Integer.toString((Integer) ob[1]))
                .setDefaultValue(Integer.toString((Integer) ob[1]))
                .setSelections(resSelections)
                .setSaveConsumer(result->addResearchable.set(result))
                .build());
        //Research Amount
        general.addEntry(entryBuilder.startIntField(new TranslatableText("mjm.config.menu.resAmt"), (Integer) ob[2])
                .setDefaultValue((Integer) ob[2])
                .setSaveConsumer(result->addReqAmt.set(result))
                .build());
        //Give Amount
        general.addEntry(entryBuilder.startIntSlider(new TranslatableText("mjm.config.menu.giveAmt"), (Integer) ob[3],1,64)
                .setDefaultValue((Integer) ob[3])
                .setSaveConsumer(result->addGiveAmt.set(result))
                .build());
        //Scoreboard Objective
        general.addEntry(entryBuilder.startStrField(new TranslatableText("mjm.config.menu.scbName"),(String) ob[4])
                .setDefaultValue((String) ob[4])
                .setSaveConsumer(result->addScbObj.set(result))
                .build());
        //Scoreboard Amount
        general.addEntry(entryBuilder.startIntField(new TranslatableText("mjm.config.menu.scbGoal"), (Integer) ob[5])
                .setDefaultValue((Integer) ob[5])
                .setSaveConsumer(result->addScbAmt.set(result))
                .build());


        builder.setSavingRunnable(()->{
            //Storing data in string map, only for packet transmission (cannot transmit object[]-- will convert on other side)
            HashMap<String, String> tempConfigMap = new HashMap<String, String>();
            //New Config
            Object[] newData=new Object[6];
            String newKey=itemName;
            int newResearchInt = 0;
            if (addResearchable.get().contains("0")){
                newResearchInt=0;
            }
            if (addResearchable.get().contains("1")){
                newResearchInt=1;
            }
            if (addResearchable.get().contains("2")){
                newResearchInt=2;
            }
            if (addResearchable.get().contains("3")){
                newResearchInt=3;
            }
            if (addResearchable.get().contains("4")){
                newResearchInt=4;
            }
            newData[0]=GetItemIdFromName.getItemIdFromName(newKey);
            newData[1]=newResearchInt;
            newData[2]=Math.abs(Integer.parseInt(addReqAmt.get().toString().replaceAll("[^0-9]", "")));
            newData[3]=Math.abs(Integer.parseInt(addGiveAmt.get().toString().replaceAll("[^0-9]", "")));
            newData[4]=addScbObj.get().replaceAll("[^a-zA-Z0-9.+_-]", "");
            newData[5]=Math.abs(Integer.parseInt(addScbAmt.get().toString().replaceAll("[^0-9]", "")));

            String newStrData=newData[0]+"|"+newData[1]+"|"+newData[2]+"|"+newData[3]+"|"+newData[4]+"|"+newData[5];
            tempConfigMap.put(newKey,newStrData);//for the single screen, send data regardless of research status. removal takes place on server side
            PacketByteBuf data = PacketByteBufs.create();
            data.writeMap(tempConfigMap, PacketByteBuf::writeString, PacketByteBuf::writeString);
            ClientPlayNetworking.send(Mcjourneymode.get_single_config_packet, data);//send the config data set here to the server for parsing/saving.
        });
        Screen screen = builder.build();//Build the screen
        MinecraftClient.getInstance().setScreen(screen); //Set teh screen
    }
}
