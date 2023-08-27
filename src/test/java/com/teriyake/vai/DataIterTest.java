package com.teriyake.vai;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.teriyake.stava.parser.PlayerParser;
import com.teriyake.stava.stats.Player;

public class DataIterTest {
    public static void main(String[] args) throws IOException {
        // File pathsFilePath = new File(VaiUtil.getTestDataPath(), "thing.txt");
        // ArrayList<String> paths = VaiUtil.readCSVFile(pathsFilePath);
        // for(String path : paths) {
        //     File dataPath = new File(path);
        //     String data = VaiUtil.readFile(dataPath);
        //     if(!data.substring(0, 1).equals("\"") && !data.substring(data.length() - 1, data.length()).equals("\""))
        //         continue;
        //     data = data.substring(1, data.length() - 1);
        //     data = data.replaceAll(Matcher.quoteReplacement("\\"), "");
        //     Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
        //     data = gson.toJson(gson.fromJson(data, Object.class));
        //     // System.out.println(data);
        //     VaiUtil.writeFile(dataPath, data, false);
        //     System.out.println(path);
        // }
        int asdf = 8;
        System.out.println(asdf);
        add(asdf);
        System.out.println(asdf);
    }
    public static void add(int a) {
        a += 10;
    }
}