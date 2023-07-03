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
        for(String act : subPaths) { // act folders
            File actPath = new File(dataPath, "/" + act);
            String[] playerPaths = actPath.list();
            for(String player : playerPaths) { //player
                total++;
                File playerPath = new File(actPath, "/" + player + "/" + "player.json");
                String json = VaiUtil.readFile(playerPath);
                Player data = PlayerParser.parsedJsonToPlayer(json);
                if(!data.containsMode("competitive") || data.getMode("competitive").getMatchesPlayed() <= 3)
                    continue;
                boolean inCSV = false;
                ArrayList<String> csv = VaiUtil.readCSVFile(csvPath);
                for(String line : csv) {
                    if(line.contains("\\" + player + "\\player.json")) {
                        inCSV = true;
                        break;
                    }
                }
                if(inCSV)
                    continue;
                try {
                    VaiUtil.addToCSVFile(csvPath, csv.size() + "," + playerPath.getCanonicalPath());
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Added " + player);
                attempted++;
            }
        }
        System.out.println("Task Finished. " + attempted + "/" + total + " Succesfully added to CSV file");
    }
}


