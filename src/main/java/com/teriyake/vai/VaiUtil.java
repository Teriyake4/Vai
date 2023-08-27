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
        int size = readCSVFile(csvPath).size();
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(csvPath, true))) {
            if(size > 0)
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

    public static void clearFile(File filePath) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            writer.write("");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(File filePath, String toWrite, boolean append) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
            writer.write(toWrite);
            writer.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static File getTestDataPath() throws IOException {
        // File file = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data"));
        File file = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/");
        file = file.getCanonicalFile();
        return file;
    }

    public static String changeToTeriy(String dataPath) {
        if(System.getProperty("user.home").contains("teriy"))
            return dataPath;
        else if(dataPath.contains("C:\\Users\\Ian")) {
            int userIndex = dataPath.indexOf("Ian");
            dataPath = dataPath.substring(0, userIndex) + "teriy" + dataPath.substring(userIndex + 3);
        }
        return dataPath;
    }
}
