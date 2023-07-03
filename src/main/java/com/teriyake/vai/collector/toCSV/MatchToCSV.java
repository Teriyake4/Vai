package com.teriyake.vai.collector.toCSV;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.teriyake.stava.parser.MatchParser;
import com.teriyake.vai.VaiUtil;

public class MatchToCSV {
    static boolean add = true;
    static boolean balanced = false;
    public static void main(String[] args) {
        int def = 0;
        int att = 0;
        File csvPath = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/CSVMatchIndex.csv");
        File dataPath = new File(System.getProperty("user.home") + "/OneDrive/Documents/StaVa/data/match");
        String[] subPaths = dataPath.list();
        File playerPath = new File(System.getProperty("user.home") + "/OneDrive/Documents/StaVa/data/player");
        String[] playerActPaths = playerPath.list();
        int total = 0;
        int attempted = 0;
        for(String act : subPaths) { // act folders
            File actPath = new File(dataPath, "/" + act);
            String[] matchPaths = actPath.list();
            for(String match : matchPaths) { // match
                total++;
                File matchPath = new File(actPath, "/" + match + "/" + "match.json");
                String json = VaiUtil.readFile(matchPath);
                // System.out.println(MatchParser.getMode(json));
                if(!MatchParser.getMode(json).equals("competitive"))
                    continue;
                String result = MatchParser.getWinningTeam(json);
                if(result.equals("defender"))
                    def++;
                else if(result.equals("attacker"))
                    att++;
                else if(MatchParser.getWinningTeam(json).equals("tie"))
                    continue;
                
                String csv = VaiUtil.readFile(csvPath);
                if(csv.contains(match)) {
                    System.out.println("Already in file: " + match);
                    continue;
                }
                String bal = VaiUtil.readFile(new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/CSVBalanced.csv"));
                if(bal.contains(match)) {
                    System.out.println("Already in bal: " + match);
                    continue;
                }

                // checking number of players per match are stored
                int defP = 0;
                int attP = 0;
                Map<String, ArrayList<String>> players = MatchParser.getTeams(json);
                for(String team : players.keySet()) { // both teams
                    for(String p : players.get(team)) { // team
                        for(String playerAct : playerActPaths) { // act folders of all players
                            File playerActPath = new File(playerPath, "/" + playerAct);
                            String[] playerList = playerActPath.list(); // list of player names within act
                            for(String player : playerList) { // player
                                boolean exists = p.equals(player);
                                if(exists && team.equals("defender")) {
                                    defP++;
                                    break;
                                }
                                else if(exists && team.equals("attacker")) {
                                    attP++;
                                    break;
                                }
                            }
                        }
                    }
                }
                if(balanced && (defP <= 3 || attP <= 3) || (defP + attP < 8)) // bal
                    continue;
                else if((defP <= 2 || attP <= 2) || (defP + attP < 5))
                    continue;
                ArrayList<String> csvArray = VaiUtil.readCSVFile(csvPath);
                if(add) {
                    try {
                        VaiUtil.addToCSVFile(csvPath, csvArray.size() + "," + matchPath.getCanonicalPath());
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("Added " + match);
                attempted++;
            }
        }
        if(balanced)
            balance(csvPath);
        System.out.println("Task Finished. " + attempted + "/" + total + " Succesfully added to CSV file");
        System.out.println("def: " + def + " att: " + att + " - Defense WR%: " + (((double) (def) * 100.0) / total));
    }

    public static void balance(File csvPath) {
        ArrayList<String> csv = VaiUtil.readCSVFile(csvPath);
        int def = 0;
        int att = 0;
        Map<String, Integer> matchO = new ConcurrentHashMap<String, Integer>();
        for(String line : csv) {
            String path = line.substring(line.indexOf(",") + 1);
            String json = VaiUtil.readFile(new File(path));
            String result = MatchParser.getWinningTeam(json);
            if(result.equals("defender")) {
                def++;
                matchO.put(path, 0);
            }
            else if(result.equals("attacker")) {
                att++;
                matchO.put(path, 1);
            }
        }
        VaiUtil.clearFile(csvPath);
        for(String path : matchO.keySet()) {
            if(def == att)
                break;
            if(def >= att && matchO.get(path) == 0) {
                matchO.remove(path);
                def--;
            }
            else if(att >= def && matchO.get(path) == 1) {
                matchO.remove(path);
                att--;
            }
        }
        int i = 0;
        for(String path : matchO.keySet()) {
            if(add) {
                try {
                    VaiUtil.addToCSVFile(csvPath, i + "," + path);
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
           }
           i++;
        }
    }
}
