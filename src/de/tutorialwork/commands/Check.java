package de.tutorialwork.commands;

import de.tutorialwork.main.Main;
import de.tutorialwork.utils.BanManager;
import de.tutorialwork.utils.IPManager;
import de.tutorialwork.utils.UUIDFetcher;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Check extends Command {
    public Check(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(p.hasPermission("professionalbans.check") || p.hasPermission("professionalbans.*")){
                if(args.length == 0){
                    p.sendMessage(Main.Prefix+"/check <Spieler/IP>");
                } else {
                    String UUID = UUIDFetcher.getUUID(args[0]);
                    if(IPBan.validate(args[0])){
                        String IP = args[0];
                        if(IPManager.IPExists(IP)){
                            p.sendMessage("§8[]===================================[]");
                            if(IPManager.getPlayerFromIP(IP) != null){
                                p.sendMessage("§7Spieler: §e§l"+BanManager.getNameByUUID(IPManager.getPlayerFromIP(IP)));
                            } else {
                                p.sendMessage("§7Spieler: §c§lKeiner");
                            }
                            if(IPManager.isBanned(IP)){
                                p.sendMessage("§7IP-Ban: §c§lJa §8/ "+IPManager.getReasonString(IPManager.getIPFromPlayer(UUID)));
                            } else {
                                p.sendMessage("§7IP-Ban: §a§lNein");
                            }
                            p.sendMessage("§7Bans: §e§l"+IPManager.getBans(IP));
                            p.sendMessage("§7Zuletzt genutzt: §e§l"+BanManager.formatTimestamp(IPManager.getLastUseLong(IP)));
                            p.sendMessage("§8[]===================================[]");
                        } else {
                            p.sendMessage(Main.Prefix+"§cZu dieser IP-Adresse sind keine Informationen verfügbar");
                        }
                    } else {
                        if(BanManager.playerExists(UUID)){
                            p.sendMessage("§8[]===================================[]");
                            p.sendMessage("§7Spieler: §e§l"+BanManager.getNameByUUID(UUID));
                            if(BanManager.isBanned(UUID)){
                                p.sendMessage("§7Gebannt: §c§lJa §8/ "+BanManager.getReasonString(UUID));
                            } else {
                                p.sendMessage("§7Gebannt: §a§lNein");
                            }
                            if(BanManager.isMuted(UUID)){
                                p.sendMessage("§7Gemutet: §c§lJa §8/ "+BanManager.getReasonString(UUID));
                            } else {
                                p.sendMessage("§7Gemutet: §a§lNein");
                            }
                            if(IPManager.isBanned(IPManager.getIPFromPlayer(UUID))){
                                p.sendMessage("§7IP-Ban: §c§lJa §8/ "+IPManager.getReasonString(IPManager.getIPFromPlayer(UUID)));
                            } else {
                                p.sendMessage("§7IP-Ban: §a§lNein");
                            }
                            p.sendMessage("§7Bans: §e§l"+BanManager.getBans(UUID));
                            p.sendMessage("§7Mutes: §e§l"+BanManager.getMutes(UUID));
                            if(BanManager.getLastLogin(UUID) != null){
                                p.sendMessage("§7Letzter Login: §e§l"+BanManager.formatTimestamp(Long.valueOf(BanManager.getLastLogin(UUID))));
                            }
                            if(BanManager.getFirstLogin(UUID) != null){
                                p.sendMessage("§7Erster Login: §e§l"+BanManager.formatTimestamp(Long.valueOf(BanManager.getFirstLogin(UUID))));
                            }
                            p.sendMessage("§8[]===================================[]");
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
                BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"/check <Spieler/IP>");
            } else {
                String UUID = UUIDFetcher.getUUID(args[0]);
                if(IPBan.validate(args[0])){
                    String IP = args[0];
                    if(IPManager.IPExists(IP)){
                        BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
                        if(IPManager.getPlayerFromIP(IP) != null){
                            BungeeCord.getInstance().getConsole().sendMessage("§7Spieler: §e§l"+BanManager.getNameByUUID(IPManager.getPlayerFromIP(IP)));
                        } else {
                            BungeeCord.getInstance().getConsole().sendMessage("§7Spieler: §c§lKeiner");
                        }
                        if(IPManager.isBanned(IP)){
                            BungeeCord.getInstance().getConsole().sendMessage("§7IP-Ban: §c§lJa §8/ "+IPManager.getReasonString(IPManager.getIPFromPlayer(UUID)));
                        } else {
                            BungeeCord.getInstance().getConsole().sendMessage("§7IP-Ban: §a§lNein");
                        }
                        BungeeCord.getInstance().getConsole().sendMessage("§7Bans: §e§l"+IPManager.getBans(IP));
                        BungeeCord.getInstance().getConsole().sendMessage("§7Zuletzt genutzt: §e§l"+BanManager.formatTimestamp(IPManager.getLastUseLong(IP)));
                        BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
                    } else {
                        BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§cZu dieser IP-Adresse sind keine Informationen verfügbar");
                    }
                } else {
                    if(BanManager.playerExists(UUID)){
                        BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
                        BungeeCord.getInstance().getConsole().sendMessage("§7Spieler: §e§l"+BanManager.getNameByUUID(UUID));
                        if(BanManager.isBanned(UUID)){
                            BungeeCord.getInstance().getConsole().sendMessage("§7Gebannt: §c§lJa §8/ "+BanManager.getReasonString(UUID));
                        } else {
                            BungeeCord.getInstance().getConsole().sendMessage("§7Gebannt: §a§lNein");
                        }
                        if(BanManager.isMuted(UUID)){
                            BungeeCord.getInstance().getConsole().sendMessage("§7Gemutet: §c§lJa §8/ "+BanManager.getReasonString(UUID));
                        } else {
                            BungeeCord.getInstance().getConsole().sendMessage("§7Gemutet: §a§lNein");
                        }
                        if(IPManager.isBanned(IPManager.getIPFromPlayer(UUID))){
                            BungeeCord.getInstance().getConsole().sendMessage("§7IP-Ban: §c§lJa §8/ "+IPManager.getReasonString(IPManager.getIPFromPlayer(UUID)));
                        } else {
                            BungeeCord.getInstance().getConsole().sendMessage("§7IP-Ban: §a§lNein");
                        }
                        BungeeCord.getInstance().getConsole().sendMessage("§7Bans: §e§l"+BanManager.getBans(UUID));
                        BungeeCord.getInstance().getConsole().sendMessage("§7Mutes: §e§l"+BanManager.getMutes(UUID));
                        if(BanManager.getLastLogin(UUID) != null){
                            BungeeCord.getInstance().getConsole().sendMessage("§7Letzter Login: §e§l"+BanManager.formatTimestamp(Long.valueOf(BanManager.getLastLogin(UUID))));
                        }
                        if(BanManager.getFirstLogin(UUID) != null){
                            BungeeCord.getInstance().getConsole().sendMessage("§7Erster Login: §e§l"+BanManager.formatTimestamp(Long.valueOf(BanManager.getFirstLogin(UUID))));
                        }
                        BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
                    } else {
                        BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§cDieser Spieler hat den Server noch nie betreten");
                    }
                }
            }
        }
    }
}
