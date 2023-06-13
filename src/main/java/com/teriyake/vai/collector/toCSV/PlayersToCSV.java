package com.teriyake.vai.collector.toCSV;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.Player;
import com.teriyake.vai.VaiUtil;
// ONLY RUN WITH ONE DEVICE ie: ENVY
public class PlayersToCSV {
    public static void main(String[] args) {
        File csvPath = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/CSVPlayerIndex.csv");
        File dataPath = new File(System.getProperty("user.home") + "/OneDrive/Documents/StaVa/data/player");
        String[] subPaths = dataPath.list();
        int total = 0;
        int attempted = 0;
        for(String i : subPaths) { // act folders
            File subPath = new File(dataPath, "/" + i);
            String[] playerPaths = subPath.list();
            for(String j : playerPaths) { //player
                total++;
                File playerPath = new File(subPath, "/" + j + "/" + "player.json");
                String json = VaiUtil.readFile(playerPath);
                Player player = PlayerParser.parsedJsonToPlayer(json);
                ArrayList<String> csv = VaiUtil.readCSVFile(csvPath);
                if(!player.containsMode("competitive") || player.getMode("competitive").getMatchesPlayed() <= 3)
                    continue;
                boolean inCSV = true;
                for(String contents : csv) {
                    try {
                        inCSV = contents.contains(playerPath.getCanonicalPath());
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                    if(inCSV)
                        break;
                }
                if(inCSV)
                    continue;
                // System.out.println(inCSV);
                try {
                    VaiUtil.addToCSVFile(csvPath, csv.size() + "," + playerPath.getCanonicalPath());
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Added " + j);
                attempted++;
            }
        }
        System.out.println("Task Finished. " + attempted + "/" + total + " Succesfully added to CSV file");
    }
}


