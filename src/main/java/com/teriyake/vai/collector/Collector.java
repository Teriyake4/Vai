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

public class Collector {
    private Retriever ret;
    private int numToRet;
    private File filePath; // filepath of list of players already retrieved
    // private File retPath;
    private ArrayList<String> toRet;
    private String start;
    // private ArrayList<String> attempted;

    public Collector(Retriever retriever, int numRet, File path, String toStart) {
        ret = retriever;
        numToRet = numRet;
        filePath = path;
        // retPath = ret.getStorage().getFilePath();
        toRet = new ArrayList<String>();
        // attempted = new ArrayList<String>();
        start = toStart;
    }

    public void collect() throws FileNotFoundException, IOException {
        if(start == "")
            initToRet();
        else
            toRet.add(start);
        for(int i = 0; i < toRet.size(); i++) {
            if(i >= toRet.size() - 1 && i < numToRet) {
                System.out.println("Adding Players");
                addToRet();
            }
            timeBuffer(false);
            String player = toRet.get(i);
            Player data = null;
            boolean isPrivate = false;
            try {
                data = ret.getPlayer(player);
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
            if(data == null) {
                toRet.remove(i);
                i--;
                continue;
            }

            String name = data.info().getName();
            System.out.println("[" + data.info().getDate() + "] Collected: " + name + " --- " + (i + 1) + "/" + numToRet);
        }
    }

    private void addToRet() throws FileNotFoundException, IOException {
        int initSize = toRet.size();
        int i = 1;
        while(initSize >= toRet.size()) {
            timeBuffer(false);
            String[] toAdd = null;
            toAdd = ret.getPlayersFromRecentMatch(toRet.get(initSize - i));
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
            if(initSize == toRet.size() && i < toRet.size())
                i++;
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
            addToRet();
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

    private void timeBuffer(boolean isSearch) {
        int smallTime = 5;
        if(isSearch)
            smallTime = (int) (Math.random() * 7) + 1;
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