package com.eternalfragment.mcjourneymode.config;


import com.eternalfragment.mcjourneymode.Mcjourneymode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.spongepowered.include.com.google.gson.JsonParseException;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Config {
    public static HashMap<String, Object[]> configMap = new HashMap<String, Object[]>();

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
                System.out.println("ERROR NULL IN CLASSLOADER");
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
        File directory = new File(Mcjourneymode.worldPath);
        if (directory.exists()) {
            success = true;
        } else {
            success = directory.mkdir();
            if (!success){
                Mcjourneymode.mylogger.atError().log("Failed to access world directory");
            }
        }
        //Create new directory for mod data
        directory = new File(Mcjourneymode.worldPath + "/" + Mcjourneymode.modDir);
        if (directory.exists()) {
            success = true;
        } else {
            success = directory.mkdir();
            if (!success){
                Mcjourneymode.mylogger.atError().log("Failed to create working directory");
            }
        }
        // Creating new directory in Java, if it doesn't exist
        directory = new File(Mcjourneymode.worldPath + "/" + Mcjourneymode.modPlayerDir);
        if (directory.exists()) {
            success = true;
        } else {
            success = directory.mkdir();
            if (!success){
                Mcjourneymode.mylogger.atError().log("Failed to create player directory");
            }
        }
        if (success) {
            // Creating new config from default, if it does not exist
            File defaultConfig = getResourceAsFile("jsons/default_config.json");
            File f = new File(Mcjourneymode.worldPath + "/" + Mcjourneymode.modDir + "config.json");
            File f2 = new File(Mcjourneymode.worldPath + "/" + Mcjourneymode.modDir + "default_config.json");
            if (f.exists()) {
            } else {
                success = copyContent(defaultConfig, f);
                if (success) {
                    Mcjourneymode.mylogger.atInfo().log("Successfully created default config file");
                } else {
                    Mcjourneymode.mylogger.atError().log("Failed to create default config file");
                }
            }
            if (!f2.exists()) {
                success = copyContent(defaultConfig, f2);
            }
        } else {
            Mcjourneymode.mylogger.atFatal().log("Directories failed to initialize. Config failure. Relaunch");
        }
    }
    static void getConfig() throws Exception {
        //map structure ['name'][0-ITEM ID, 1-researchable,2-req_amt,3-give-amt];
        try {
            Object obj = new JSONParser().parse(new FileReader(Mcjourneymode.worldPath + "/" + Mcjourneymode.modDir + "config.json"));
            JSONObject jo = (JSONObject) obj;
            JSONArray ja = (JSONArray) jo.get("Items");
            System.out.println("WORKIN");
            for (Object o : ja) {
                Iterator<Map.Entry> itr1 = ((Map) o).entrySet().iterator();
                String namevalue = "";
                Object[] settingvalue = new Object[6];
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
                            namevalue = (String) pair.getValue();
                            namevalue = namevalue.toLowerCase().replaceAll("[^a-zA-Z0-9.+_-]", "");
                            int itemID = GetItemIdFromName.getItemIdFromName(namevalue);
                            settingvalue[0] = itemID;
                        }
                        case "Researchable" -> settingvalue[1] = Math.abs(Integer.parseInt(pair.getValue().toString().replaceAll("[^0-9]", "")));
                        case "req_amt" -> settingvalue[2] = Math.abs(Integer.parseInt(pair.getValue().toString().replaceAll("[^0-9]", "")));
                        case "give_amt" -> settingvalue[3] = Math.abs(Integer.parseInt(pair.getValue().toString().replaceAll("[^0-9]", "")));
                        case "scb_obj" -> settingvalue[4] = pair.getValue().toString().replaceAll("[^a-zA-Z0-9.+_-]", "");
                        case "scb_amt" -> settingvalue[5] = Math.abs(Integer.parseInt(pair.getValue().toString().replaceAll("[^0-9]", "")));
                        default -> Mcjourneymode.mylogger.atError().log("INVALID CONFIG IMPORT: " + pair.getValue());
                    }
                }
                if ((int) settingvalue[1] != 0) {
                    configMap.put(namevalue, settingvalue);
                }
            }
        }
        catch (Exception e){
            Mcjourneymode.mylogger.atFatal().log("MOD WILL NOT WORK! Error loading config file (illegal character).");
            e.printStackTrace();
        }
    }
    public static void main() throws Exception {
        setupConfig();
        getConfig();
    }
    public static HashMap<String, Object[]> playerConfigMap = new HashMap<>();
    //playerConfigMap: ['ItemName'][0-ITEM ID, 1-researchable,2-req_amt,3-give_amt, 4-paid_amt, 5-unocked];
    public static HashMap<String, int[]> globalConfigMap = new HashMap<>();//TODO: implement possible weather
}