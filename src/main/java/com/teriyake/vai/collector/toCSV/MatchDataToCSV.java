package com.teriyake.vai.collector.toCSV;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.teriyake.stava.parser.MatchParser;
import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.Player;
import com.teriyake.stava.stats.player.PlayerMode;
import com.teriyake.vai.VaiUtil;

public class MatchDataToCSV {
    final static String[] AGENTS = {
        "brimstone", "phoenix", "sage", "sova", "viper", "cypher", "reyna", 
        "killjoy", "breach", "omen", "jett", "raze", "skye", "yoru", "astra", 
        "kay/o", "chamber", "neon", "fade", "harbor", "gecko", "deadlock"
    };

    final static String CSV = "MatchWinPredTrain.csv";
    final static boolean BALANCE = true;
    final static String MATCH_TYPE = "premier";
    final static int NUM_FEATURES = 14 + AGENTS.length;
    final static int NUM_MISSING = 9;

    static Map<String, File> playerListAndPath;
    static File csvPath;
    public static void main(String[] args) throws IOException {
        int def = 0;
        int att = 0;
        csvPath = new File(VaiUtil.getTestDataPath(), CSV);
        VaiUtil.clearFile(csvPath);
        File dataPath = new File(System.getProperty("user.home") + "/OneDrive/Documents/StaVa/data/match");
        String[] subPaths = dataPath.list();
        addPlayerListPath();
        ArrayList<String> attemptedMatches = new ArrayList<String>();
        for(String act : subPaths) {
            File actPath = new File(dataPath, "/" + act);
            String[] matchPaths = actPath.list();
            for(String match : matchPaths) { // match
                double[] statsSingleLine = new double[(NUM_FEATURES * 10) + 1];
                // first value is defense win (1) or loss (0)
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
                int dataIndex = 1;
                Map<String, ArrayList<String>> playersFromMatch = MatchParser.getTeams(matchJson);
                String[] order = {"defender", "attacker"}; // to make sure players are in proper order
                int numPlayers = 0;
                for(int ord = 0; ord < order.length; ord++) {
                    for(String player : playersFromMatch.get(order[ord])) {
                        double[] singlePlayerData = getPlayerData(player, MatchParser.getAgentOfPlayer(matchJson, player), order[ord]);
                        dataIndex = addPlayerStatsToLine(statsSingleLine, singlePlayerData, dataIndex);
                        if(singlePlayerData[1] == 1)
                            numPlayers++;
                    }
                }
                String toWrite = "";
                if(numPlayers <= NUM_MISSING)
                    continue;
                for(int i = 0; i < statsSingleLine.length; i++) {
                    toWrite += statsSingleLine[i] + ",";
                }
                toWrite = toWrite.substring(0, toWrite.length() - 1);
                VaiUtil.addToCSVFile(csvPath, toWrite);
            }
        }
        if(BALANCE)
            balance();
        System.out.println("defP: " + def + " attP: " + att);
    }

    public static void addPlayerListPath() {
        System.out.println("Indexing Players...");
        File playerPath = new File(System.getProperty("user.home") + "/OneDrive/Documents/StaVa/data/player/");
        String[] playerActPaths = getSortedFolders(playerPath);
        playerListAndPath = new HashMap<String, File>();
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
        for(String line : allData) {
            if(line.substring(0, 1).equals("1"))
                defData.add(line);
            else if(line.substring(0, 1).equals("0"))
                attData.add(line);
        }
        while(defData.size() != attData.size()) {
            if(defData.size() > attData.size())
                defData.remove(defData.size() - 1);
            else if(attData.size() > attData.size())
                attData.remove(attData.size() - 1);
        }
        VaiUtil.clearFile(csvPath);
        for(int i = 0; i < defData.size(); i++) {
            VaiUtil.addToCSVFile(csvPath, defData.get(i));
            VaiUtil.addToCSVFile(csvPath, attData.get(i));
        }
        System.out.println("Done Balancing");
    }

    public static double[] getPlayerData(String name, String agent, String team) {
        if(!playerListAndPath.containsKey(name))
            return emptyPlayerData(team, agent);
        String playerJson = VaiUtil.readFile(playerListAndPath.get(name));
        Player playerData = null;
        if(playerJson.contains("\"schema\": \"statsv2\"")) // unparsed json
            playerData = PlayerParser.getPlayer(playerJson);
        else
            playerData = PlayerParser.parsedJsonToPlayer(playerJson);
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
        playerFeatures[4] = modeData.getKAST();
        playerFeatures[5] = modeData.getAssistsPerMatch();
        playerFeatures[6] = modeData.getKillsPerMatch();
        playerFeatures[7] = modeData.getDeathsPerMatch();
        playerFeatures[8] = modeData.getKDRatio();
        playerFeatures[9] = modeData.getScorePerRound();
        playerFeatures[10] = modeData.getDamagePerRound();
        playerFeatures[11] = modeData.getDefusesPerMatch();
        playerFeatures[12] = modeData.getPlantsPerMatch();
        playerFeatures[13] = modeData.getFirstBloodsPerMatch();

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
        for(int i = 0; i < AGENTS.length; i++) {
            if(agent.equals(AGENTS[i]))
                agentIndex = i;
        }
        for(int i = 14; i < NUM_FEATURES; i++) {
            data[i] = 0;
        }
        data[14 + agentIndex] = 1;
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
