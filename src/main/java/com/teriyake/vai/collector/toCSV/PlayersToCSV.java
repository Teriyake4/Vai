package com.teriyake.vai.collector.toCSV;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.threadly.concurrent.collections.ConcurrentArrayList;

import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.Player;
import com.teriyake.stava.stats.player.PlayerMode;
import com.teriyake.vai.VaiUtil;
public class PlayersToCSV {
    static boolean recordAsValues = true;

    static int total = 0;
    static int attempted = 0;
    public static void main(String[] args) throws IOException {
        // PlayerParser parser = new PlayerParser();
        File csvPath = new File(VaiUtil.getTestDataPath(), "CSVPlayerIndex.csv");
        File dataPath = new File(System.getProperty("user.home") + "/OneDrive/Documents/StaVa/data/player");
        // String[] subPaths = dataPath.list();
        List<String> subPaths = Arrays.asList(dataPath.list());
        List<String> dataList = new ConcurrentArrayList<String>();
        if(recordAsValues)
            dataList.add("WR%,KAST%,ScorePerRound,DamagePerRound,KAD,RoundsWin%,KillsPerRound,HS%,EconRating,FirstBloods");
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
                    for(String line : dataList) {
                        if(line.contains("\\" + player + "\\player.json")) {
                            inCSV = true;
                            break;
                        }
                    }
                    if(inCSV)
                        continue;
                    String toLine;
                    if(recordAsValues) {
                        StringBuilder line = new StringBuilder();
                        double[] stats = new double[9];
                        PlayerMode mode = data.getMode("competitive");
                        stats[0] = mode.getKAST();
                        stats[1] = mode.getScorePerRound();
                        stats[2] = mode.getDamagePerRound();
                        stats[3] = mode.getKADRatio();
                        stats[4] = mode.getRoundsWinPct() / 100;
                        stats[5] = mode.getKillsPerRound();
                        stats[6] = mode.getHeadshotsPercentage() / 100;
                        stats[7] = mode.getEconRatingPerMatch();
                        stats[8] = mode.getFirstBloodsPerMatch(); // X FACTOR!
                        line.append(mode.getMatchesWinPct());
                        for(double stat : stats)
                            line.append(",").append(stat);
                        toLine = line.toString();
                    }
                    else
                        toLine = dataList.size() + "," + act + "\\" + player + "\\" + "player.json";
                    dataList.add(toLine);
                    attempted++;
                    if(attempted % 100 == 0)
                        System.out.println("Added " + attempted + " players");
                    // System.out.println("Added " + player);
                }
            });
        }
        executor.shutdown();
        try {
            if(!executor.awaitTermination(2000, TimeUnit.MILLISECONDS))
                executor.shutdownNow();
        }
        catch(InterruptedException e) {
            executor.shutdownNow();
        }
        while(!executor.isTerminated()) {
            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        for(String data : dataList)
            VaiUtil.addToCSVFile(csvPath, data);
        System.out.println("Task Finished. " + attempted + "/" + total + " Succesfully added to CSV file");
    }
}


