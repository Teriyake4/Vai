package com.teriyake.vai.collector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.teriyake.stava.HttpStatusException;
import com.teriyake.stava.Retriever;
import com.teriyake.stava.parser.MatchParser;
import com.teriyake.stava.stats.Player;
import com.teriyake.vai.VaiUtil;

public class CollectorClass {
    private Retriever ret;
    private int numToRet;
    private File vaiPath; // filepath of list of players already retrieved
    private File stavaPath;
    private int wait;
    // private File retPath;
    private ArrayList<String> toRet;
    private ArrayList<String> matchesCollected;
    private ArrayList<String> playersCollected;
    // private ArrayList<String> attempted;

    public CollectorClass(Retriever retriever, int numRet, File path, String start, int maxWait) throws FileNotFoundException, IOException {
        ret = retriever;
        numToRet = numRet;
        vaiPath = new File(path, "Vai/");
        stavaPath = new File(path, "StaVa/data/");
        wait = maxWait;
        toRet = new ArrayList<String>();
        matchesCollected = new ArrayList<String>();
        playersCollected = new ArrayList<String>();
        if(start.equals(""))
            initToRet();
        else
            toRet.add(start);
        addAlreadyCollectedMatches();
        addAlreadyCollectedPlayers();
    }

    public void collect() throws FileNotFoundException, IOException {
        int attempted = 0;
        int numRet = 0;
        boolean isPrivate = false;
        for(int i = 0; i < toRet.size(); i++) {
            // System.out.println("i: " + i + 1 + " toRet: " + toRet.size() + " numRet: " + numRet + " numToRet: " + numToRet);
            if(i + 1 >= toRet.size() && numRet < numToRet) {
                try {
                    addToRet();
                }
                catch(HttpStatusException e) {
                    int statusCode = e.getStatusCode();
                    if(statusCode == 429 || statusCode == 403) {
                        System.out.println("Terminating collection because of HTTP error: " + statusCode);
                        i = toRet.size();
                        continue;
                    }
                }
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
                int statusCode = e.getStatusCode();
                if(statusCode == 451) {
                    System.out.println(player + " is private");
                    isPrivate = true;
                }
                else if(statusCode == 429 || statusCode == 403) {
                    System.out.println("Terminating collection because of HTTP error: " + statusCode);
                    i = toRet.size();
                    continue;
                }
                else
                    e.printStackTrace();
            }
            catch(NullPointerException e) {
                System.out.println(player + " is invalid");
            }
            addToTxt(player, isPrivate);
            if(data == null)
                continue;
            String name = data.info().getName();
            System.out.println("[" + data.info().getDate() + "] Collected: " + name + " --- Player: " + (attempted) + " - Match: " + numRet);
        }
    }

    private void addToRet() throws FileNotFoundException, IOException, HttpStatusException {
        int initSize = toRet.size();
        for(int i = toRet.size(); i > 0; i--) {
            timeBuffer(false);
            String[] toAdd = null;
            try {
                // toAdd = ret.getPlayersFromRecentMatch(toRet.get(initSize - i), "premier"); // premier match list
                System.out.println("Attempting match from " + toRet.get(initSize - i));
                toAdd = getPlayersFromValidMatch(toRet.get(initSize - i));
            }
            catch(HttpStatusException e) {
                int statusCode = e.getStatusCode();
                if(statusCode == 451)
                    continue;
                else if(statusCode == 429 || statusCode == 403) {
                    throw e;
                }
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
                if(!playersCollected.contains(player)) {
                    toRet.add(player);
                    playersCollected.add(player);
                    System.out.println("Added " + player + " to list");
                }
                else {
                    System.out.println(player + " is collected");
                }
            }
            if(initSize < toRet.size())
                return;
        }
    }

    // returns last player from already retrieved to file
    private void initToRet() throws FileNotFoundException, IOException {
        File file = new File(vaiPath, "/collection/HasRet.txt");
        ArrayList<String> output = VaiUtil.readCSVFile(file);
        for(int i = output.size() - 1; i >= 0; i--) {
            if(toRet.size() >= 1)
                break;
            System.out.println(output.get(i));
            toRet.add(output.get(i));
        }
    }

    private String[] getPlayersFromValidMatch(String playerMatch) throws HttpStatusException {
        String[] matches = ret.getRecentMatches(playerMatch, "competitive");
        timeBuffer(true);
        ArrayList<String> players = new ArrayList<String>();
        for(String matchID : matches) {
            if(matchesCollected.contains(matchID))
                continue;
            matchesCollected.add(matchID);
            String match = ret.getMatch(matchID);
            String[] tempPlayers = MatchParser.getPlayers(match);
            for(String player : tempPlayers) {
                if(!playersCollected.contains(player))
                    players.add(player);
            }
            if(players.size() > 0)
                break;
        }
        if(players.size() == 0)
            return null;
        String[] toReturn = new String[players.size()];
        for(int i = 0; i < toReturn.length; i++) {
            toReturn[i] = players.get(i);
        }
        return toReturn;
    }

    private void addAlreadyCollectedMatches() {
        File matchPath = new File(stavaPath, "match/");
        String[] actPaths = matchPath.list();
        for(String act : actPaths) { // act folders
            File actPath = new File(matchPath, "/" + act);
            String[] matchPaths = actPath.list();
            for(String match : matchPaths) { // match
                matchesCollected.add(match);
            }
        }
    }

    private void addAlreadyCollectedPlayers() {
        File playerPath = new File(stavaPath, "player/");
        String[] actPaths = playerPath.list();
        for(String act : actPaths) { // act folders
            File actPath = new File(playerPath, "/" + act);
            String[] playerPaths = actPath.list();
            for(String player : playerPaths) { // match
                playersCollected.add(player);
            }
        }
    }


    private void addToTxt(String player, boolean isPrivate) {
        String file = "/collection/HasRet.txt";
        if(isPrivate)
            file = "/collection/Private.txt";
        File path = new File(vaiPath, file);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
            writer.newLine();
            writer.write(player);
            writer.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    // private boolean isInTxt(String player) throws FileNotFoundException, IOException {
    //     String output = "";
    //     File file = new File(vaiPath, "/collection/HasRet.txt");
    //     output = VaiUtil.readFile(file);
    //     if(output.contains(player))
    //         return true;
    //     file = new File(vaiPath, "/collection/Private.txt");
    //     output = VaiUtil.readFile(file);
    //     return output.contains(player);
    // }

    private void timeBuffer(boolean fast) {
        int smallTime = 5;
        if(fast)
            smallTime = (int) (Math.random() * 5) + 1;
        else
            smallTime = (int) (Math.random() * wait) + 3; // 118
        System.out.println("Waiting " + smallTime + " sec");
        smallTime *= 1000;
        try {
            Thread.sleep(smallTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}