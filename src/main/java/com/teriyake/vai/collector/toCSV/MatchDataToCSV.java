package com.teriyake.vai.collector.toCSV;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.threadly.concurrent.collections.ConcurrentArrayList;

import com.teriyake.stava.parser.MatchParser;
import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.Player;
import com.teriyake.stava.stats.player.PlayerMode;
import com.teriyake.vai.VaiUtil;
import com.teriyake.vai.data.GameValues;

public class MatchDataToCSV {
    final static String CSV = "MatchWinPredTrain.csv";
    final static boolean BALANCE = true;
    final static String MATCH_TYPE = "competitive";
    final static int NUM_FEATURES = 16 + GameValues.AGENT_LIST.length;
    // number max inclusive
    final static int MAX_NUM_IN = 10; // 10 to include all players
    // number min inclusive
    final static int MIN_NUM_IN = 7;
    // 10 max, 7 min train balance
    // 6 max, 2 min test
    // 5 max, 1 min other


    static Map<String, File> playerListAndPath;
    static File csvPath;
    static List<String> fileData;

    static int def = 0;
    static int att = 0;
    static int i = 0;
    public static void main(String[] args) throws IOException {
        fileData = new ConcurrentArrayList<String>();
        csvPath = new File(VaiUtil.getTestDataPath(), CSV);
        VaiUtil.clearFile(csvPath);
        File dataPath = new File(System.getProperty("user.home") + "/OneDrive/Documents/StaVa/data/match");
        List<String> subPaths = Arrays.asList(dataPath.list());
        addPlayerListPath();
        ArrayList<String> attemptedMatches = new ArrayList<String>();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        for(String act : subPaths) {
            executor.submit(() -> {
                PlayerParser parser = new PlayerParser();
            // subPaths.parallelStream().forEach(act -> {
                File actPath = new File(dataPath, "/" + act);
                String[] matchPaths = actPath.list();
                for(String match : matchPaths) { // match
                    double[] statsSingleLine = new double[(NUM_FEATURES * 10) + 3];
                    // first value is defense win (1) or loss (0), second and third are rounds won def wins, att wins respectivley
                    if(attemptedMatches.contains(match))
                        continue;
                    else
                        attemptedMatches.add(match);
                    String matchJson = VaiUtil.readFile(new File(actPath, "/" + match + "/" + "match.json"));
                    String result = MatchParser.getWinningTeam(matchJson);
                    if(!MatchParser.getMode(matchJson).equals(MATCH_TYPE))
                        continue;
                    if(result.equals("defender")) {
                        def++;
                        statsSingleLine[0] = 1;
                    }
                    else if(result.equals("attacker")) {
                        att++;
                        statsSingleLine[0] = 0;
                    }
                    else if(result.equals("tie"))
                        continue;
                    statsSingleLine[1] = MatchParser.getDefRoundsWon(matchJson);
                    statsSingleLine[2] = MatchParser.getAttRoundsWon(matchJson);
                    int dataIndex = 3;
                    Map<String, ArrayList<String>> playersFromMatch = MatchParser.getTeams(matchJson);
                    String[] order = {"defender", "attacker"}; // to make sure players are in proper order
                    int numPlayers = 0;
                    for(int ord = 0; ord < order.length; ord++) {
                        for(String player : playersFromMatch.get(order[ord])) {
                            // System.out.print(player + " ");
                            double[] singlePlayerData = getPlayerData(parser, player, MatchParser.getAgentOfPlayer(matchJson, player), order[ord]);
                            dataIndex = addPlayerStatsToLine(statsSingleLine, singlePlayerData, dataIndex);
                            if(singlePlayerData[1] == 1)
                                numPlayers++;
                        }
                    }
                    String toWrite = "";
                    if(numPlayers < MIN_NUM_IN || numPlayers > MAX_NUM_IN)
                        continue;
                    // System.out.println(numPlayers);
                    for(int i = 0; i < statsSingleLine.length; i++) {
                        toWrite += statsSingleLine[i] + ",";
                    }
                    toWrite = toWrite.substring(0, toWrite.length() - 1);
                    fileData.add(toWrite);
                    i++;
                    System.out.println(i + ": " + match);
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
        for(String data : fileData)
            VaiUtil.addToCSVFile(csvPath, data);
        if(BALANCE)
            balance();
    }

    public static void addPlayerListPath() {
        System.out.println("Indexing Players...");
        File playerPath = new File(System.getProperty("user.home") + "/OneDrive/Documents/StaVa/data/player/");
        String[] playerActPaths = getSortedFolders(playerPath);
        playerListAndPath = new ConcurrentHashMap<String, File>();
        for(int i = playerActPaths.length - 1; i >= 0; i--) {
            File actPath = new File(playerPath, "\\" + playerActPaths[i]);
            String[] playerPaths = actPath.list();
            for(String player : playerPaths) {
                playerListAndPath.put(player, new File(playerPath, playerActPaths[i] + "/" + player + "/player.json"));
            }
        }
        System.out.println("Done Indexing Players");
    }

    public static void balance() throws IOException {
        System.out.println("Balancing...");
        ArrayList<String> defData = new ArrayList<String>();
        ArrayList<String> attData = new ArrayList<String>();
        ArrayList<String> allData = VaiUtil.readCSVFile(csvPath);
        int def = 0;
        int att = 0;
        for(String line : allData) {
            if(line.substring(0, 1).equals("1")) {
                defData.add(line);
                def++;
            }
            else if(line.substring(0, 1).equals("0")) {
                attData.add(line);
                att++;
            }
        }
        System.out.println("Def: " + def + " Att: " + att);
        while(defData.size() != attData.size()) {
            if(defData.size() > attData.size())
                defData.remove(defData.size() - 1);
            else if(attData.size() > defData.size())
                attData.remove(attData.size() - 1);
        }
        VaiUtil.clearFile(csvPath);
        for(int i = 0; i < defData.size(); i++) {
            VaiUtil.addToCSVFile(csvPath, defData.get(i));
            VaiUtil.addToCSVFile(csvPath, attData.get(i));
        }
        System.out.println("Done Balancing");
    }

    public static double[] getPlayerData(PlayerParser parser, String name, String agent, String team) {
        if(!playerListAndPath.containsKey(name))
            return emptyPlayerData(team, agent);
        String playerJson = VaiUtil.readFile(playerListAndPath.get(name));
        Player playerData = null;
        try {
            parser.setJsonString(playerJson);
        }
        catch(NullPointerException e) {
            return emptyPlayerData(team, agent);
        }
        if(playerJson.contains("\"schema\": \"statsv2\""))// unparsed json
            playerData = parser.getPlayer();
        else
            playerData = parser.parsedJsonToPlayer();
        if(playerData == null || !playerData.containsMode(MATCH_TYPE))
            return emptyPlayerData(team, agent);
        double[] playerFeatures = new double[NUM_FEATURES];
        if(team.equals("defender"))
            playerFeatures[0] = 1;
        else if(team.equals("attacker"))
            playerFeatures[0] = 0;
        else
            System.out.println("NO TEAM: " + name);

        PlayerMode modeData = playerData.getMode(MATCH_TYPE);
        playerFeatures[1] = 1; // exists
        playerFeatures[2] = modeData.getMatchesWinPct();
        playerFeatures[3] = modeData.getRoundsWinPct();
        playerFeatures[4] = modeData.getDeathsPerMatch();
        playerFeatures[5] = modeData.getKDRatio();

        playerFeatures[6] = modeData.getAttackTraded()/modeData.getAttackRoundsPlayed();
        playerFeatures[7] = modeData.getDefenseTraded()/modeData.getDefenseRoundsPlayed();
        playerFeatures[8] = modeData.getTraded()/modeData.getRoundsPlayed();
        playerFeatures[9] = modeData.getDamageDelta();
        playerFeatures[10] = modeData.getDamagePerMatch();
        playerFeatures[11] = modeData.getDamageReceived()/modeData.getMatchesPlayed();
        playerFeatures[12] = modeData.getEconRatingPerMatch();
        playerFeatures[13] = modeData.getKDARatio();
        String rank = modeData.getRank();
        String peak = modeData.getPeakRank();
        for(int i = 0; i < GameValues.RANK_LIST.length; i++) {
            if(GameValues.RANK_LIST[i].equals(rank))
                playerFeatures[14] = i;
            if(GameValues.RANK_LIST[i].equals(peak))
                playerFeatures[15] = i;
        }

        for(int i = 0; i < playerFeatures.length; i++) {
            if(Double.isInfinite(playerFeatures[i]) || Double.isNaN(playerFeatures[i]))
                playerFeatures[i] = 0;
        }

        // playerFeatures[6] = modeData.getKillsPerMatch();
        // playerFeatures[7] = modeData.getKAST();
        // playerFeatures[8] = modeData.getAssistsPerMatch();
        // playerFeatures[9] = modeData.getScorePerRound();
        // playerFeatures[10] = modeData.getDamagePerRound();
        // playerFeatures[11] = modeData.getDefusesPerMatch();
        // playerFeatures[12] = modeData.getPlantsPerMatch();
        // playerFeatures[13] = modeData.getFirstBloodsPerMatch();

        playerFeatures = setAgent(playerFeatures, agent);

        return playerFeatures;
    }

    public static double[] emptyPlayerData(String team, String agent) {
        double[] emptyPlayerFeatures = new double[NUM_FEATURES];
        if(team.equals("defender"))
            emptyPlayerFeatures[0] = 1;
        else if(team.equals("attacker"))
            emptyPlayerFeatures[0] = 0;
        else
            System.out.println("NO TEAM: EMPTY");
        for(int i = 1; i < emptyPlayerFeatures.length; i++) {
            emptyPlayerFeatures[i] = 0;
        }
        emptyPlayerFeatures = setAgent(emptyPlayerFeatures, agent);
        
        return emptyPlayerFeatures;
    }

    public static double[] setAgent(double[] data, String agent) {
        int agentIndex = 0;
        for(int i = 0; i < GameValues.AGENT_LIST.length; i++) {
            if(agent.equals(GameValues.AGENT_LIST[i]))
                agentIndex = i;
        }
        for(int i = NUM_FEATURES - GameValues.AGENT_LIST.length; i < NUM_FEATURES; i++) {
            data[i] = 0;
        }
        data[NUM_FEATURES - GameValues.AGENT_LIST.length + agentIndex] = 1;
        return data;
    }

    public static int addPlayerStatsToLine(double[] matchData, double[] playerData, int start) {
        for(int i = start; i < playerData.length + start; i++) {
            matchData[i] = playerData[i - start];
        }
        return start += playerData.length;
    }

    public static String[] getSortedFolders(File folderPath) {
        String[] actFolders = folderPath.list();
        // String highest = actFolders[0];
        for(int i = 1; i < actFolders.length; i++) { // sort by newest
            String curFolder = actFolders[i];
            int curNum = Integer.parseInt(curFolder.substring(0, curFolder.indexOf("-"))) * 3;
            curNum += Integer.parseInt(curFolder.substring(curFolder.indexOf("-") + 1));

            int j = i - 1;
            int checkNum = Integer.parseInt(actFolders[j].substring(0, actFolders[j].indexOf("-"))) * 3;
            checkNum += Integer.parseInt(actFolders[j].substring(actFolders[j].indexOf("-") + 1));

            while(j >= 0 && checkNum < curNum) {
                actFolders[j + 1] = actFolders[j];
                j--;
                if(j >= 0) {
                    checkNum = Integer.parseInt(actFolders[j].substring(0, actFolders[j].indexOf("-"))) * 3;
                    checkNum += Integer.parseInt(actFolders[j].substring(actFolders[j].indexOf("-") + 1));
                }
            }
            actFolders[j + 1] = curFolder;
        }
        return actFolders;
    }
}
