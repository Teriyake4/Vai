package com.teriyake.vai.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.teriyake.stava.parser.MatchParser;
import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.Player;
import com.teriyake.vai.VaiUtil;

public class Stats {
    public static void main(String[] args) {
        ArrayList<String> allPlayers = VaiUtil.readCSVFile(new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/CSVAllPlayerIndex.csv"));
        int matchNum = 0;
        int defWin = 0;
        int attWin = 0;

        File csvPath = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/StaticMatchBalIndex.csv");
        ArrayList<String> csv = VaiUtil.readCSVFile(csvPath);
        for(int i = 0; i < csv.size(); i++) {
            String line = csv.get(i);
            int csvIndex = line.indexOf(",");
            String dataPath = line.substring(csvIndex + 1);
            // if(System.getProperty("user.home").contains("Ian")) {
            //     int userIndex = dataPath.indexOf("teriy");
            //     dataPath = dataPath.substring(0, userIndex) + "Ian" + dataPath.substring(userIndex + 5);
            // }
            File jsonPath = new File(dataPath);
            String matchJson = VaiUtil.readFile(jsonPath);
            String outcome = MatchParser.getWinningTeam(matchJson);
            String map = MatchParser.getMap(matchJson).toLowerCase();
            if(outcome.equals("defender"))
                defWin++;
            else if(outcome.equals("attacker"))
                attWin++;

            Map<String, ArrayList<String>> playersFromMatch = MatchParser.getTeams(matchJson);
            Map<String, Integer> players = new HashMap<String, Integer>();
            String[] order = {"defender", "attacker"}; // to make sure data is in proper order
            for(int ord = 0; ord < order.length; ord++) {
                for(String player : playersFromMatch.get(order[ord])) {
                    boolean put = false;
                    for(String p : allPlayers) {
                        if(p.contains(player)) {
                            players.put(player, 1);
                            put = true;
                            break;
                        }
                    }
                    if(!put) {
                        players.put(player, 0);
                    }
                }
            }

            int index = 0;
            int teamNum = 0;
            double attAvg = 0;
            double defAvg = 0;
            double stat = 0;
            for(String player : players.keySet()) {
                // System.out.println(player);
                if(players.get(player) == 0) {
                    if(index == 5) {
                        defAvg = defAvg / teamNum;
                        teamNum = 0;
                    }
                    else if(index == 9) {
                        attAvg = attAvg / teamNum;
                    }
                    index++;
                    continue;
                }
                File playerPath = null;
                File playerDataPath = new File(System.getProperty("user.home") + "/OneDrive/Documents/StaVa/data/player/");
                String[] playerActPaths = playerDataPath.list();
                for(String act : playerActPaths) {
                    playerPath = new File(playerDataPath, act + "/" + player + "/player.json");
                    if(playerPath.exists())
                        break;
                }
                if(playerPath == null) {
                    if(index == 5) {
                        defAvg = defAvg / teamNum;
                        teamNum = 0;
                    }
                    else if(index == 9) {
                        attAvg = attAvg / teamNum;
                    }
                    index++;
                    continue;
                }
                String json = VaiUtil.readFile(playerPath);
                Player playerStats = null;
                if(json.contains("\"schema\": \"statsv2\"")) // unparsed json
                    playerStats = PlayerParser.getPlayer(json);
                else
                    playerStats = PlayerParser.parsedJsonToPlayer(json);
                    
                boolean contains = playerStats.containsMode("competitive");
                // boolean contains = playerStats.containsMap(map);
                if(!contains|| playerStats == null) {
                    if(index == 5) {
                        defAvg = defAvg / teamNum;
                        teamNum = 0;
                    }
                    else if(index == 9) {
                        attAvg = attAvg / teamNum;
                    }
                    index++;
                    continue;
                }
                // .getClutches() / playerStats.getMode("competitive").getMatchesPlayed()
                stat = playerStats.getMode("competitive").getDefenseRoundsWinPct(); // STAT
                // stat = playerStats.getMap(map).getFirstBloodsPerMatch();
                if(index < 5) {
                    defAvg += stat;
                    teamNum++;
                }
                if(index == 5) {
                    defAvg = defAvg / teamNum;
                    teamNum = 0;
                }

                if(index > 5) {
                    attAvg += stat;
                    teamNum++;
                }
                if(index == 9) {
                    attAvg = attAvg / teamNum;
                }
                index++;
            }
            String end = "";
            if(outcome.equals("attacker") && attAvg > defAvg) {
                end = "Att MATCH";
            }
            else if(outcome.equals("defender") && defAvg > attAvg) {
                end = "Def MATCH";
            }
            else {
                end = outcome;
            }
            // end = outcome.substring(0, 1).toUpperCase() + outcome.substring(1, 3);
            // System.out.println(outcome + " def: " + defAvg + "% att: " + attAvg + "% " + end);
            System.out.println(defAvg + ", " + attAvg);
            // System.out.println(end);
            if(end.contains("MATCH"))
                matchNum++;
        }
        System.out.println(matchNum + "/" + csv.size());
        System.out.println("def wins: " + defWin + " att wins: " + attWin);
    }
}
