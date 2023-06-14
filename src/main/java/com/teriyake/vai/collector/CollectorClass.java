package com.teriyake.vai.collector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.teriyake.stava.HttpStatusException;
import com.teriyake.stava.Retriever;
import com.teriyake.stava.stats.Player;
import com.teriyake.vai.VaiUtil;

public class CollectorClass {
    private Retriever ret;
    private int numToRet;
    private File filePath; // filepath of list of players already retrieved
    // private File retPath;
    private ArrayList<String> toRet;
    // private ArrayList<String> attempted;

    public CollectorClass(Retriever retriever, int numRet, File path, String start) throws FileNotFoundException, IOException {
        ret = retriever;
        numToRet = numRet;
        filePath = path;
        toRet = new ArrayList<String>();
        if(start.equals(""))
            initToRet();
        else
            toRet.add(start);

    }

    public void collect() throws FileNotFoundException, IOException {
        int attempted = 0;
        int numRet = 0;
        boolean isPrivate = false;
        for(int i = 0; i < toRet.size(); i++) {
            // System.out.println("i: " + i + 1 + " toRet: " + toRet.size() + " numRet: " + numRet + " numToRet: " + numToRet);
            if(i + 1 >= toRet.size() && numRet < numToRet) {
                addToRet();
                numRet++;
            }
            timeBuffer(isPrivate);
            isPrivate = false;
            String player = toRet.get(i);
            Player data = null;
            try {
                data = ret.getPlayer(player);
                attempted++;
            }
            catch(HttpStatusException e) {
                if(e.getStatusCode() == 451) {
                    System.out.println(player + " is private");
                    isPrivate = true;
                }
                else
                    e.printStackTrace();
            }
            addToTxt(player, isPrivate);
            if(data == null)
                continue;
            String name = data.info().getName();
            System.out.println("[" + data.info().getDate() + "] Collected: " + name + " --- Player: " + (attempted) + " - Match: " + numRet);
        }
    }

    private void addToRet() throws FileNotFoundException, IOException {
        int initSize = toRet.size();
        for(int i = toRet.size(); i > 0; i--) {
            timeBuffer(false);
            String[] toAdd = null;
            try {
                toAdd = ret.getPlayersFromRecentMatch(toRet.get(initSize - i));
            }
            catch(HttpStatusException e) {
                if(e.getStatusCode() == 451)
                    continue;
                else
                    e.printStackTrace();
            }
            if(toAdd == null) {
                System.out.println("Null toAdd");
                continue;
            }
            if(toRet.size() == 1) {
                toRet.remove(0);
                if(toAdd.length == 0)
                    return;
            }
            for(String player : toAdd) {
                boolean contains = false;
                for(String check : toRet) {
                    contains = player.equals(check);
                    if(contains)
                        break;
                }
                if(!isInTxt(player)) {
                    toRet.add(player);
                    System.out.println("Added " + player + " to list");
                }
            }
            if(initSize < toRet.size())
                return;
        }
    }

    // returns last player from already retrieved to file
    private void initToRet() throws FileNotFoundException, IOException {
        File file = new File(filePath, "/HasRet.txt");
        ArrayList<String> output = VaiUtil.readCSVFile(file);
        for(int i = output.size() - 1; i >= 0; i--) {
            if(toRet.size() >= 1)
                break;
            System.out.println(output.get(i));
            toRet.add(output.get(i));
        }
    }

    private void addToTxt(String player, boolean isPrivate) {
        String file = "/HasRet.txt";
        if(isPrivate)
            file = "/Private.txt";
        File path = new File(filePath, file);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
            writer.newLine();
            writer.write(player);
            writer.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isInTxt(String player) throws FileNotFoundException, IOException {
        String output = "";
        File file = new File(filePath, "/HasRet.txt");
        output = VaiUtil.readFile(file);
        if(output.contains(player))
            return true;
        file = new File(filePath, "/Private.txt");
        output = VaiUtil.readFile(file);
        return output.contains(player);
    }

    private void timeBuffer(boolean isPrivate) {
        int smallTime = 5;
        if(isPrivate)
            smallTime = (int) (Math.random() * 5) + 1;
        else
            smallTime = (int) (Math.random() * 118) + 3;
        System.out.println("Waiting " + smallTime + " sec");
        smallTime *= 1000;
        try {
            Thread.sleep(smallTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}