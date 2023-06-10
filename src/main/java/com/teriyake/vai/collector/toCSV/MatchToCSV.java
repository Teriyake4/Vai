package com.teriyake.vai.collector.toCSV;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.teriyake.vai.VaiUtil;

public class MatchToCSV {
    public static void main(String[] args) {
        File csvPath = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/CSVMatchIndex.csv");
        File dataPath = new File(System.getProperty("user.home") + "/OneDrive/Documents/StaVa/data/match");
        String[] subPaths = dataPath.list();
        int total = 0;
        int attempted = 0;
        for(String i : subPaths) { // act folders
            File subPath = new File(dataPath, "/" + i);
            String[] matchPaths = subPath.list();
            for(String j : matchPaths) { //player
                total++;
                File matchPath = new File(subPath, "/" + j + "/" + "match.json");
                ArrayList<String> csv = VaiUtil.readCSVFile(csvPath);
                boolean inCSV = true;
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
                System.out.println("Added " + j);
                attempted++;
            }
        }
        System.out.println("Task Finished. " + attempted + "/" + total + " Succesfully added to CSV file");
    }
}
