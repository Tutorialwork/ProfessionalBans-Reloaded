package de.tutorialwork.commands;

import de.tutorialwork.main.Main;
import de.tutorialwork.utils.BanManager;
import de.tutorialwork.utils.IPManager;
import de.tutorialwork.utils.LogManager;
import de.tutorialwork.utils.UUIDFetcher;
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
                    p.sendMessage(Main.Prefix+"/unban <Spieler/IP>");
                } else {
                    String UUID = UUIDFetcher.getUUID(args[0]);
                    if(IPBan.validate(args[0])){
                        IPManager.unban(args[0]);
                        BanManager.sendNotify("UNBANIP", args[0], p.getName(), null);
                        p.sendMessage(Main.Prefix+"§7Die IP-Adresse §e§l"+args[0]+" §7wurde §aerfolgreich §7entbannt");
                        LogManager.createEntry(null, p.getUniqueId().toString(), "UNBAN_IP", args[0]);
                    } else {
                        if(BanManager.playerExists(UUID)){
                            if(IPManager.isBanned(IPManager.getIPFromPlayer(UUID))){
                                IPManager.unban(IPManager.getIPFromPlayer(UUID));
                                p.sendMessage(Main.Prefix+"Die IP §e§l"+IPManager.getIPFromPlayer(UUID)+ " §7war gebannt und wurde ebenfalls §aentbannt");
                            }
                            if(BanManager.isBanned(UUID)){
                                BanManager.unban(UUID);
                                BanManager.sendNotify("UNBAN", BanManager.getNameByUUID(UUID), p.getName(), "null");
                                p.sendMessage(Main.Prefix+"§e§l"+BanManager.getNameByUUID(UUID)+" §7wurde §aerfolgreich §7entbannt");
                                LogManager.createEntry(UUID, p.getUniqueId().toString(), "UNBAN_BAN", null);
                            } else if(BanManager.isMuted(UUID)) {
                                BanManager.unmute(UUID);
                                BanManager.sendNotify("UNMUTE", BanManager.getNameByUUID(UUID), p.getName(), "null");
                                p.sendMessage(Main.Prefix+"§e§l"+BanManager.getNameByUUID(UUID)+" §7wurde §aerfolgreich §7entmutet");
                                LogManager.createEntry(UUID, p.getUniqueId().toString(), "UNBAN_MUTE", null);
                            } else {
                                p.sendMessage(Main.Prefix+"§e§l"+BanManager.getNameByUUID(UUID)+" §7ist weder gebannt oder gemutet");
                            }
                        } else {
                            p.sendMessage(Main.Prefix+"§cDieser Spieler hat den Server noch nie betreten");
                        }
                    }
                }
            } else {
                p.sendMessage(Main.NoPerms);
            }
        } else {
            if(args.length == 0){
                BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"/unban <Spieler/IP>");
            } else {
                String UUID = UUIDFetcher.getUUID(args[0]);
                if(IPBan.validate(args[0])){
                    IPManager.unban(args[0]);
                    BanManager.sendNotify("UNBANIP", args[0], "KONSOLE", null);
                    BungeeCord.getInstance().getConsole().sendMessage((Main.Prefix+"§7Die IP-Adresse §e§l"+args[0]+" §7wurde §aerfolgreich §7entbannt"));
                    LogManager.createEntry(null, "KONSOLE", "UNBAN_IP", args[0]);
                } else {
                    if(BanManager.playerExists(UUID)){
                        if(IPManager.isBanned(IPManager.getIPFromPlayer(UUID))){
                            IPManager.unban(IPManager.getIPFromPlayer(UUID));
                            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"Die IP §e§l"+IPManager.getIPFromPlayer(UUID)+ " §7war gebannt und wurde ebenfalls §aentbannt");
                        }
                        if(BanManager.isBanned(UUID)){
                            BanManager.unban(UUID);
                            BanManager.sendNotify("UNBAN", BanManager.getNameByUUID(UUID), "KONSOLE", "null");
                            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§e§l"+BanManager.getNameByUUID(UUID)+" §7wurde §aerfolgreich §7entbannt");
                            LogManager.createEntry(UUID, "KONSOLE", "UNBAN_BAN", null);
                        } else if(BanManager.isMuted(UUID)) {
                            BanManager.unmute(UUID);
                            BanManager.sendNotify("UNMUTE", BanManager.getNameByUUID(UUID), "KONSOLE", "null");
                            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§e§l"+BanManager.getNameByUUID(UUID)+" §7wurde §aerfolgreich §7entmutet");
                            LogManager.createEntry(UUID, "KONSOLE", "UNBAN_MUTE", null);
                        } else {
                            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§e§l"+BanManager.getNameByUUID(UUID)+" §7ist weder gebannt oder gemutet");
                        }
                    } else {
                        BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§cDieser Spieler hat den Server noch nie betreten");
                    }
                }
            }
        }
    }
}
