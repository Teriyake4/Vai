package com.teriyake.vai.models.matchWinPred;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import com.teriyake.stava.parser.MatchParser;
import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.Player;
import com.teriyake.stava.stats.player.PlayerMap;
import com.teriyake.stava.stats.player.PlayerMode;
import com.teriyake.vai.VaiUtil;

public class MatchWinPredIterator implements DataSetIterator {
    private static File playerCsvPath;
    private File matchCsvPath;
    private int csvCursor;
    private int csvLength;
    private int batch;
    private int featPerPlay;
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

    public MatchWinPredIterator(File matchCsvP, int batch, int featPerPlay) throws IOException {
        this.playerCsvPath = new File(VaiUtil.getTestDataPath(), "CSVPlayerIndex.csv");
        this.batch = batch;
        this.featPerPlay = featPerPlay;
        csvCursor = 0;
        String line = "";
        matchCsvPath = matchCsvP;
        // BufferedReader csvReader = new BufferedReader(new FileReader(matchCsvPath));
        matchCsv = VaiUtil.readCSVFile(matchCsvPath);
        allPlayers = VaiUtil.readCSVFile(new File(VaiUtil.getTestDataPath(), "CSVAllPlayerIndex.csv"));
    }

    @Override
    public DataSet next(int num) {
        int def = 0;
        int att = 0;
        double[][] featureList = new double[num][featPerPlay * (10)]; // player stats
        double[][] labelList = new double[num][2]; // outcome
        // index 0 = defending win, index 1 = attacking win
        for(int i = 0; i < num; i++) {
            StringTokenizer line = new StringTokenizer(matchCsv.get(csvCursor), ",");
            csvCursor++;
            double outcome = Double.parseDouble(line.nextToken());
            if(outcome == 1) { // defender win
                def++;
                labelList[i][0] = 1;
                labelList[i][1] = 0;
            }
            else if(outcome == 0) { // attacker win
                att++;
                labelList[i][0] = 0;
                labelList[i][1] = 1;
            }
            else {
                System.out.println("TIE");
                continue;
            }
            for(int j = 0; j < (featPerPlay * 10); j++) {
                featureList[i][j] = Double.parseDouble(line.nextToken());
            }
        }
        featureList = normalizeMinMax(featureList);

        INDArray features = Nd4j.create(featureList);
        INDArray labels = Nd4j.create(labelList);
        DataSet ds = new DataSet(features, labels);
        System.out.println("def: " + def + " - att: " + att);
        return ds;
    }

    private double[][] normalizeMinMax(double[][] dataSet) {
        for(int c = 0; c < featPerPlay; c++) {
            double max = Integer.MIN_VALUE;
            double min = Integer.MAX_VALUE;
            if(checkToSkip(c))
                continue;
            for(int r = 0; r < dataSet.length; r++) {
                for(int subC = 0; subC < 10; subC++) {
                    int col = c + (subC * featPerPlay);
                    if(max < dataSet[r][col])
                        max = dataSet[r][col];
                    else if(min > dataSet[r][col])
                        min = dataSet[r][col];
                }
            }
            if(c == 2 || c == 3 || c == 4) {
                max = 100;
                min = 0;
            }
            System.out.println(min + ":" + max);
            for(int r = 0; r < dataSet.length; r++) {
                for(int subC = 0; subC < 10; subC++) {
                    int col = c  + (subC * featPerPlay);
                    dataSet[r][col] = (dataSet[r][col] - min) / (max - min);
                }
            }
        }
        return dataSet;
    }

    private boolean checkToSkip(int c) {
        int[] statsToSkip = {0, 1, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22 , 23, 24 , 25, 26 , 27, 28 , 29, 30 , 31, 32 , 33, 34 , 35 , 36};
        for(int num : statsToSkip) {
            if(c == num) // features to skip
                return true;
        }
        return false;
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
