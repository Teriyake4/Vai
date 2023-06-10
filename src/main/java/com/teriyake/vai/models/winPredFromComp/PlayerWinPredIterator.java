package com.teriyake.vai.models.winPredFromComp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.player.PlayerMode;

public class PlayerWinPredIterator implements DataSetIterator{
    private BufferedReader csvReader;
    private static File csvPath = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/CSVPlayerIndex.csv");
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
    private String[] toLabel;
    private String[] toFeature;
    private String[] featureByMatch;
    private ArrayList<String> csv = new ArrayList<String>();
    // private String[] toExclude;

    public PlayerWinPredIterator(int batch, int csvStart) throws IOException {
        csvReader = new BufferedReader(new FileReader(csvPath));
        this.batch = batch;
        csvCursor = csvStart;
        // 96 features
        String[] labels = {"matchWinPct"}; // roundsWinPct, attackRoundsWinPct, defenseRoundsWinPct
        String[] features = {"rank", "peakRank", "matchesPlayed", "matchesDuration", "roundsDuration", "scorePerMatch", "scorePerRound", "killsPerRound", "killsPerMatch", "deathsPerRound", "deathsPerMatch", "assistsPerRound", "assistsPerMatch", "kDRatio", "kDARatio", "kADRatio", "damageDeltaPerRound", "damagePerRound", "damagePerMatch", "damagePerMinute", "headshotsPerRound", "headshotsPercentage", "grenadeCastsPerRound", "grenadeCastsPerMatch", "ability1CastsPerRound", "ability1CastsPerMatch", "ability2CastsPerRound", "ability2CastsPerMatch", "ultimateCastsPerRound", "ultimateCastsPerMatch", "econRatingPerMatch", "econRatingPerRound", "firstBloodsPerMatch", "kAST", "mostKillsInMatch", "plantsPerMatch", "plantsPerRound", "attackKDRatio", "attackKAST", "defusesPerMatch", "defusesPerRound", "defenseKDRatio", "defenseKAST"};
        String[] byMatch = {"roundsPlayed", "roundsWon", "roundsLost", "dealtHeadshots", "dealtBodyshots", "dealtLegshots", "receivedHeadshots", "receivedBodyshots", "receivedLegshots", "suicides", "firstDeaths", "lastDeaths", "survived", "traded", "flawless", "thrifty", "aces", "teamAces", "clutches", "clutchesLost", "clutches1v1", "clutches1v2", "clutches1v3", "clutches1v4", "clutches1v5", "clutchesLost1v1", "clutchesLost1v2", "clutchesLost1v3", "clutchesLost1v4", "clutchesLost1v5", "kills1K", "kills2K", "kills3K", "kills4K", "kills5K", "kills6K", "plants", "attackAssists", "attackScore", "attackDamage", "attackHeadshots", "attackTraded", "attackSurvived", "attackFirstBloods", "attackFirstDeaths", "defenseAssists", "defenseScore", "defenseDamage", "defenseHeadshots", "defenseTraded", "defenseSurvived", "defenseFirstBloods", "defenseFirstDeaths"};
        String[] exclude = {"info", "matchesWon", "matchesLost", "matchesTied", "matchesWinPct", "timePlayed", "score", "kills", "deaths", "assists", "damage", "damageDelta", "damageReceived", "headshots", "grenadeCasts", "ability1Casts", "ability2Casts", "ultimateCasts", "econRating", "firstBloods", "attackKills", "attackDeaths", "attackRoundsWon", "attackRoundsLost", "defuses", "defenseKills", "defenseDeaths", "defenseRoundsWon", "defenseRoundsLost", "roundsWinPct", "attackRoundsWinPct", "defenseRoundsWinPct"};
        
        toFeature = features;
        toLabel = labels;
        featureByMatch = byMatch;
        // toExclude = exclude;
        String line = "";
        while ((line = csvReader.readLine()) != null) {
            csvLength++;
            csv.add(line);
        }
    }
    @Override
    public DataSet next(int num){
        double[][] featureList = new double[num][9]; // player stats
        double[][] labelList = new double[num][1]; // winrates
        for(int i = 0; i < num; i++) {
            String line = csv.get(csvCursor);
            csvCursor++;
            int csvIndex = line.indexOf(",");
            String dataPath = line.substring(csvIndex + 1);
            if(System.getProperty("user.home").contains("teriy")) {
                int userIndex = dataPath.indexOf("Ian");
                dataPath = dataPath.substring(0, userIndex) + "teriy" + dataPath.substring(userIndex + 3);
            }
            File jsonPath = new File(dataPath);
            String json = fileReader(jsonPath);
            PlayerMode player = PlayerParser.parsedJsonToPlayer(json).getMode("competitive");
            labelList[i][0] = player.getMatchesWinPct() / 100;
            featureList[i][0] = player.getKAST() / 100;
            featureList[i][1] = player.getScorePerRound();// / 1000; MAX: 1500
            featureList[i][2] = player.getDamagePerRound();// / 1000; MAX: 750
            featureList[i][3] = player.getKADRatio();// / 2;
            featureList[i][4] = player.getRoundsWinPct() / 100;
            featureList[i][5] = player.getKillsPerRound();// / 2; MAX: 6, maybe 7?
            featureList[i][6] = player.getHeadshotsPercentage() / 100;
            featureList[i][7] = player.getEconRatingPerMatch();// / 100;
            featureList[i][8] = player.getRoundsDuration(); // MAX: 140 sec
        }

        for(int c = 0; c < featureList[0].length; c++) {
            double max = Integer.MIN_VALUE;
            double min = Integer.MAX_VALUE;
            if(c == 0 || c == 4 || c == 6)
                continue;
            for(int r = 0; r < featureList.length; r++) {
                if(max < featureList[r][c])
                   max = featureList[r][c];
                else if(min > featureList[r][c])
                    min = featureList[r][c];
            }
            if(c == 1) {
                max = 1500;
                min = 0;
            }
            else if(c == 2) {
                max = 750;
                min = 0;
            }
            // else if(c == 5) { // produces worse results?
            //     max = 6;
            //     min = 0;
            // }
            else if(c == 8) {
                max = 140;
                min = 0;
            }
            else if(c == 3 || c == 7) {
                min = 0;
            }
            for(int r = 0; r < featureList.length; r++) {
                featureList[r][c] = (featureList[r][c] - min) / (max - min);
            }
            System.out.println(min + ":" + max);
        }

        INDArray features = Nd4j.create(featureList);
        INDArray labels = Nd4j.create(labelList);
        DataSet ds = new DataSet(features, labels);
        return ds;
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
        return toLabel.length;
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
        try {            
            if (csvReader != null)
                csvReader.close();
            csvReader = new BufferedReader(new FileReader(csvPath));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
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

    private String fileReader(File filePath) {
        String output = "";
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder("");
            String line;
             // Holds true until there is nothing to read
            while((line = reader.readLine()) != null) {
                sb.append(line);
            }
            output = sb.toString();
            reader.close();
        }
        catch(Exception e) {
            e.printStackTrace();
            return output;
        }
        return output;
    }

    public void close() {
        try {
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
