package com.eternalfragment.bookofblocks.config;


import com.eternalfragment.bookofblocks.Bookofblocks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Config {

    public static HashMap<String, Object[]> configMap = new HashMap<String, Object[]>();
    //Global config map structure ['name'][0-ITEM ID, 1-researchable,2-req_amt,3-give-amt,4-scb_obj,5-scb_amt];
    private static FileWriter file;
    public static String[] tabNames={"ABCD","EFGH","IJKL","MNOP","QRST","UVW","XYZ"};
    public static boolean copyContent(File a, File b) throws Exception {
        try (FileInputStream in = new FileInputStream(a); FileOutputStream out = new FileOutputStream(b)) {
            int n;
            // read() function to read the
            // byte of data
            while ((n = in.read()) != -1) {
                // write() function to write
                // the byte of data
                out.write(n);
            }
        }
        // close() function to close the
        // stream
        // close() function to close
        // the stream
        return true;
    }
    static public String getFile(String fileName) {
        //Get file from resources folder
        ClassLoader classLoader = Config.class.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream(fileName);
        try
        {
            if (stream == null)
            {
                throw new Exception("Cannot find file " + fileName);
            }
            return "noreturn";
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
    public static File getResourceAsFile(String resourcePath) {
        try {
            InputStream in = Config.class.getClassLoader().getResourceAsStream(resourcePath);
            if (in == null) {
                Bookofblocks.mylogger.atError().log("ERROR NULL IN CLASSLOADER");
                return null;
            }
            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                //copy stream
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void setupConfig() throws Exception {
        boolean success = false;
        // Creating new directory for world, if it doesn't exist
        File directory = new File(Bookofblocks.worldPath);
        if (directory.exists()) {
            success = true;
        } else {
            success = directory.mkdir();
            if (!success){
                Bookofblocks.mylogger.atError().log("Failed to access world directory");
            }
        }
        //Create new directory for mod data
        directory = new File(Bookofblocks.worldPath + "/" + Bookofblocks.modDir);
        if (directory.exists()) {
            success = true;
        } else {
            success = directory.mkdir();
            if (!success){
                Bookofblocks.mylogger.atError().log("Failed to create working directory");
            }
        }
        // Creating new directory in Java, if it doesn't exist
        directory = new File(Bookofblocks.worldPath + "/" + Bookofblocks.modPlayerDir);
        if (directory.exists()) {
            success = true;
        } else {
            success = directory.mkdir();
            if (!success){
                Bookofblocks.mylogger.atError().log("Failed to create player directory");
            }
        }
        if (success) {
            // Creating new config from default, if it does not exist
            File defaultConfig = getResourceAsFile("jsons/default_config.json");
            File f = new File(Bookofblocks.worldPath + "/" + Bookofblocks.modDir + "config.json");
            File f2 = new File(Bookofblocks.worldPath + "/" + Bookofblocks.modDir + "default_config.json");
            if (f.exists()) {
            } else {
                success = copyContent(defaultConfig, f);
                if (success) {
                    Bookofblocks.mylogger.atInfo().log("Successfully created default config file");
                } else {
                    Bookofblocks.mylogger.atError().log("Failed to create default config file");
                }
            }
            if (!f2.exists()) {
                success = copyContent(defaultConfig, f2);
            }
        } else {
            Bookofblocks.mylogger.atFatal().log("Directories failed to initialize. Config failure. Relaunch");
        }
    }
    static void getConfig() throws Exception {
        //map structure ['name'][0-ITEM ID, 1-researchable,2-req_amt,3-give-amt,4-scb_obj,5-scb_amt];
        try {
            Object obj = new JSONParser().parse(new FileReader(Bookofblocks.worldPath + "/" + Bookofblocks.modDir + "config.json"));
            JSONObject jo = (JSONObject) obj;
            JSONArray ja = (JSONArray) jo.get("Items");
            for (Object o : ja) {
                Iterator<Map.Entry> itr1 = ((Map) o).entrySet().iterator();
                String nameValue = "";
                Object[] settingValue = new Object[6];
                //Setting Value Value Breakdown
                //0- Item ID (gotten from name in config)
                //1- Researchable status
                //      0-No Research
                //      1-Pay to unlock
                //      2-Scoreboard to Unlock
                //      3-Pay & Scoreboard to unlock
                //      4-Pay or Scoreboard to unlock
                //2- Required Pay Amt
                //3- Amt to give to player
                //4- Name of scoreboard objective for unlocking
                //5- Score required to unlock
                while (itr1.hasNext()) {
                    Map.Entry pair = itr1.next();
                    String key = (String) pair.getKey();
                    switch (key) {
                        case "Name" -> {
                            nameValue = (String) pair.getValue();
                            nameValue = nameValue.toLowerCase().replaceAll("[^a-zA-Z0-9.+_-]", "");
                            int itemID = GetItemIdFromName.getItemIdFromName(nameValue);
                            if (nameValue=="crimson_nylium"){
                                System.out.println("FOUND CRIMSON NYLIUM");
                                System.out.println("nameValue: "+nameValue);
                                System.out.println("itemID: "+itemID);

                            }
                            if (itemID==19){
                                System.out.println("19: "+nameValue);
                            }
                            settingValue[0] = itemID;
                        }
                        case "Researchable" -> settingValue[1] = Math.abs(Integer.parseInt(pair.getValue().toString().replaceAll("[^0-9]", "")));
                        case "req_amt" -> settingValue[2] = Math.abs(Integer.parseInt(pair.getValue().toString().replaceAll("[^0-9]", "")));
                        case "give_amt" -> settingValue[3] = Math.abs(Integer.parseInt(pair.getValue().toString().replaceAll("[^0-9]", "")));
                        case "scb_obj" -> settingValue[4] = pair.getValue().toString().replaceAll("[^a-zA-Z0-9.+_-]", "");
                        case "scb_amt" -> settingValue[5] = Math.abs(Integer.parseInt(pair.getValue().toString().replaceAll("[^0-9]", "")));
                        default -> Bookofblocks.mylogger.atError().log("INVALID CONFIG IMPORT: " + pair.getValue());
                    }
                }
                if (((int) settingValue[1] != 0)&&((int)settingValue[0]!=-1)) {
                    configMap.put(nameValue, settingValue);
                }
            }
        }
        catch (Exception e){
            Bookofblocks.mylogger.atFatal().log("MOD WILL NOT WORK! Error loading config file (illegal character).");
            e.printStackTrace();
        }
    }
    static void setConfig() throws Exception{
        //map structure ['name'][0-ITEM ID, 1-researchable,2-req_amt,3-give-amt,4-scb_obj,5-scb_amt];
        //Get data from config map, and store it into file
        JsonArray ja = new JsonArray();
        int numEntries=configMap.size();
        JsonObject[] jo = new JsonObject[numEntries];
        JsonObject joP = new JsonObject();
        int joIt=0;
        for (Map.Entry<String, Object[]> entry : configMap.entrySet()) {
            Object[] ob = entry.getValue();
            jo[joIt]= new JsonObject();
            jo[joIt].addProperty("Name", entry.getKey());
            jo[joIt].addProperty("Researchable", (Number) ob[1]);
            jo[joIt].addProperty("req_amt", (Number) ob[2]);
            jo[joIt].addProperty("give_amt", (Number) ob[3]);
            jo[joIt].addProperty("scb_obj", (String) ob[4]);
            jo[joIt].addProperty("scb_amt", (Number) ob[5]);
            ja.add(jo[joIt]);
            joIt++;
        }
        joP.add("Items",ja);
        Gson gson= new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(joP);

        try{
            file = new FileWriter(Bookofblocks.worldPath + "/" + Bookofblocks.modDir + "config.json");
            file.write(prettyJson);
            Bookofblocks.mylogger.atInfo().log("Successfully wrote config to file");
        }catch (IOException e){
            e.printStackTrace();
        }finally{
            file.flush();
            file.close();
        }
    }
    public static void main() throws Exception {
        setupConfig();
        getConfig();
    }
    public static HashMap<String, Object[]> playerConfigMap = new HashMap<>();
    //playerConfigMap: ['ItemName'][0-ITEM ID, 1-researchable,2-req_amt,3-give_amt, 4-paid_amt, 5-unocked];
    public static HashMap<String, int[]> globalConfigMap = new HashMap<>();//TODO:POSSIBLE-- implement other global options to store here
    public static HashMap<String, Object[]> configStoO(HashMap<String,String> stringMap){
        HashMap<String,Object[]> newMap=new HashMap<>();
        for (Map.Entry<String,String> entry: stringMap.entrySet()){
            String key=entry.getKey();
            String value=entry.getValue();
            String[] oldSettingValue= value.split("[|]");//Split string based on | delimiter
            Object[] settingvalue = new Object[6];//new Object[] to hold data
            //Cleaning Name/key
            String namevalue = key;
            namevalue = namevalue.toLowerCase().replaceAll("[^a-zA-Z0-9.+_-]", "");
            //Cleaning/setting Item ID
            int itemID = GetItemIdFromName.getItemIdFromName(namevalue);
            settingvalue[0] = itemID;
            settingvalue[1] = Math.abs(Integer.parseInt(oldSettingValue[1].toString().replaceAll("[^0-9]", "")));
            settingvalue[2] = Math.abs(Integer.parseInt(oldSettingValue[2].toString().replaceAll("[^0-9]", "")));
            settingvalue[3] = Math.abs(Integer.parseInt(oldSettingValue[3].toString().replaceAll("[^0-9]", "")));
            settingvalue[4] = oldSettingValue[4].toString().replaceAll("[^a-zA-Z0-9.+_-]", "");
            settingvalue[5] = Math.abs(Integer.parseInt(oldSettingValue[5].toString().replaceAll("[^0-9]", "")));
            newMap.put(namevalue,settingvalue);
        }
        return newMap;
    }
    public static HashMap<String, String> configOtoS(HashMap<String,Object[]> objMap){
        HashMap<String, String> tempConfigMap = new HashMap<String, String>();
        for (Map.Entry<String, Object[]> entry : objMap.entrySet()) {
            String nameKey = entry.getKey();
            Object[] data = entry.getValue();
            String strData=data[0]+"|"+data[1]+"|"+data[2]+"|"+data[3]+"|"+data[4]+"|"+data[5];
            tempConfigMap.put(nameKey,strData);
        }
        return tempConfigMap;
    }
    public static String configEntryOtoS(Object[] data){
            String strData=data[0]+"|"+data[1]+"|"+data[2]+"|"+data[3]+"|"+data[4]+"|"+data[5];
        return strData;
    }
    public static void getConfigPacket(PlayerEntity player, PacketByteBuf buf, ServerPlayNetworkHandler handler) throws Exception {
        PacketByteBuf.PacketReader<String> keyConsumer = PacketByteBuf::readString;
        PacketByteBuf.PacketReader<String> valConsumer = PacketByteBuf::readString;
        HashMap<String, String> getMap = new HashMap<String, String>(buf.readMap(keyConsumer, valConsumer));
        HashMap<String,Object[]> newMap=configStoO(getMap);
        Bookofblocks.mylogger.atInfo().log("Player updated config: "+player.getName());
        configMap=newMap;
        setConfig();

    }
    public static void getSingleConfigPacket(PlayerEntity player, PacketByteBuf buf, ServerPlayNetworkHandler handler) throws Exception {
        PacketByteBuf.PacketReader<String> keyConsumer = PacketByteBuf::readString;
        PacketByteBuf.PacketReader<String> valConsumer = PacketByteBuf::readString;
        HashMap<String, String> getMap = new HashMap<String, String>(buf.readMap(keyConsumer, valConsumer));
        HashMap<String,Object[]> newMap=configStoO(getMap);
        for (Map.Entry<String, Object[]> entry : newMap.entrySet()) {
            String nameKey = entry.getKey();
            Object[] data = entry.getValue();
            if ((int)data[1]==0){
                //if the data says researchable is 0, remove item from the config
                configMap.remove(nameKey);
            }else{
                configMap.put(nameKey,data);//take data sent, and update local config map
            }
        }
        Bookofblocks.mylogger.atInfo().log("Player updated config: "+player.getName());
        setConfig();

    }

}