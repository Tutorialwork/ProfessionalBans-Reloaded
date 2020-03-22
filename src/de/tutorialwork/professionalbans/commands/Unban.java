package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.LogManager;
import de.tutorialwork.professionalbans.utils.UUIDFetcher;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Unban extends Command {
    public Unban(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(p.hasPermission("professionalbans.unban") || p.hasPermission("professionalbans.*")){
                if(args.length == 0){
                    p.sendMessage(Main.data.Prefix+"/unban <"+Main.messages.getString("player")+"/IP>");
                } else {
                    String UUID = UUIDFetcher.getUUID(args[0]);
                    if(IPBan.validate(args[0])){
                        Main.ip.unban(args[0]);
                        Main.ban.sendNotify("UNBANIP", args[0], p.getName(), null);
                        p.sendMessage(Main.data.Prefix+Main.messages.getString("unban_ip").replace("%ip%", args[0]));
                        LogManager.createEntry(null, p.getUniqueId().toString(), "UNBAN_IP", args[0]);
                    } else {
                        if(Main.ban.playerExists(UUID)){
                            if(Main.ip.isBanned(Main.ip.getIPFromPlayer(UUID))){
                                Main.ip.unban(Main.ip.getIPFromPlayer(UUID));
                                p.sendMessage(Main.data.Prefix+Main.messages.getString("unban_ban_ip").replace("%player%", Main.ip.getIPFromPlayer(UUID)));
                            }
                            if(Main.ban.isBanned(UUID)){
                                Main.ban.unban(UUID);
                                Main.ban.sendNotify("UNBAN", Main.ban.getNameByUUID(UUID), p.getName(), "null");
                                p.sendMessage(Main.data.Prefix+"§e§l"+Main.ban.getNameByUUID(UUID)+" "+Main.messages.getString("unban_ban"));
                                LogManager.createEntry(UUID, p.getUniqueId().toString(), "UNBAN_BAN", null);
                            } else if(Main.ban.isMuted(UUID)) {
                                Main.ban.unmute(UUID);
                                Main.ban.sendNotify("UNMUTE", Main.ban.getNameByUUID(UUID), p.getName(), "null");
                                p.sendMessage(Main.data.Prefix+"§e§l"+Main.ban.getNameByUUID(UUID)+" "+Main.messages.getString("unban_mute"));
                                LogManager.createEntry(UUID, p.getUniqueId().toString(), "UNBAN_MUTE", null);
                            } else {
                                p.sendMessage(Main.data.Prefix+"§e§l"+Main.ban.getNameByUUID(UUID)+" "+Main.messages.getString("unban_not_banned"));
                            }
                        } else {
                            p.sendMessage(Main.data.Prefix+Main.messages.getString("player_404"));
                        }
                    }
                }
            } else {
                p.sendMessage(Main.data.NoPerms);
            }
        } else {
            if(args.length == 0){
                BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+"/unban <"+Main.messages.getString("player")+"/IP>");
            } else {
                String UUID = UUIDFetcher.getUUID(args[0]);
                if(IPBan.validate(args[0])){
                    Main.ip.unban(args[0]);
                    Main.ban.sendNotify("UNBANIP", args[0], "KONSOLE", null);
                    BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("unban_ip").replace("%ip%", args[0]));
                    LogManager.createEntry(null, "KONSOLE", "UNBAN_IP", args[0]);
                } else {
                    if(Main.ban.playerExists(UUID)){
                        if(Main.ip.isBanned(Main.ip.getIPFromPlayer(UUID))){
                            Main.ip.unban(Main.ip.getIPFromPlayer(UUID));
                            BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("unban_ban_ip").replace("%player%", Main.ip.getIPFromPlayer(UUID)));
                        }
                        if(Main.ban.isBanned(UUID)){
                            Main.ban.unban(UUID);
                            Main.ban.sendNotify("UNBAN", Main.ban.getNameByUUID(UUID), "KONSOLE", "null");
                            BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+"§e§l"+Main.ban.getNameByUUID(UUID)+" "+Main.messages.getString("unban_ban"));
                            LogManager.createEntry(UUID, "KONSOLE", "UNBAN_BAN", null);
                        } else if(Main.ban.isMuted(UUID)) {
                            Main.ban.unmute(UUID);
                            Main.ban.sendNotify("UNMUTE", Main.ban.getNameByUUID(UUID), "KONSOLE", "null");
                            BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+"§e§l"+Main.ban.getNameByUUID(UUID)+" "+Main.messages.getString("unban_mute"));
                            LogManager.createEntry(UUID, "KONSOLE", "UNBAN_MUTE", null);
                        } else {
                            BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+"§e§l"+Main.ban.getNameByUUID(UUID)+" "+Main.messages.getString("unban_not_banned"));
                        }
                    } else {
                        BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("player_404"));
                    }
                }
            }
        }
    }
}
