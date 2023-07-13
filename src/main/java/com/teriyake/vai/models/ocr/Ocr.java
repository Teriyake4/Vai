package com.teriyake.vai.models.ocr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Ocr {
    private Tesseract tesseract;

    public Ocr(File tessdataPath) {
        tesseract = new Tesseract();
        try {
            tesseract.setDatapath(tessdataPath.getCanonicalPath());
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(6);
        tesseract.setOcrEngineMode(1);
    }

    public ArrayList<Map<String, String>> getPlayersAgentsFromImage(File imagePath) throws TesseractException {
        String text = tesseract.doOCR(imagePath);
        Map<String, String> team1 = new HashMap<String, String>();
        Map<String, String> team2 = new HashMap<String, String>();
        ArrayList<Map<String, String>> output = new ArrayList<Map<String, String>>();
        output.add(team1);
        output.add(team2);
        String[] lines = text.split("[\\r\\n]+");
        for(int i = 2; i < lines.length  + 1; i += 2) {
            if(i <= 10)
                team1.put(lines[i - 2], lines[i - 1]);
            else
                team2.put(lines[i - 2], lines[i - 1]);
        }
        return output;
    }

    public String getSinglePlayerNameFromImage(File imagePath) throws TesseractException {
        String text = tesseract.doOCR(imagePath);
        String[] lines = text.split("[\\r\\n]+");
        return lines[0];
    }
}
