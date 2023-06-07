package com.teriyake.vai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class VaiUtil {
    public static ArrayList<String> readCSVFile(File filePath) {
        ArrayList<String> output = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while((line = reader.readLine()) != null)
                output.add(line);
            reader.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static void addToCSVFile(File csvPath, String data) throws IOException {
        int size = readCSVFile(csvPath).size() - 1;
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(csvPath, true))) {
            writer.newLine();
            writer.write(data);
            writer.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFile(File filePath) {
        String output = "";
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder("");
            String line;
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
}
