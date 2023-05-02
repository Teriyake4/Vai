package com.teriyake.vai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class StatIterTest {
    public static void main(String[] args) {
        File file = new File("C:/ProgramData/StaVa/data/player/0ae942f3-0646-41b6-a0fb-7df05b506fd2/player.json");
        String json = fileReader(file);

        // String features = "";
        String features = "";
        String divideByMatch = "";
        String exclude = "";

        Gson gson = new Gson();
        JsonObject jsonData = gson.fromJson(json, JsonObject.class);
        jsonData = jsonData.get("modeStats").getAsJsonObject()
            .get("competitive").getAsJsonObject();
        
        Scanner input = new Scanner(System.in);
        for(String key : jsonData.keySet()) {
            System.out.print(key + ": ");
            int choice = input.nextInt();
            switch(choice) {
                case 1:
                    features += "\"" + key + "\", ";
                    break;
                case 2:
                    divideByMatch += "\"" + key + "\", ";
                    break;
                case 3:
                    exclude += "\"" + key + "\", ";
                    break;
            }
        }

        System.out.println("\nfeatures: " + features + "\n");
        System.out.println("\ndivide by match: " + divideByMatch + "\n");
        System.out.println("\nexclude: " + exclude + "\n");

    }

    public static String fileReader(File filePath) {
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
}
