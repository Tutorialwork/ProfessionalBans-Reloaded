package de.tutorialwork.commands;

import de.tutorialwork.main.Main;
import de.tutorialwork.utils.BanManager;
import de.tutorialwork.utils.LogManager;
import de.tutorialwork.utils.UUIDFetcher;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Report extends Command {
    public Report(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(args.length == 0 || args.length == 1){
                String reasons = "";
                int komma = Main.reportreasons.size();
                for(String reason : Main.reportreasons){
                    komma--;
                    if(komma != 0){
                        reasons = reasons + reason + ", ";
                    } else {
                        reasons = reasons + reason;
                    }
                }
                p.sendMessage(Main.Prefix+"Verfügbare Reportgründe: §e§l"+reasons);
                p.sendMessage(Main.Prefix+"/report <Spieler> <Grund>");
            } else {
                if(args[0].toUpperCase().equals(p.getName().toUpperCase())){
                    p.sendMessage(Main.Prefix+"§cDu kannst dich nicht selbst melden");
                    return;
                }
                if(Main.reportreasons.contains(args[1].toUpperCase())){
                    ProxiedPlayer target = BungeeCord.getInstance().getPlayer(args[0]);
                    if(target != null){
                        BanManager.createReport(target.getUniqueId().toString(), p.getUniqueId().toString(), args[1].toUpperCase(), null);
                        p.sendMessage(Main.Prefix+"Der Spieler §e§l"+target.getName()+" §7wurde erfolgreich wegen §e§l"+args[1].toUpperCase()+" §7gemeldet");
                        BanManager.sendNotify("REPORT", target.getName(), p.getName(), args[1].toUpperCase());
                        LogManager.createEntry(target.getUniqueId().toString(), p.getUniqueId().toString(), "REPORT", args[1].toUpperCase());
                    } else {
                        try{
                            File file = new File(Main.main.getDataFolder(), "config.yml");
                            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
                            if(cfg.getBoolean("REPORTS.OFFLINEREPORTS")){
                                String UUID = UUIDFetcher.getUUID(args[0]);
                                if(UUID != null){
                                    if(BanManager.playerExists(UUID)){
                                        BanManager.createReport(UUID, p.getUniqueId().toString(), args[1].toUpperCase(), null);
                                        p.sendMessage(Main.Prefix+"Der Spieler §e§l"+target.getName()+" §7(§4Offline§7) wurde erfolgreich wegen §e§l"+args[1].toUpperCase()+" §7gemeldet");
                                        LogManager.createEntry(UUID, p.getUniqueId().toString(), "REPORT_OFFLINE", args[1].toUpperCase());
                                    } else {
                                        p.sendMessage(Main.Prefix+"§cDieser Spieler wurde nicht gefunden");
                                    }
                                } else {
                                    p.sendMessage(Main.Prefix+"§cDieser Spieler wurde nicht gefunden");
                                }
                            } else {
                                p.sendMessage(Main.Prefix+"§cDieser Spieler ist offline");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    p.sendMessage(Main.Prefix+"§cDer eingegebene Reportgrund wurde nicht gefunden");
                }
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§e§lReports §7sind nur als Spieler verfügbar");
        }
    }
}
