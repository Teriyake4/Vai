package com.teriyake.vai;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.teriyake.vai.models.ocr.Ocr;

import net.sourceforge.tess4j.TesseractException;

public class MatchRecognitionTest {
    public static void main(String[] args ) {
        Ocr pAN = new Ocr(new File(System.getProperty("user.home") + "/OneDrive/Projects/Coding Projects/Java Projects/External Libraries/Tess4J/tessdata"));
        List<Map<String, String>> output = null;
        try {
            File imagePath = new File(VaiUtil.getTestDataPath().getCanonicalPath(), "/images/TestMatchCrop.png");
            output = pAN.getPlayersAgentsFromImage(imagePath);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("Teams");
        for(int i = 0; i < output.size(); i++) {
            System.out.println(i);
            Map<String, String> team = output.get(i);
            for(String name : team.keySet()) {
                System.out.println(name + " - " + team.get(name));
            }
        }

        System.out.println("\nPlayer");
        try {
            // File imagePath = new File("C:/Users/Ian/OneDrive/Pictures/Screenshots/Screenshot 2023-07-07 151125.png");
            File imagePath = new File("C:\\Users\\teriy\\OneDrive\\Pictures\\Screenshots\\Screenshot 2023-08-01 194136.png");
            String name = pAN.getSinglePlayerNameFromImage(imagePath);
            System.out.println(name);
            VaiUtil.writeFile(new File("C:\\Users\\teriy\\OneDrive\\Projects\\Coding Projects\\Java Projects\\Vai\\vai\\src\\test\\java\\com\\teriyake\\vai\\ocrTestOutput.txt"), name, false);
        }
        catch(TesseractException e) {
            e.printStackTrace();
        }
        
        // Tesseract tesseract = new Tesseract();
        // try {
        //     File imagePath = new File(VaiUtil.getTestDataPath().getCanonicalPath(), "/images/TestMatchCrop.png");
        //     tesseract.setDatapath(System.getProperty("user.home") + "/OneDrive/Projects/Coding Projects/Java Projects/External Libraries/Tess4J/tessdata");
        //     tesseract.setLanguage("eng");
        //     tesseract.setPageSegMode(6);
        //     tesseract.setOcrEngineMode(1);
        //     System.out.println("Start");
        //     String text = tesseract.doOCR(imagePath);
        //     System.out.println("End");
        //     System.out.print(text);
        // }
        // catch(Exception e) {
        //     e.printStackTrace();
        // }
    }
}
