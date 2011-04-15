/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.parties;

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Administrator
 */
public class PartyPlayerListener extends PlayerListener {

    private Parties parent;

    public PartyPlayerListener(Parties plugIn)
    {
        parent = plugIn;
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        if(event.isCancelled() || !parent.isEnabled())
            return;
        String pname = event.getPlayer().getName();
        if(Parties.getPartyManager().partyChatEnabled(pname) && Parties.getPartyManager().isPlayerInParty(pname))
        {
            Parties.getPartyManager().chatInParty(pname, event.getMessage());
            event.setCancelled(true);
            return;
        }
        super.onPlayerChat(event);
    }

    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        if(!parent.isEnabled())
            return;
        String pname = event.getPlayer().getName();
        if (Parties.getPartyManager().partyChatEnabled(pname) && Parties.getPartyManager().isPlayerInParty(pname)) {
            Parties.getPartyManager().sendPartyMessage(Parties.getPartyManager().getPlayersPartyName(pname),"Party Member " + pname+" has logged in.");
            return;
        }
        super.onPlayerLogin(event);
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(!parent.isEnabled())
            return;
        String pname = event.getPlayer().getName();
        if (Parties.getPartyManager().partyChatEnabled(pname) && Parties.getPartyManager().isPlayerInParty(pname)) {
            Parties.getPartyManager().sendPartyMessage(Parties.getPartyManager().getPlayersPartyName(pname), "Party Member " + pname+" has logged out.");
            return;
        }
        super.onPlayerQuit(event);
    }
}
