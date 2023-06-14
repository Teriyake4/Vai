package com.teriyake.vai.collector.toCSV;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.teriyake.stava.parser.MatchParser;
import com.teriyake.vai.VaiUtil;

public class MatchToCSV {
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
        for(String i : subPaths) { // act folders
            File subPath = new File(dataPath, "/" + i);
            String[] matchPaths = subPath.list();
            for(String j : matchPaths) { // match
                total++;
                File matchPath = new File(subPath, "/" + j + "/" + "match.json");
                String json = VaiUtil.readFile(matchPath);

                


                // checking number of players per match are stored
                int defP = 0;
                int attP = 0;
                Map<String, ArrayList<String>> players = MatchParser.getTeams(json);
                ArrayList<String> hasRet = VaiUtil.readCSVFile(new File(System.getProperty("user.home") + "/OneDrive/Documents/Vai/HasRet.txt"));
                for(String team : players.keySet()) {
                    for(String player : players.get(team)) {
                        for(String p : hasRet) { // act folders
                            // System.out.println(playerP);
                            boolean exists = p.equals(player);
                            if(exists && team.equals("defender"))
                                defP++;
                            else if(exists && team.equals("attacker"))
                                attP++;
                        }
                    }
                }
                System.out.println("defender: " + defP + " attacker: " + attP);
                if(defP <= 2 || attP <= 2)
                    continue;


                if(MatchParser.getWinningTeam(json).equals("tie"))
                    continue;
                ArrayList<String> csv = VaiUtil.readCSVFile(csvPath);
                boolean inCSV = false;
                for(String contents : csv) {
                    try {
                        inCSV = contents.contains(matchPath.getCanonicalPath());
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                    if(inCSV)
                        break;
                }
                if(inCSV)
                    continue;
                try {
                    VaiUtil.addToCSVFile(csvPath, csv.size() + "," + matchPath.getCanonicalPath());
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
                // System.out.println("Added " + j);
                attempted++;
            }
        }
        System.out.println("Task Finished. " + attempted + "/" + total + " Succesfully added to CSV file");
        System.out.println("def: " + def + " att: " + att + " - " + (((double) (def)) / att));
    }
}
