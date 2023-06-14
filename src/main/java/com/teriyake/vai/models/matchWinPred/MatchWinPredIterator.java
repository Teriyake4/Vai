package com.teriyake.vai.models.matchWinPred;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import com.teriyake.vai.VaiUtil;

public class MatchWinPredIterator implements DataSetIterator {
    private static File playerPath = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/CSVPlayerIndex.csv");
    private static File matchPath = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/CSVMatchIndex.csv");
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

    public MatchWinPredIterator(int batch, int csvStart) throws IOException {
        BufferedReader csvReader = new BufferedReader(new FileReader(matchPath));
        this.batch = batch;
        csvCursor = csvStart;
        String line = "";
        matchCsv = VaiUtil.readCSVFile(matchPath);
    }

    @Override
    public DataSet next(int num) {
        double[][] featureList = new double[num][3 * (10)]; // player stats
        double[][] labelList = new double[num][2]; // outcome
        for(int i = 0; i < num; i++) {
            String line = matchCsv.get(csvCursor);
            csvCursor++;
            int csvIndex = line.indexOf(",");
            String dataPath = line.substring(csvIndex + 1);
            if(System.getProperty("user.home").contains("teriy")) {
                int userIndex = dataPath.indexOf("Ian");
                dataPath = dataPath.substring(0, userIndex) + "teriy" + dataPath.substring(userIndex + 3);
            }
            File jsonPath = new File(dataPath);
            String matchJson = VaiUtil.readFile(jsonPath);
        }
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
