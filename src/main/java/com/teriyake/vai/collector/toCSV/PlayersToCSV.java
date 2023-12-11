package com.teriyake.vai.collector.toCSV;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.threadly.concurrent.collections.ConcurrentArrayList;

import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.Player;
import com.teriyake.vai.VaiUtil;
public class PlayersToCSV {
    static int total = 0;
    static int attempted = 0;
    public static void main(String[] args) throws IOException {
        // PlayerParser parser = new PlayerParser();
        File csvPath = new File(VaiUtil.getTestDataPath(), "CSVPlayerIndex.csv");
        File dataPath = new File(System.getProperty("user.home") + "/OneDrive/Documents/StaVa/data/player");
        // String[] subPaths = dataPath.list();
        List<String> subPaths = Arrays.asList(dataPath.list());
        List<String> dataList = new ConcurrentArrayList<String>();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        for(String act : subPaths) { // act folders
        // subPaths.parallelStream().forEach(act -> {
        executor.submit(() -> {
            PlayerParser parser = new PlayerParser();
            File actPath = new File(dataPath, "/" + act);
            String[] playerPaths = actPath.list();
            for(String player : playerPaths) { //player
                total++;
                File playerPath = new File(actPath, "/" + player + "/" + "player.json");
                String json = VaiUtil.readFile(playerPath);
                try {
                    parser.setJsonString(json);
                }
                catch(NullPointerException e) {
                    e.printStackTrace();
                }
                Player data = null;
                if(json.contains("\"schema\": \"statsv2\""))// unparsed json
                    data = parser.getPlayer();
                else
                    data = parser.parsedJsonToPlayer();
                if(!data.containsMode("competitive") || data.getMode("competitive").getMatchesPlayed() <= 3)
                    continue;
                boolean inCSV = false;
                // ArrayList<String> csv = VaiUtil.readCSVFile(csvPath);
                // for(String line : csv) {
                //     if(line.contains("\\" + player + "\\player.json")) {
                //         inCSV = true;
                //         break;
                //     }
                // }
                for(String line : dataList) {
                    if(line.contains("\\" + player + "\\player.json")) {
                        inCSV = true;
                        break;
                    }
                }
                if(inCSV)
                    continue;
                String path = act + "\\" + player + "\\" + "player.json";
                dataList.add(dataList.size() + "," + path);
                // try {
                //     String path = act + "\\" + player + "\\" + "player.json";
                //     VaiUtil.addToCSVFile(csvPath, csv.size() + "," + path);
                // }
                // catch(IOException e) {
                //     e.printStackTrace();
                // }
                System.out.println("Added " + player);
                attempted++;
            }
        });
        }
        for(String data : dataList) {
            VaiUtil.addToCSVFile(csvPath, data);
        }
        System.out.println("Task Finished. " + attempted + "/" + total + " Succesfully added to CSV file");
    }
}


