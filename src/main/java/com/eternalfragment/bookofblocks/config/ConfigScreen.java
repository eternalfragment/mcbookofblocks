package com.eternalfragment.bookofblocks.config;

import com.eternalfragment.bookofblocks.Bookofblocks;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigScreen {
    public static void callBuildScreen(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender){
        //this method is in place to ensure the call to build the menu screen only calls from the client and not the server instance -- primarily in singleplayer
        client.execute(() -> {
            buildScreen();
        });

    }
    public static void buildScreen() {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(MinecraftClient.getInstance().currentScreen)
                .setTitle(Text.translatable("bob.config.title"));
        HashMap<String, Object[]> localConfigMap=Config.configMap;
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        int numOptions = localConfigMap.size();
        AtomicReference<Item>[] configItem= new AtomicReference[numOptions];
        AtomicReference<String>[] configResearchable= new AtomicReference[numOptions];
        AtomicReference<Integer>[] configReqAmt= new AtomicReference[numOptions];
        AtomicReference<Integer>[] configGiveAmt= new AtomicReference[numOptions];
        AtomicReference<String>[] configScbObj= new AtomicReference[numOptions];
        AtomicReference<Integer>[] configScbAmt= new AtomicReference[numOptions];
        int itemIt=0;
        String[] nameArray=new String[numOptions];
        Iterable<String> resSelections =new Iterable<String>() {
            @NotNull
            @Override
            public Iterator<String> iterator() {
                List<String> ulchoices= new ArrayList<String>();
                ulchoices.add("0 - "+Text.translatable("bob.config.menu.research.remove").getString());
                ulchoices.add("1 - "+Text.translatable("bob.config.menu.research.pay").getString());
                ulchoices.add("2 - "+Text.translatable("bob.config.menu.research.score").getString());
                ulchoices.add("3 - "+Text.translatable("bob.config.menu.research.payascore").getString());
                ulchoices.add("4 - "+Text.translatable("bob.config.menu.research.payoscore").getString());
                Iterator<String> ulIterator = ulchoices.iterator();
                return ulIterator;
            }
        };
        AtomicReference<Item> addItem= new AtomicReference();
        AtomicReference<String> addResearchable= new AtomicReference();
        AtomicReference<Integer> addReqAmt= new AtomicReference();
        AtomicReference<Integer> addGiveAmt= new AtomicReference();
        AtomicReference<String> addScbObj= new AtomicReference();
        AtomicReference<Integer> addScbAmt= new AtomicReference();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("bob.config.menu.titleGeneral"));
        //New Item listing in General Tab
        general.addEntry(entryBuilder.startTextDescription(Text.translatable("bob.config.menu.addItem")).build());
        general.addEntry(entryBuilder.startDropdownMenu(Text.translatable("bob.config.menu.item"),  DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(Items.DIRT))
                .setDefaultValue(Items.DIRT) // You should define a default value here
                .setSelections(Registries.ITEM.stream().collect(Collectors.toSet()))
                .setSaveConsumer(item -> addItem.set(item)) // You should save it here, cast the item because Java is "smart"
                .build());
        //Researchable Option
        general.addEntry(entryBuilder.startStringDropdownMenu(Text.translatable("bob.config.menu.researchable"), "1 - "+Text.translatable("bob.config.menu.research.pay").getString())
                .setDefaultValue("1 - "+Text.translatable("bob.config.menu.research.pay").getString())
                .setSelections(resSelections)
                .setSaveConsumer(result->addResearchable.set(result))
                .build());
        //Research Amount
        general.addEntry(entryBuilder.startIntField(Text.translatable("bob.config.menu.resAmt"), 512)
                .setDefaultValue(512)
                .setSaveConsumer(result->addReqAmt.set(result))
                .build());
        //Give Amount
        general.addEntry(entryBuilder.startIntSlider(Text.translatable("bob.config.menu.giveAmt"), 64,1,64)
                .setDefaultValue(64)
                .setSaveConsumer(result->addGiveAmt.set(result))
                .build());
        //Scoreboard Objective
        general.addEntry(entryBuilder.startStrField(Text.translatable("bob.config.menu.scbName"),"")
                .setDefaultValue("")
                .setSaveConsumer(result->addScbObj.set(result))
                .build());
        //Scoreboard Amount
        general.addEntry(entryBuilder.startIntField(Text.translatable("bob.config.menu.scbGoal"), 0)
                .setDefaultValue(0)
                .setSaveConsumer(result->addScbAmt.set(result))
                .build());

        for (Map.Entry<String, Object[]> entry : localConfigMap.entrySet()) {
            nameArray[itemIt]=entry.getKey();
            Object[] ob1 = entry.getValue();
            itemIt++;
        }
        nameArray = Stream.of(nameArray).sorted().toArray(String[]::new);
        for (int i=0;i<nameArray.length;i++){

            String object =nameArray[i];
            Object[] ob = localConfigMap.get(object);
            //Item Option
            configItem[i] = new AtomicReference<>(Registries.ITEM.get((Integer) ob[0]));
            configResearchable[i] = new AtomicReference(ob[1]);
            configReqAmt[i]=new AtomicReference(ob[2]);
            configGiveAmt[i]=new AtomicReference(ob[3]);
            configScbObj[i]=new AtomicReference(ob[4]);
            configScbAmt[i]=new AtomicReference(ob[5]);
        }
        builder.setSavingRunnable(()->{
            //Storing data in string map, only for packet transmission (cannot transmit object[]-- will convert on other side)
            HashMap<String, String> tempConfigMap = new HashMap<String, String>();
            //existing config changes
            for (int s=0; s<numOptions; s++){
                Object[] data=new Object[6];
                String key=configItem[s].get().toString();
                data[0]=GetItemIdFromName.getItemIdFromName(key);
                data[1]=configResearchable[s].get();
                data[2]=Math.abs(Integer.parseInt(configReqAmt[s].get().toString().replaceAll("[^0-9]", "")));
                data[3]=Math.abs(Integer.parseInt(configGiveAmt[s].get().toString().replaceAll("[^0-9]", "")));
                data[4]=configScbObj[s].get().replaceAll("[^a-zA-Z0-9.+_-]", "");
                data[5]=Math.abs(Integer.parseInt(configScbAmt[s].get().toString().replaceAll("[^0-9]", "")));
                String strData=data[0]+"|"+data[1]+"|"+data[2]+"|"+data[3]+"|"+data[4]+"|"+data[5];
                if (configResearchable[s].get()!="0"){
                    tempConfigMap.put(key,strData);
                }
            }
            //New Config
                Object[] newData=new Object[6];
                String newKey=addItem.get().toString();
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
                if (newResearchInt!=0){
                    tempConfigMap.put(newKey,newStrData);
                }

            PacketByteBuf data = PacketByteBufs.create();
            data.writeMap(tempConfigMap, PacketByteBuf::writeString, PacketByteBuf::writeString);
            ClientPlayNetworking.send(Bookofblocks.get_config_packet, data);//send the config data set here to the server for parsing/saving.
        });
        Screen screen = builder.build();//Build the screen
        MinecraftClient.getInstance().setScreen(screen); //Set the screen
    }
}
