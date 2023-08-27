package com.teriyake.vai.collector.toCSV;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.teriyake.vai.VaiUtil;

public class AllPlayersToCSV {
    // RUN WITH ENVY
    public static void main(String[] args)  throws IOException {
        File csvPath = new File(VaiUtil.getTestDataPath(), "CSVAllPlayerIndex.csv");
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
                    String path = playerPath.getCanonicalPath();
                    path = VaiUtil.changeToTeriy(path);
                    VaiUtil.addToCSVFile(csvPath, csv.size() + "," + path);
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
