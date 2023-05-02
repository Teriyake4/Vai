package com.teriyake.vai.collector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

    public Collector(Retriever retriever, int numRet, File path) {
        ret = retriever;
        numToRet = numRet;
        filePath = path;
        // retPath = ret.getStorage().getFilePath();
        toRet = new ArrayList<String>();
    }

    public void collect() throws FileNotFoundException, IOException {
        initToRet();
        for(int i = 0; i < numToRet; i++) {
            if(i >= toRet.size())
                addToRet();
            timeBuffer(false);
            String player = toRet.get(i);
            Player data = null;
            try {
                data = ret.getPlayer(player);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            if(data == null) {
                toRet.remove(i);
                i--;
            }
            else {
                String name = data.info().getName();
                addToTxt(player);
                System.out.println("[" + data.info().getDate() + "] Collected " + (i + 1) + ": " + name);
            }
        }
    }

    private boolean isPublic(String player) throws HttpStatusException {
        timeBuffer(true);
        String[] results = ret.getNonPrivateSearch(player);
        for(String i : results) {
            if(player.equals(i))
                return true;
        }
        return false;
    }

    private void addToRet() throws FileNotFoundException, IOException {
        int initSize = toRet.size();
        int i = 1;
        while(initSize >= toRet.size()) {
            timeBuffer(false);
            String[] toAdd = null;
            if(toRet.size() == 1) {
                try {
                    toAdd = ret.getPlayersFromRecentMatch(toRet.get(0));
                }
                catch(HttpStatusException e) {
                }
                toRet.remove(0);
                if(toAdd == null)
                    return;
            }
            else
                toAdd = ret.getPlayersFromRecentMatch(toRet.get(initSize - i));
            for(String player : toAdd) {
                if(toRet.size() >= numToRet)
                    break;
                if(isPublic(player) && !isInTxt(player)) {
                    toRet.add(player);
                    System.out.println("Added " + player + " to list");
                }
            }
            if(toRet.size() >= numToRet || toRet.size() == 0)
                break;
            else if(initSize == toRet.size() && i < toRet.size() )
                i++;
        }

    }

    // returns last player from already retrieved to file
    private void initToRet() throws FileNotFoundException, IOException {
        ArrayList<String> output = VaiUtil.readCSVFile(filePath);
        for(int i = output.size() - 1; i >= 0; i--) {
            if(toRet.size() >= 1)
                break;
            System.out.println(output.get(i));
            toRet.add(output.get(i));
            addToRet();
        }
    }

    private void addToTxt(String player) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
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
        return output.contains(player);
    }

    private void timeBuffer(boolean isSearch) {
        int smallTime = 5;
        if(isSearch)
            smallTime = (int) (Math.random() * 7) + 1;
        else
            smallTime = (int) (Math.random() * 58) + 3;
        
        System.out.println("Waiting " + smallTime + " sec");
        smallTime *= 1000;
        try {
            Thread.sleep(smallTime);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
    }
}