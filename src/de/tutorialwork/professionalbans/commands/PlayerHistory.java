package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.BanManager;
import de.tutorialwork.professionalbans.utils.LogManager;
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
                    p.sendMessage(Main.Prefix+"/history <§eSpieler§7>");
                } else {
                    String UUID = UUIDFetcher.getUUID(args[0]);
                    if(UUID != null && BanManager.playerExists(UUID)){
                        p.sendMessage("§8[]===================================[]");
                        p.sendMessage("§e§l"+BanManager.getNameByUUID(UUID));
                        ArrayList ids = LogManager.getLog(UUID);
                        for (int i = 0; i < LogManager.getLog(UUID).size(); i++){
                            int logid = Integer.parseInt(ids.get(i).toString());
                            String action = LogManager.getLogAction(logid);
                            String teamname = BanManager.getNameByUUID(LogManager.getLogTeam(logid));
                            String note = LogManager.getLogNote(logid);

                            if(teamname == null){
                                teamname = "KONSOLE";
                            }
                            if(action.equals("BAN") || action.equals("UNBAN_BAN")
                            || action.equals("MUTE") || action.equals("UNBAN_MUTE")
                            || action.equals("AUTOMUTE_BLACKLIST") || action.equals("AUTOMUTE_ADBLACKLIST")){
                                switch (action){
                                    case "BAN":
                                        p.sendMessage("§7Wurde von §e"+teamname+" §7gebannt wegen §e§l"+BanManager.getReasonByID(Integer.parseInt(note)));
                                        break;
                                    case "UNBAN_BAN":
                                        p.sendMessage("§7Wurde von §e"+teamname+" §7entbannt");
                                        break;
                                    case "MUTE":
                                        p.sendMessage("§7Wurde von §e"+teamname+" §7gemutet wegen §e§l"+BanManager.getReasonByID(Integer.parseInt(note)));
                                        break;
                                    case "UNBAN_MUTE":
                                        p.sendMessage("§7Wurde von §e"+teamname+" §7entmutet");
                                        break;
                                    case "AUTOMUTE_BLACKLIST":
                                        p.sendMessage("§7Wurde wegen seinem Verhalten von dem §cSystem §7gemutet ("
                                                + LogManager.getLogNote(logid) +
                                                ")");
                                    case "AUTOMUTE_ADBLACKLIST":
                                        p.sendMessage("§7Wurde wegen Werbung von dem §cSystem §7gemutet ("
                                                + LogManager.getLogNote(logid) +
                                                ")");
                                }
                            }
                        }
                        p.sendMessage("§8[]===================================[]");
                    } else {
                        p.sendMessage(Main.Prefix+"§cZu diesem Spieler ist kein Verlauf verfügbar");
                    }
                }
            } else {
                p.sendMessage(Main.NoPerms);
            }
        } else {

        }
    }
}
