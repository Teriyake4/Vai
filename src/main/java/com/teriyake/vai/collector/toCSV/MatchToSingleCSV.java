package com.teriyake.vai.collector.toCSV;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.teriyake.vai.VaiUtil;
import com.teriyake.vai.data.GameValues;

/*
 * Uses match data CSV and combines averages all player stats. 
 * Removes player agents and adds number of players that exist per team
 * Each feature are average of defense - average of attack
 * Outcome: 0 = attacker, 1 = defender
 */

public class MatchToSingleCSV {
    static File initCSVPath = new File("src\\main\\java\\com\\teriyake\\vai\\data\\MatchWinPredTrain.csv");
    static File finalCSVPath = new File("src\\main\\java\\com\\teriyake\\vai\\data\\MatchSingle.csv");
    static boolean outcomeAsName = true;
    
    public static void main(String[] args) {
        int numFeatures = MatchDataToCSV.NUM_FEATURES - GameValues.AGENT_LIST.length;

        VaiUtil.clearFile(finalCSVPath);
        List<String> initFile = VaiUtil.readCSVFile(initCSVPath);
        List<String> formated = new ArrayList<String>();
        formated.add("Outcome,NumDef,NumAtt,MatchWin%,RoundWin%,Deaths,KDRatio,AttackTraded,DefenseTraded,Traded,DamageDelta,DamagePerMatch,DamageReceivedPerMatch,EconPerMatch,KDA,Rank,Peak");
        for(int i = 0; i < initFile.size(); i++) { // iterates over each line (each match)
            // 2 previous values used for player team and existence allocated to number of players that exist per side
            int numDef = 0;
            int numAtt = 0;
            StringTokenizer initLine = new StringTokenizer(initFile.get(i), ",");
            double[] bothTeams = new double[(numFeatures - 2) * 2];
            StringBuilder line = new StringBuilder();

            if(outcomeAsName) { // outcome of match
                double outcome = Double.parseDouble(initLine.nextToken());
                if(outcome == 0) // attacker
                    line.append("0");
                else if(outcome == 1) // defender
                    line.append("1");
            }
            else {
                line.append(Double.parseDouble(initLine.nextToken()));
            }

            for(int j = 0; j < 2; j++) // skip round outcomes
                initLine.nextToken();
            
            for(int j = 0; j < 10; j++) { // iterates over each player in the line
                double side = Double.parseDouble(initLine.nextToken());
                double exists = Double.parseDouble(initLine.nextToken());
                // counts number of players that exist per team
                if(side == 0) // attacker
                    numAtt += exists;
                else if(side == 1) // defender
                    numDef += exists;
                for(int k = 0; k < numFeatures - 2; k++) { // iterates over each stat of the player
                    int spacer = 0;
                    if(j >= 5)
                        spacer += (numFeatures - 2);
                    bothTeams[k + spacer] += Double.parseDouble(initLine.nextToken());
                }
                for(int k = 0; k < GameValues.AGENT_LIST.length; k++) // skip agents
                    initLine.nextToken();
            }

            line.append(",").append(numDef);
            line.append(",").append(numAtt);
            
            for(int j = 0; j < bothTeams.length; j++) { // averages stats
                if(j >= bothTeams.length / 2) // changes to attacker stats
                    bothTeams[j] /= numAtt;
                else
                    bothTeams[j] /= numDef;
            }
            for(int j = 0; j < bothTeams.length / 2; j++) // inserts delta of bothTeams stats onto formattedLine
                line.append(",").append(bothTeams[j] - bothTeams[j + (bothTeams.length / 2)]);
            // (d / x) - (a / y)
            // (d - a) / (x + y)
            formated.add(line.toString());
        }
        for(String data : formated) {
            try {
                VaiUtil.addToCSVFile(finalCSVPath, data);
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}