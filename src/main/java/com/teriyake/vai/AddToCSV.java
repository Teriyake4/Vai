package com.teriyake.vai;

import java.io.File;
import java.io.IOException;

import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.Player;
import com.teriyake.vai.collector.AddDataSet;

// ONLY RUN WITH ONE DEVICE ie: ENVY
public class AddToCSV {
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
                // System.out.println(player + ": " + player.exists());
                String json = VaiUtil.readFile(playerPath);
                Player player = PlayerParser.parsedJsonToPlayer(json);
                if(!player.containsMode("competitive") || player.getMode("competitive").getMatchesPlayed() <= 3)
                    continue;
                boolean added = false;
                try {
                    added = AddDataSet.addToCSV(playerPath, csvPath);
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
                if(added) {
                    System.out.println("Added " + j);
                    attempted++;
                }
            }
        }
        System.out.println("Task Finished. " + attempted + "/" + total + " Succesfully added to CSV file");
    }
}
