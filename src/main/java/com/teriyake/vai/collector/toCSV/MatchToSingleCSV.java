package com.teriyake.vai.collector.toCSV;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.teriyake.vai.VaiUtil;
import com.teriyake.vai.data.GameValues;

/*
 * Uses match data CSV and combines averages all player stats. 
 * Removes player agents and adds number of players that exist per team
 */

public class MatchToSingleCSV {
    public static void main(String[] args) {
        File initCSVPath = new File("src\\main\\java\\com\\teriyake\\vai\\data\\MatchWinPredTrain.csv");
        List<String> initFile = VaiUtil.readCSVFile(initCSVPath);
        List<String> formated = new ArrayList<String>();
        for(int i = 0; i < initFile.size(); i++) { // iterates over each line (each match)
            // 2 previous values used for player team and existence allocated to number of players that exist per side
            int numDef = 0;
            int numAtt = 0;
            StringTokenizer initLine = new StringTokenizer(initFile.get(i), ",");
            double[] formatedLine = new double[MatchDataToCSV.NUM_FEATURES - (GameValues.AGENT_LIST.length)];
            
            formatedLine[0] = Double.parseDouble(initLine.nextToken()); // outcome of match
            // for()
        }
    }
}
