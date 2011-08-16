/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.parties;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author Administrator
 */
public class Parties extends JavaPlugin {

    private static PartyManager pmanager;
    private static Server mcserv;
    private static PartyEntityListener pelistener;
    private static PartyPlayerListener pplistener;
    private boolean firstenable = true;
    private static PermissionHandler authority;
    
    public void onDisable() {
        PartyManager.serialize(pmanager, new File(this.getDataFolder(),"parties.dat"));
        pmanager.disbandAllPartys();
        pmanager = null;
        mcserv = null;
        Logger.getLogger("Minecraft").log(Level.INFO, "[Parties] Disabled!");
    }

    public void onLoad() {
        pelistener = new PartyEntityListener(this);
        pplistener = new PartyPlayerListener(this);
    }

    public void onEnable() {
        if(firstenable)
        {
            getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, pplistener, Priority.Normal, this);
            getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, pplistener, Priority.Normal, this);
            getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, pplistener, Priority.Normal, this);
            getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, pelistener, Priority.Normal, this);
            firstenable = false;
        }
        mcserv = this.getServer();
        if(!this.getDataFolder().isDirectory())
            this.getDataFolder().mkdirs();
        this.getConfiguration().load();
        Configuration config = this.getConfiguration();
        pmanager = PartyManager.deserialize(new File(this.getDataFolder(),"parties.dat"));
        if(pmanager == null)
            pmanager = new PartyManager();
        pmanager.setChatEnabled(config.getBoolean("partyChatEnabled", true));
        pmanager.setPvpEnabled(config.getBoolean("partyPvpEnabled", false));
        pmanager.setTpEnabled(config.getBoolean("partyTpEnabled", true));
        pmanager.setMaxPartySize(config.getInt("maxPartySize", 20));
        pmanager.setChatPrefix(config.getString("partyChatPrefix","[Party]"));
        pmanager.setMessagePrefix(config.getString("partyMessagePrefix","[Party Message]"));
        try
        {
            pmanager.setChatColor(ChatColor.valueOf(config.getString("partyChatColor","GREEN").toUpperCase()));
            pmanager.setChatPrefixColor(ChatColor.valueOf(config.getString("partyChatPrefixColor","DARK_GREEN").toUpperCase()));
            pmanager.setMessageColor(ChatColor.valueOf(config.getString("partyMessageColor","GREEN").toUpperCase()));
            pmanager.setMessagePrefixColor(ChatColor.valueOf(config.getString("partyMessagePrefixColor","DARK_GREEN").toUpperCase()));
            pmanager.setErrorColor(ChatColor.valueOf(config.getString("partyErrorColor","RED").toUpperCase()));
            pmanager.setSuccessColor(ChatColor.valueOf(config.getString("partySuccessColor","GREEN").toUpperCase()));
        } catch(Exception ex){
            pmanager.setChatColor(ChatColor.GREEN);
            pmanager.setChatPrefixColor(ChatColor.DARK_GREEN);
            pmanager.setMessageColor(ChatColor.GREEN);
            pmanager.setMessagePrefixColor(ChatColor.DARK_GREEN);
            pmanager.setErrorColor(ChatColor.RED);
            pmanager.setSuccessColor(ChatColor.GREEN);
        }
        Logger.getLogger("Minecraft").log(Level.INFO, "[Parties] Enabled! Version: " + this.getDescription().getVersion() + " by bekvon");
        checkPermissions();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(this.isEnabled() && sender instanceof Player)
        {
            Player player = (Player)sender;
            if(command.getName().equals("party"))
            {
                if(args.length!=0)
                {
                    args[0] = args[0].toLowerCase();
                    if(args[0].equals("join"))
                    {
                        if(args.length!=2)
                            return false;
                        pmanager.joinParty(player.getName(), args[1]);
                    }
                    else if(args[0].equals("list"))
                    {
                        StringBuilder list = new StringBuilder();
                        String[] plist;
                        if(pmanager.isPlayerInParty(player.getName()))
                        {
                            list.append("§aPlayers in your party:§2 ");
                            plist = pmanager.getPlayersPartyList(player.getName());
                        }
                        else
                        {
                            list.append("§aParties:§2 ");
                            plist = pmanager.getPartyList();
                        }
                        for(int i = 0; i < plist.length; i ++)
                        {
                            list.append(plist[i]).append(" ");
                        }
                        player.sendMessage(list.toString());
                        return true;
                    }
                    else if(args[0].equals("leave"))
                    {
                        pmanager.removePlayerFromParty(player.getName(), player.getName());
                    }
                    else if(args[0].equals("invite"))
                    {
                        if(args.length!= 2)
                            return false;
                        pmanager.sendPartyInvite(player.getName(), args[1]);
                    }
                    else if(args[0].equals("tpaccept"))
                    {
                        pmanager.tpAccept(player.getName());
                    }
                    else if(args[0].equals("tpdecline"))
                    {
                        pmanager.tpDecline(player.getName());
                    }
                    else if(args[0].equals("accept"))
                    {
                        pmanager.inviteAccept(player.getName());
                    }
                    else if(args[0].equals("decline"))
                    {
                        pmanager.inviteDecline(player.getName());
                    }
                    else if(args[0].equals("create"))
                    {
                        if(args.length<2)
                            return false;
                        PartyManager.PartyType ptype = PartyManager.PartyType.OPEN;
                        if(args.length>=3)
                        {
                            if(args[2].equals("invite"))
                            {
                                ptype = PartyManager.PartyType.INVITE;
                            }
                            else if(args[2].equals("open"))
                            {
                                ptype = PartyManager.PartyType.OPEN;
                            }
                            else if(args[2].equals("leader"))
                            {
                                ptype = PartyManager.PartyType.LEADER;
                            }
                            else
                            {
                                player.sendMessage("§cInvalid party type, must be OPEN, INVITE, or LEADER...");
                                return true;
                            }
                        }
                        pmanager.createParty(args[1], player.getName(), ptype);
                        return true;
                    }
                    else if(args[0].equals("disband"))
                    {
                        pmanager.disbandParty(player.getName());
                    }
                    else if(args[0].equals("tp"))
                    {
                        if(args.length!=2)
                            return false;
                        pmanager.tpRequest(player.getName(), args[1]);
                    }
                    else if(args[0].equals("leader"))
                    {
                        if(args.length!=2)
                            return false;
                        pmanager.changePartyLeader(player.getName(), args[1]);
                    }
                    else if(args[0].equals("rename"))
                    {
                        if(args.length!=2)
                            return false;
                        pmanager.renameParty(player.getName(), args[1]);
                    }
                    else if(args[0].equals("kick"))
                    {
                        if(args.length!=2)
                            return false;
                        pmanager.removePlayerFromParty(args[1], player.getName());
                    }
                    else if(args[0].equals("settype"))
                    {
                        if (args.length != 2) {
                            return false;
                        }
                        PartyManager.PartyType ptype;
                        if (args[1].equals("invite")) {
                            ptype = PartyManager.PartyType.INVITE;
                        } else if (args[1].equals("open")) {
                            ptype = PartyManager.PartyType.OPEN;
                        } else if (args[1].equals("leader")) {
                            ptype = PartyManager.PartyType.LEADER;
                        } else {
                            player.sendMessage("§cInvalid party type, must be OPEN, INVITE, or LEADER...");
                            return true;
                        }
                        pmanager.changePartyType(player.getName(), ptype);
                    }
                    return true;
                }
                return false;
            }
            else if(command.getName().equals("pc"))
            {
                if(args.length == 0)
                {
                    if(pmanager.partyChatEnabled(player.getName()))
                    {
                        player.sendMessage("§aToggled party chat §4OFF!");
                        pmanager.setPartyChatEnabled(player.getName(), false);
                    }
                    else
                    {
                        player.sendMessage("§aToggled party chat §2ON!");
                        pmanager.setPartyChatEnabled(player.getName(), true);
                    }
                    return true;
                }
                StringBuilder chatMessage = new StringBuilder();
                for(int i = 0; i < args.length; i++)
                    chatMessage.append(args[i]).append(" ");
                if(!pmanager.partyChatEnabled(player.getName()))
                    pmanager.chatInParty(player.getName(), chatMessage.toString());
                else
                    player.chat(chatMessage.toString());
                return true;
            }
        }
        return super.onCommand(sender, command, label, args);
    }

    public static Player getPlayer(String player)
    {
        if(mcserv != null)
            return mcserv.getPlayer(player);
        return null;
    }

    public static PartyManager getPartyManager()
    {
        return pmanager;
    }

    public static PermissionHandler getAuthorityManager()
    {
        return authority;
    }

    public static boolean hasAuthority(Player player, String permission, boolean def)
    {
        if(player.hasPermission(permission))
            return true;
        if(authority == null)
            return def;
        else
            return authority.has(player, permission);
    }

    private void checkPermissions() {
        Plugin p = getServer().getPluginManager().getPlugin("Permissions");
        if (p != null) {
            authority = ((Permissions) p).getHandler();
            Logger.getLogger("Minecraft").log(Level.INFO, "[Parties] Found Permissions Plugin!");
        } else {
            authority = null;
            Logger.getLogger("Minecraft").log(Level.INFO, "[Parties] Permissions Plugin NOT Found!");
        }
    }
}
