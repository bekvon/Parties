/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.parties;

import java.io.Serializable;
import java.util.ArrayList;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class PlayerParty implements Serializable {
    private int partyLeader;
    private ArrayList<String> joinedPlayers;
    private String partyName;

    public PlayerParty(String name, String partyCreator)
    {
        partyName = name;
        joinedPlayers = new ArrayList<String>();
        joinedPlayers.add(partyCreator);
        partyLeader = 0;
    }

    public synchronized void addToParty(String player)
    {
        if(player == null)
            return;
        if(!isInParty(player))
        {
            joinedPlayers.add(player);
            if(joinedPlayers.size()==1)
                setPartyLeader(player);
            sendPartyMessage(player + " has joined the party.");
        }
    }

    public synchronized void removeFromParty(String playerName)
    {
        for(int i = 0; i < joinedPlayers.size(); i ++)
        {
            String thisPlayer = joinedPlayers.get(i);
            if(thisPlayer.equals(playerName))
            {
                joinedPlayers.remove(i);
                Player player = Parties.getPlayer(playerName);
                if(player != null && player.isOnline())
                    player.sendMessage(Parties.getPartyManager().getMessageColor()+"You are no longer in a party.");
                sendPartyMessage(thisPlayer + " has left the party.");
                if(i == partyLeader && !joinedPlayers.isEmpty())
                {
                    setPartyLeader(joinedPlayers.get(0));
                }
                return;
            }
        }
    }

    public synchronized boolean isInParty(String playerName)
    {
        for(int i = 0; i < joinedPlayers.size(); i++)
        {
            String thisPlayer = joinedPlayers.get(i);
            if(thisPlayer.equals(playerName))
                return true;
        }
        return false;
    }

    public synchronized boolean isPartyLeader(String playerName)
    {
        if(joinedPlayers.isEmpty())
            return false;
        return joinedPlayers.get(partyLeader).equals(playerName);
    }

    public synchronized String getPartyLeader()
    {
        if(joinedPlayers.isEmpty())
            return null;
        return joinedPlayers.get(partyLeader);
    }

    public synchronized int getPartyCount()
    {
        return joinedPlayers.size();
    }

    public synchronized String getPartyName()
    {
        return partyName;
    }

    public synchronized void setPartyName(String newname)
    {
        sendPartyMessage("Party is being renamed to " + newname);
        partyName = newname;
    }

    public synchronized void sendPartyChat(String message, String srcPlayer)
    {
        PartyManager pmanager = Parties.getPartyManager();
        for(int i = 0; i < joinedPlayers.size(); i ++)
        {
            Player player = Parties.getPlayer(joinedPlayers.get(i));
            if(player!= null && player.isOnline())
                player.sendMessage(pmanager.getChatPrefixColor() + pmanager.getChatPrefix() + " " + srcPlayer + ": " + pmanager.getChatColor() + message);
        }
    }
    
    public synchronized void sendPartyMessage(String message) {
        PartyManager pmanager = Parties.getPartyManager();
        for (int i = 0; i < joinedPlayers.size(); i++) {
            Player player = Parties.getPlayer(joinedPlayers.get(i));
            if(player != null && player.isOnline())
                player.sendMessage(pmanager.getMessagePrefixColor() +pmanager.getMessagePrefix()+" " + pmanager.getMessageColor() + message);
        }
    }

    public synchronized void removeAllFromParty()
    {
        sendPartyMessage("Party is being disbanded...");
        joinedPlayers.clear();
        partyLeader = 0;
    }

    public synchronized void setPartyLeader(String playerName)
    {
        for(int i = 0; i < joinedPlayers.size(); i++)
        {
            String player = joinedPlayers.get(i);
            if(player.equals(playerName))
            {
                partyLeader = i;
                sendPartyMessage(player + " is now party leader.");
                return;
            }
        }
    }

    public synchronized void partyTP(String player1, String player2)
    {
        Player p1 = Parties.getPlayer(player1);
        Player p2 = Parties.getPlayer(player2);
        if(p1!=null && p2!=null)
        {
            if(p1.isOnline() && p2.isOnline())
            {
                p1.teleport(p2.getLocation());
            }
        }
    }

    private synchronized String getPlayerFromName(String name) {
        for (int i = 0; i < joinedPlayers.size(); i++) {
            String player = joinedPlayers.get(i);
            if(player.equals(name))
                return player;
        }
        return null;
    }
    
    public String[] getPlayerList()
    {
        String[] list = new String[joinedPlayers.size()];
        for(int i = 0; i < joinedPlayers.size(); i ++)
        {
            list[i] = joinedPlayers.get(i);
        }
        return list;
    }
}
