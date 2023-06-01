package com.teriyake.vai.collector;

import java.io.File;
import java.util.Scanner;

public class CollectorInit {
    public static File initFile(Scanner input) {
        System.out.print("Store data in: 1-Cloud (Default), 2-Local: ");
        String user = input.nextLine();
        String path = "";
        if(user.equals("2"))
            path = "C:/ProgramData";
        else
            path = System.getProperty("user.home") + "/OneDrive/Documents";
        File file = new File(path);
        return file;
    }

    public static int setNumToRet(Scanner input) {
        System.out.print("Number of players to retrieve, 5-Default: ");
        String user = input.nextLine();        
        int num = 5;
        try {
            num = Integer.parseInt(user);
        }
        catch(NumberFormatException e) {
        }
        System.out.println("Retrieving " + num + " players");
        return num;
    }

    public static String setPlayerToRet(Scanner input) {
        System.out.print("Player to retrieve from, last-Default: ");
        String user = "";
        user = input.nextLine();      
        return user;
    }

    public static int setTimeLimit() {
        Scanner input = new Scanner(System.in);
        System.out.print("Set time limit, 60sec-Default: ");
        String user = input.nextLine();
        input.close();
        int num = 5;
        try {
            num = Integer.parseInt(user);
        }
        catch(NumberFormatException e) {
        }
        return num;
    }
}