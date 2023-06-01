package com.teriyake.vai;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.teriyake.stava.HttpStatusException;
import com.teriyake.stava.Retriever;
import com.teriyake.stava.Store;
import com.teriyake.vai.collector.Collector;
import com.teriyake.vai.collector.CollectorInit;

public class DataCollector {
    // 티  个ＲЦＣＫ囗
    // sbananas#1766
    public static void main(String[] args) throws HttpStatusException {
        Scanner input = new Scanner(System.in);
        int numToRet = CollectorInit.setNumToRet(input);
        String start = CollectorInit.setPlayerToRet(input);

        System.out.println("Loading...");
        Retriever retriever = new Retriever();
        // File file = CollectorInit.initFile(input);
        String path = System.getProperty("user.home") + "/OneDrive/Documents/";
        File list = new File(path, "Vai/");
        File file = new File(path);

        Store store = new Store(file);
        file = store.getFilePath(); // changes to absolute file path of where players are stored. 
        store.setStorePatternByAct();
        store.setFileNameAsName();
        retriever.setStorage(store);
        System.out.println("Storing player data in: " + store.getFilePath());

        input.close();

        Collector collector = new Collector(retriever, numToRet, list, start);
        try {
            collector.collect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retriever.closeConnection();
        System.out.println("Task completed. Exiting.");
        System.exit(0);
    }
}
