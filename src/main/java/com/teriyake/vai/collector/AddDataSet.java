package com.teriyake.vai.collector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.Player;
import com.teriyake.vai.VaiUtil;

public class AddDataSet {

    public static boolean isElegible(Player player, File playerPath, ArrayList<String> csv) throws IOException {
        for(String i : csv) {
            if(i.contains(playerPath.getCanonicalPath()))
                return false;
        }
        return player.containsMode("competitive");
    }

    public static boolean addToCSV(File playerPath, File csvPath) throws IOException {
        ArrayList<String> csv = VaiUtil.readCSVFile(csvPath);
        String data = VaiUtil.readFile(playerPath);
        Player player = PlayerParser.parsedJsonToPlayer(data);
        if(!isElegible(player, playerPath, csv))
            return false;
        // System.out.println(csv.size());
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(csvPath, true))) {
            writer.write((csv.size()) + "," + playerPath.getCanonicalPath());
            writer.newLine();
            writer.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }


}
