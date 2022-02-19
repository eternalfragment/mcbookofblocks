package com.eternalfragment.mcjourneymode;

import com.eternalfragment.mcjourneymode.config.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PlayerFileManager {

    public static boolean playerFileCreate(PlayerEntity user) throws IOException {
        //Create player's mjm tracking file if not existing
        String userID = user.getUuidAsString();
        File f = new File(Mcjourneymode.worldPath + "/" + Mcjourneymode.modPlayerDir + userID + ".json");
        if (f.exists()) {
            return true;
        } else {
            FileWriter writer = new FileWriter(f);
            writer.append("{\"Items\":[{\"Name\":\"AIR\",\"unlocked\":0,\"paid\":0}]}");
            writer.flush();
            writer.close();
            return true;
        }
    }
    public static HashMap<String, int[]> getPlayerFile(PlayerEntity user) throws IOException, ParseException {
        //get the player's file as map for tracking what the player has unlocked and their progress
        HashMap<String, int[]> mjmPlayerData = new HashMap<>();
        String userID = user.getUuidAsString();
        File f = new File(Mcjourneymode.worldPath + "\\" + Mcjourneymode.modPlayerDir + userID + ".json");
        if (f.exists()) {
            try {
            Object obj = new JSONParser().parse(new FileReader(Mcjourneymode.worldPath + "\\" + Mcjourneymode.modPlayerDir + userID + ".json"));
            JSONObject jo = (JSONObject) obj;
            if (jo.get("Items") != null) {
                //check if the player file has an 'items' wrapper. if not, error with player file
                JSONArray ja = (JSONArray) jo.get("Items");
                for (Object o : ja) {
                    Iterator<Map.Entry> itr1 = ((Map) o).entrySet().iterator();
                    String namevalue = "";
                    int[] settingvalue = new int[2];
                    while (itr1.hasNext()) {
                        Map.Entry pair = itr1.next();
                        String key = (String) pair.getKey();
                        switch (key) {
                            case "Name" -> {
                                namevalue = (String) pair.getValue();
                                namevalue = namevalue.toLowerCase().replaceAll("[^a-zA-Z0-9.+_-]", "");
                            }
                            case "unlocked" -> settingvalue[0] = Math.abs(Integer.parseInt(pair.getValue().toString().replaceAll("[^0-9]", "")));
                            case "paid" -> settingvalue[1] = Math.abs(Integer.parseInt(pair.getValue().toString().replaceAll("[^0-9]", "")));
                            default -> Mcjourneymode.mylogger.atError().log("INVALID CONFIG IMPORT: " + pair.getValue());
                        }
                    }

                    mjmPlayerData.put(namevalue, settingvalue);
                }
                return mjmPlayerData;
            } else {
                Mcjourneymode.mylogger.atError().log("Error in user file");
                return null;
            }
        }
        catch (Exception e){
                Mcjourneymode.mylogger.atFatal().log("Player File Error! Error parsing player file (illegal character). Printing stack Trace...");
                e.printStackTrace();
            }
        } else {
            Mcjourneymode.mylogger.atFatal().log("Error with player config file: " + userID);
            return null;
        }
        return mjmPlayerData;
    }
    public static JSONObject setPlayerFile(HashMap<String, int[]> rawData) throws IOException, ParseException {
//TODO: re-work playerFile method--verify new options changes
        JSONObject joNew = new JSONObject();//re-structure data from plain ints to keyed items
        JSONObject jo1 = new JSONObject();//json object to be the 'wrapper'
        JSONArray items = new JSONArray();
        for (Map.Entry<String, int[]> entry : rawData.entrySet()) {
            JSONObject jo = new JSONObject();//json object to hold the raw data
            int[] tmp = entry.getValue();
            jo.put("Name", entry.getKey());
            jo.put("paid", tmp[1]);
            jo.put("unlocked", tmp[0]);

            items.add(jo);

        }
        jo1.put("Items", items);//put the array into the main wrap
        return jo1;
    }
    public static void manualUnlockPlayerFile(PlayerEntity user, String item) throws IOException, ParseException {
        HashMap<String, int[]> mjmPlayerData = new HashMap<String, int[]>();
        String userID = user.getUuidAsString();
        boolean plFile = false;
        File f = new File(Mcjourneymode.worldPath + "\\" + Mcjourneymode.modPlayerDir + userID + ".json");
        if (f.exists()) {
            plFile = true;
        } else {
            plFile = playerFileCreate(user);
        }
        if (plFile) {
            if (Config.configMap.get(item) != null) {
                mjmPlayerData = getPlayerFile(user);
                if (mjmPlayerData.get(item) != null) {
                    //if the object is already in the player's file (in progress)
                    int[] pldata = mjmPlayerData.get(item);//get the user data
                    pldata[0]=1;
                    mjmPlayerData.put(item, pldata);
                    JSONObject plFileJson = setPlayerFile(mjmPlayerData);
                    FileWriter writer = new FileWriter(f);
                    writer.write(plFileJson.toJSONString());
                    writer.flush();
                    writer.close();
                }else{
                    //item is not yet in the player's file, thus add it with no progress, but unlocked
                    int[] pldata=new int[2];
                    pldata[0]=1;
                    pldata[1]=0;
                    mjmPlayerData.put(item, pldata);
                    JSONObject plFileJson = setPlayerFile(mjmPlayerData);
                    FileWriter writer = new FileWriter(f);
                    writer.write(plFileJson.toJSONString());
                    writer.flush();
                    writer.close();
                }
            }
        }
    }
    public static void writePlayerFile(PlayerEntity user, String item, int amt) throws IOException, ParseException {
        HashMap<String, int[]> mjmPlayerData = new HashMap<String, int[]>();
        //take the user. Item which you are going to adjust. Amt for how much they add to their list.
        //FIRST check if file exists
        //FIRST check config to see if item sent is researchable [0].
        //Function should only be called by system checking amt needed, after amt removed from acct
        String userID = user.getUuidAsString();
        boolean plFile = false;
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        File f = new File(Mcjourneymode.worldPath + "\\" + Mcjourneymode.modPlayerDir + userID + ".json");
        if (f.exists()) {
            plFile = true;
        } else {
            plFile = playerFileCreate(user);
        }

        if (plFile) {
            if (item == "__SETUP__") {
            } else {
                if (Config.configMap.get(item) != null) {
                    Object[] itemConfig = Config.configMap.get(item);
                    if ((int)itemConfig[1] > 0) {
                        //if the supplied item is permitted to be researched in any way
                        mjmPlayerData = getPlayerFile(user);

                        if (mjmPlayerData.get(item) != null) {
                            //if the item is already in the player's config

                            int[] pldata = mjmPlayerData.get(item);//get the user data
                            int currentPaid = (int)pldata[1];
                            int newPaid = currentPaid + amt;
                            pldata[1] = newPaid;
                            boolean scoreMet=false;
                            ScoreboardObjective thisObjective = user.getScoreboard().getObjective((String) itemConfig[4]);
                            if (thisObjective==null){
                                if ((int)itemConfig[1]>1){
                                    Mcjourneymode.mylogger.atError().log("Item config requires scoreboard objective, none found. Config #: "+itemConfig[1]+"| Item ID: " + itemConfig[0]);
                                }
                            }else{
                                int thisScore = user.getScoreboard().getPlayerScore(user.getDisplayName().asString(), user.getScoreboard().getObjective((String) itemConfig[4])).getScore();
                                int scoreTarget = (int)itemConfig[5];
                                if (thisScore>=scoreTarget){scoreMet=true;}
                            }
                            //
                            if ((int)itemConfig[2]<=newPaid){
                                //if the new paid amt meets the required amt --CHECK WHAT TYPE OF UNLOCK IT IS
                                switch ((int)itemConfig[1]){
                                    case 1: pldata[0]=1; break;//Only needs pay
                                    case 2: if (scoreMet){pldata[0]=1;}break;//Scoreboard only
                                    case 3: if (scoreMet){pldata[0]=1;}break;//Pay & Scoreboard
                                    case 4: pldata[0]=1; break;//Either/or
                                    default: Mcjourneymode.mylogger.atError().log("Invalid Item Research Status for item: "+itemConfig[0]);break;
                                }
                            }

                            mjmPlayerData.put(item, pldata);
                            //pldata = mjmPlayerData.get(item);
                            JSONObject plFileJson = setPlayerFile(mjmPlayerData);
                            FileWriter writer = new FileWriter(f);
                            writer.write(plFileJson.toJSONString());
                            writer.flush();
                            writer.close();
                        } else {
                            //if the item is not in the player's config, create the entry with the defined amts
                            int[] pldata=new int[2];
                            pldata[0]=0;
                            pldata[1]=amt;
                            boolean scoreMet=false;
                            ScoreboardObjective thisObjective = user.getScoreboard().getObjective((String) itemConfig[4]);
                            if (thisObjective==null){
                                if ((int)itemConfig[1]>1){
                                    Mcjourneymode.mylogger.atError().log("Item config requires scoreboard objective, none found. Config #: "+itemConfig[1]+"| Item ID: " + itemConfig[0]);
                                }
                            }else{
                                int thisScore = user.getScoreboard().getPlayerScore(user.getDisplayName().asString(), user.getScoreboard().getObjective((String) itemConfig[4])).getScore();
                                int scoreTarget = (int)itemConfig[5];
                                if (thisScore>=scoreTarget){scoreMet=true;}
                            }


                            if ((int)itemConfig[2]<=amt){
                                switch ((int)itemConfig[1]){
                                    case 1: pldata[0]=1; break;//Only needs pay
                                    case 2: if (scoreMet){pldata[0]=1;}break;//Scoreboard only
                                    case 3: if (scoreMet){pldata[0]=1;}break;//Pay & Scoreboard
                                    case 4: pldata[0]=1; break;//Either/or
                                    default: Mcjourneymode.mylogger.atError().log("Invalid Item Research Status for item: "+itemConfig[0]);break;
                                }
                            }
                            mjmPlayerData.put(item, pldata);
                            JSONObject plFileJson = setPlayerFile(mjmPlayerData);
                            FileWriter writer = new FileWriter(f);
                            writer.write(plFileJson.toJSONString());
                            writer.flush();
                            writer.close();
                        }
                    } else {
                        user.sendMessage(Text.of("The item requested is not able to be unlocked"), false);
                    }
                } else {
                    Mcjourneymode.mylogger.atError().log("Invalid item submit for player write");
                }
            }
        } else {
            Mcjourneymode.mylogger.atError().log("Error with player config file ");
        }
    }
    public static HashMap<String, int[]> generatePlayerList(PlayerEntity user, HashMap<String, Object[]> configMap) throws IOException, ParseException {
        HashMap<String, int[]> mjmPlayerSend = new HashMap<String, int[]>();
        HashMap<String, int[]> mjmPlayerData = getPlayerFile(user);
        HashMap<String, Object[]> mjmScoreboardData= new HashMap<String, Object[]>();
        Object[] configData;

        /*Iterate over config to create default map to send to player*/
        for (String name : configMap.keySet()) {
            name = name.toLowerCase();
            configData = configMap.get(name);
            int[] stuff = new int[7];
            Object[] scbStuff=new Object[2];
            stuff[0] = (int)configData[0];//item ID
            stuff[1] = (int)configData[1];//Research Status [0-4]
            stuff[2] = (int)configData[2];//Pay Amt Requirement
            stuff[3] = (int)configData[3];//Amt to give player
            stuff[4] = 0;//unlocked
            stuff[5] = 0;//paid amt
            stuff[6] = -1;//0-100% of scoreboard goal (-1 if no scoreboard or null)
            scbStuff[0]=configData[4];//Scoreboard objective name
            scbStuff[1]=(int)configData[5]; //scoreboard objective goal
            mjmPlayerSend.put(name, stuff);
            mjmScoreboardData.put(name,scbStuff);
        }
        int[] playerstuff;
        int[] configData2;
        assert mjmPlayerData != null;
        for (String name : mjmPlayerData.keySet()) {
            name = name.toLowerCase();
            playerstuff = mjmPlayerData.get(name);
            configData2 = mjmPlayerSend.get(name);
            if (configData2 != null) {
                configData2[4] = playerstuff[0];
                configData2[5] = playerstuff[1];
                mjmPlayerSend.put(name, configData2);
            }
        }
        int[] configData3;
        for (String name : mjmPlayerSend.keySet()){
            //Do check for scoreboard objectives to change unlock status [4]
            name = name.toLowerCase();
            configData3 = mjmPlayerSend.get(name);
            Object[] itemConfig = Config.configMap.get(name);
            boolean unlockable = true;
            if (itemConfig!=null)
            {
                if ((int) itemConfig[1] > 0) {
                    int scbPerc = -1;
                    boolean scoreMet = false;
                    if ((int) itemConfig[1] > 1)
                    {
                        ScoreboardObjective thisObjective = user.getScoreboard().getObjective((String) itemConfig[4]);
                        if (thisObjective == null) {
                            //if the scoreboard cannot be found
                            if ((int) itemConfig[1] > 1) {
                                //if the item has scoreboard in the criteria
                                if (((int) itemConfig[1] == 2) || ((int) itemConfig[1] == 3)) {
                                    //scoreboard is REQUIRED
                                    unlockable = false;
                                    itemConfig[1] = 0;
                                    configData3[1] = 0;
                                    Mcjourneymode.mylogger.atError().log("Item config requires scoreboard objective, none found. Item research DISABLED. Scoreboard: " + itemConfig[4] + "| Unlockable #: " + itemConfig[1] + "| Item ID: " + itemConfig[0]);
                                } else if ((int) itemConfig[1] == 4) {
                                    Mcjourneymode.mylogger.atError().log("Item config requests scoreboard objective, none found. Item enabled without scoreboard. Scoreboard: " + itemConfig[4] + "| Unlockable #: " + itemConfig[1] + "| Item ID: " + itemConfig[0]);
                                }
                            }
                        } else {
                            int thisScore = user.getScoreboard().getPlayerScore(user.getDisplayName().asString(), user.getScoreboard().getObjective((String) itemConfig[4])).getScore();
                            int scoreTarget = (int) itemConfig[5];
                            float mixed = (float) thisScore / scoreTarget;
                            scbPerc = (int) Math.floor(mixed * 100);
                            if (thisScore >= scoreTarget) {
                                scoreMet = true;
                            }
                        }
                    }
                    if (unlockable){
                    configData3[6] = scbPerc;
                    switch ((int) itemConfig[1]) {
                        case 1:
                            if (configData3[5] >= (int) itemConfig[2]) {
                                configData3[4] = 1;
                                writePlayerFile(user, name, 0);
                            }
                            break;//Only needs pay
                        case 2:
                            if (scoreMet) {
                                configData3[4] = 1;
                                writePlayerFile(user, name, 0);
                            }
                            break;//Scoreboard only
                        case 3:
                            if ((scoreMet) & (configData3[5] >= (int) itemConfig[2])) {
                                configData3[4] = 1;
                                writePlayerFile(user, name, 0);
                            }
                            break;//Pay & Scoreboard
                        case 4:
                            if ((scoreMet) || (configData3[5] >= (int) itemConfig[2])) {
                                configData3[4] = 1;
                                writePlayerFile(user, name, 0);
                            }
                            break;//Either/or
                        default:
                            Mcjourneymode.mylogger.atError().log("Invalid Item Research Status for item: " + itemConfig[0]);
                            break;
                    }
                }
                }
            }
               mjmPlayerSend.put(name, configData3);
        }
        return mjmPlayerSend;
    }

}
