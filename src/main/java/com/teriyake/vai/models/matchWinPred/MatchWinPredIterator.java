package com.teriyake.vai.models.matchWinPred;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import com.teriyake.stava.parser.MatchParser;
import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.Player;
import com.teriyake.stava.stats.player.PlayerMode;
import com.teriyake.vai.VaiUtil;

public class MatchWinPredIterator implements DataSetIterator {
    private static File playerCsvPath = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/CSVPlayerIndex.csv");
    private File matchCsvPath;
    private int csvCursor;
    private int csvLength;
    private int batch;
    private static final String[] rankToNum = {"unrated", 
        "iron 1", "iron 2", "iron 3", 
        "bronze 1", "bronze 2", "bronze 3", 
        "silver 1", "silver 2", "silver 3", 
        "gold 1", "gold 2", "gold 3", 
        "platinum 1", "platinum 2", "platinum 3", 
        "diamond 1", "diamond 2", "diamond 3", 
        "ascendant 1", "ascendant 2", "ascendant 3", 
        "immortal 1", "immortal 2", "immortal 3", 
        "radiant"};
    private ArrayList<String> matchCsv;
    private ArrayList<String> allPlayers;

    public MatchWinPredIterator(File matchCsvP, int batch) throws IOException {
        this.batch = batch;
        csvCursor = 0;
        String line = "";
        matchCsvPath = matchCsvP;
        // BufferedReader csvReader = new BufferedReader(new FileReader(matchCsvPath));
        matchCsv = VaiUtil.readCSVFile(matchCsvPath);
        allPlayers = VaiUtil.readCSVFile(new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/CSVAllPlayerIndex.csv"));
    }

    @Override
    public DataSet next(int num) {
        int def = 0;
        int att = 0;
        double[][] featureList = new double[num][4 * (10)]; // player stats
        // exists, RoundsWinPct
        // , KAST, RoundsWinPct, map MatchsWinPct, map MatchsWinPct exists
        double[][] labelList = new double[num][2]; // outcome
        // index 0 = defending win, index 1 = attacking win
        for(int i = 0; i < num; i++) {
            String line = matchCsv.get(csvCursor);
            csvCursor++;
            int csvIndex = line.indexOf(",");
            String dataPath = line.substring(csvIndex + 1);
            // if(System.getProperty("user.home").contains("teriy")) {
            //     int userIndex = dataPath.indexOf("Ian");
            //     dataPath = dataPath.substring(0, userIndex) + "teriy" + dataPath.substring(userIndex + 3);
            // }
            // if(System.getProperty("user.home").contains("Ian")) {
            //     int userIndex = dataPath.indexOf("teriy");
            //     dataPath = dataPath.substring(0, userIndex) + "Ian" + dataPath.substring(userIndex + 5);
            // }
            File jsonPath = new File(dataPath);
            String matchJson = VaiUtil.readFile(jsonPath);
            String outcome = MatchParser.getWinningTeam(matchJson);
            String map = MatchParser.getMap(matchJson);
            // System.out.println(outcome);
            if(outcome.equals("defender")) {
                def++;
                labelList[i][0] = 1;
                labelList[i][1] = 0;
            }
            else if(outcome.equals("attacker")) {
                att++;
                labelList[i][0] = 0;
                labelList[i][1] = 1;
            }
            else {
                System.out.println("TIE");
                continue;
            }

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
            for(String player : players.keySet()) { // iterates through players of match
                if(players.get(player) == 0) {
                    featureList = addEmptyToDS(featureList, i, index);
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
                    featureList = addEmptyToDS(featureList, i, index);
                    index++;
                    continue;
                }
                String json = VaiUtil.readFile(playerPath);
                if(!PlayerParser.parsedJsonToPlayer(json).containsMode("competitive")) {
                    index++;
                    continue;
                }
                Player playerData = PlayerParser.parsedJsonToPlayer(json);

                featureList[i][index] = 1;
                // if(playerData.containsMap(map)) {
                //     featureList[i][index + 1] = playerData.getMap(map).getMatchesWinPct() / 100;
                //     featureList[i][index + 2] = 1;
                // }
                // else {
                //     featureList[i][index + 1] = 0;
                //     featureList[i][index + 2] = 0;
                // }
                featureList[i][index + 1] = playerData.getMode("competitive").getRoundsWinPct() / 100;
                featureList[i][index + 2] = playerData.getMode("competitive").getMatchesWinPct() / 100;
                featureList[i][index + 3] = playerData.getMode("competitive").getKAST() / 100;
                // featureList[i][index + 5] = playerData.getMode("competitive").getRoundsWinPct() / 100;
                // featureList[i][index + 2] = playerData.getAttackRoundsWinPct() / 100;
            }
        }
        INDArray features = Nd4j.create(featureList);
        INDArray labels = Nd4j.create(labelList);
        DataSet ds = new DataSet(features, labels);
        System.out.println("def: " + def + " - att: " + att);
        return ds;
    }

    private double[][] addEmptyToDS(double[][] dataSet, int iValue, int index) {
        dataSet[iValue][index] = 0;
        dataSet[iValue][index + 1] = 0;
        dataSet[iValue][index + 2] = 0;
        dataSet[iValue][index + 3] = 0;
        // dataSet[iValue][index + 4] = 0;
        return dataSet;
    }

    @Override
    public DataSet next() {
        return next(batch);
    }
    
    @Override
    public int inputColumns() {
        // return toFeature.length + featureByMatch.length;
        return 1;
    }
        
    @Override
    public int totalOutcomes() {
        return 2;
    }
    
    @Override
    public boolean resetSupported() {
        return true;
    }
    
    @Override
    public boolean asyncSupported() {
        return false;
    }
    
    @Override
    public void reset() {        
        csvCursor = 0;
    }
    
    @Override
    public int batch() {
        return batch;
    }
    
    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        return;
    }
    
    @Override
    public DataSetPreProcessor getPreProcessor() {
        return null;
    }
    
    @Override
    public List<String> getLabels() {
        return null;
    }

    public boolean hasNext() {
        return csvCursor < csvLength;
    }

    public int totalExamples() {
        return csvLength;
    }

    public int numExamples() {
        return csvCursor;
    }

    public void close() {
        return;
    }
    
}
