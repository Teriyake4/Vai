package com.teriyake.vai.collector;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.teriyake.stava.HttpStatusException;
import com.teriyake.stava.Retriever;
import com.teriyake.stava.Store;

public class Collector {
    // 티  个ＲЦＣＫ囗
    public static void main(String[] args) throws HttpStatusException {
        Scanner input = new Scanner(System.in);
        int numToRet = setNumToRet(input);
        String start = setPlayerToRet(input);
        int maxWait = setSpeed(input);

        System.out.println("Loading...");
        Retriever retriever = new Retriever();
        // File file = CollectorInit.initFile(input);
        String path = System.getProperty("user.home") + "/OneDrive/Documents/";
        // File list = new File(path, "Vai/");
        File file = new File(path);

        Store store = new Store(file);
        store.setStorePatternByAct();
        store.setFileNameAsName();
        retriever.setStorage(store);
        System.out.println("Storing player data in: " + store.getFilePath());
        input.close();

        try {
            CollectorClass collector = new CollectorClass(retriever, numToRet, file, start, maxWait);
            collector.collect();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        retriever.closeConnection();
        System.out.println("Task completed. Exiting.");
        System.exit(0);
    }

    public static int setNumToRet(Scanner input) {
        System.out.print("Number of matches to retrieve, 1-Default: ");
        String user = input.nextLine();        
        int num = 1;
        try {
            num = Integer.parseInt(user);
        }
        catch(NumberFormatException e) {
        }
        if(num == 1)
            System.out.println("Retrieving " + num + " match");
        else
            System.out.println("Retrieving " + num + " matchs");
        return num;
    }

    public static String setPlayerToRet(Scanner input) {
        System.out.print("Player to retrieve from, last-Default: ");
        String user = "";
        user = input.nextLine();      
        return user;
    }

    public static int setSpeed(Scanner input) {
        System.out.print("Set retrieval speed 1-2, 1 (Slow)-Default: ");
        String user = input.nextLine();        
        int num = 1;
        try {
            num = Integer.parseInt(user);
        }
        catch(NumberFormatException e) {
        }
        if(num == 1) {
            System.out.println("Slow Retrieval");
            num = 218;
        }
        else {
            System.out.println("Fast Retrieval");
            num = 18;
        }
        return num;
    }
}
