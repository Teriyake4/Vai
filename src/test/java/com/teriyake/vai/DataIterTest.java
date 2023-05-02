package com.teriyake.vai;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.Player;

public class DataIterTest {
    public static void main(String[] args) throws IOException {
        File file = new File("C:/Users/Ian/OneDrive/Projects/Coding Projects/Java Projects/Vai/vai/src/main/java/com/teriyake/vai/winPredFromComp/CSVPlayerIndex.csv");
        ArrayList<String> data = VaiUtil.readCSVFile(file);
        ArrayList<Player> players = new ArrayList<Player>();

        for(int i = 0; i < data.size(); i++) {
            String line = data.get(i);
            int csvIndex = line.indexOf(",");
            String dataPath = line.substring(csvIndex + 1);
            File jsonPath = new File(dataPath);
            String json = VaiUtil.readFile(jsonPath);
            players.add(PlayerParser.parsedJsonToPlayer(json));
        }
        File test = new File("C:/Users/Ian/OneDrive/Projects/Coding Projects/Java Projects/Vai/vai/src/main/java/com/teriyake/vai/test.csv");
        for(int i = 0; i < players.size(); i++) {
            String stats = "";
            // stats += players.get(i).getMode("competitive").getMatchesWinPct() + ",";
            stats += players.get(i).getMode("competitive").getKADRatio();
            // stats += players.get(i).getMode("competitive").getScorePerMatch() + ",";
            // stats += players.get(i).getMode("competitive").getScorePerRound() + ",";
            // stats += players.get(i).getMode("competitive").getKillsPerRound();
            VaiUtil.addToCSVFile(test, stats);
        }
    }
}
