package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.LogManager;
import de.tutorialwork.professionalbans.utils.TimeManager;
import de.tutorialwork.professionalbans.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import java.util.ArrayList;

public class PlayerHistory extends Command {
    public PlayerHistory(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(p.hasPermission("professionalbans.history") || p.hasPermission("professionalbans.*")){
                if(args.length == 0){
                    p.sendMessage(Main.data.Prefix+"/history <§e"+Main.messages.getString("player")+"§7>");
                } else {
                    String UUID = UUIDFetcher.getUUID(args[0]);
                    if(UUID != null && Main.ban.playerExists(UUID)){
                        p.sendMessage("§8[]=================§8[§e§l "+Main.ban.getNameByUUID(UUID)+" §8]=================[]");
                        ArrayList ids = LogManager.getLog(UUID);
                        for (int i = 0; i < LogManager.getLog(UUID).size(); i++){
                            int logid = Integer.parseInt(ids.get(i).toString());
                            String action = LogManager.getLogAction(logid);
                            String teamname = Main.ban.getNameByUUID(LogManager.getLogTeam(logid));
                            String note = LogManager.getLogNote(logid);
                            String date = " §8- §e"+ TimeManager.formatDate(Long.valueOf(LogManager.getLogDate(logid)));

                            String log_prefix = "§8» ";

                            if(teamname == null){
                                teamname = "KONSOLE";
                            }
                            if(action.equals("BAN") || action.equals("UNBAN_BAN")
                            || action.equals("MUTE") || action.equals("UNBAN_MUTE")
                            || action.equals("AUTOMUTE_BLACKLIST") || action.equals("AUTOMUTE_ADBLACKLIST")){
                                switch (action){
                                    case "BAN":
                                        p.sendMessage(log_prefix+Main.messages.getString("event_ban").replace("%note%", teamname)+Main.ban.getReasonByID(Integer.parseInt(note))+date);
                                        break;
                                    case "UNBAN_BAN":
                                        p.sendMessage(log_prefix+Main.messages.getString("event_unban").replace("%note%", teamname)+date);
                                        break;
                                    case "MUTE":
                                        p.sendMessage(log_prefix+Main.messages.getString("event_mute").replace("%note%", teamname)+Main.ban.getReasonByID(Integer.parseInt(note))+date);
                                        break;
                                    case "UNBAN_MUTE":
                                        p.sendMessage(log_prefix+Main.messages.getString("event_unmute").replace("%note%", teamname)+date);
                                        break;
                                    case "AUTOMUTE_BLACKLIST":
                                        p.sendMessage(log_prefix+Main.messages.getString("event_automute_blacklist").replace("%note%", teamname)
                                                + LogManager.getLogNote(logid) +
                                                ")"+date);
                                    case "AUTOMUTE_ADBLACKLIST":
                                        p.sendMessage(log_prefix+Main.messages.getString("event_automute_adblacklist").replace("%note%", teamname)
                                                + LogManager.getLogNote(logid) +
                                                ")"+date);
                                }
                            }
                        }
                        String spaces = "";
                        for(int i = 0; i < Main.ban.getNameByUUID(UUID).length() + 4; i++){
                            spaces = spaces + "=";
                        }
                        p.sendMessage("§8[]=================================="+spaces+"[]");
                    } else {
                        p.sendMessage(Main.data.Prefix+Main.messages.getString("no_history"));
                    }
                }
            } else {
                p.sendMessage(Main.data.NoPerms);
            }
        } else {

        }
    }
}
