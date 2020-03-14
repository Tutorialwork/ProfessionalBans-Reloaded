package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.BanManager;
import de.tutorialwork.professionalbans.utils.LogManager;
import de.tutorialwork.professionalbans.utils.UUIDFetcher;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Report extends Command {
    public Report(String name) {
        super(name);
    }

    public static ArrayList<ProxiedPlayer> players = new ArrayList<>();

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
                p.sendMessage(Main.Prefix+Main.messages.getString("reason")+": §e§l"+reasons);
                p.sendMessage(Main.Prefix+"/report <"+Main.messages.getString("player")+"> <"+Main.messages.getString("reason")+">");
            } else {
                if(args[0].toUpperCase().equals(p.getName().toUpperCase())){
                    p.sendMessage(Main.Prefix+Main.messages.getString("no_self_report"));
                    return;
                }
                if(Main.reportreasons.contains(args[1].toUpperCase())){
                    ProxiedPlayer target = BungeeCord.getInstance().getPlayer(args[0]);
                    if(target != null){
                        if(!players.contains(p)){
                            players.add(p);
                            BanManager.createReport(target.getUniqueId().toString(), p.getUniqueId().toString(), args[1].toUpperCase(), null);
                            p.sendMessage(Main.Prefix+Main.messages.getString("report_success").replace("%player%", target.getName()).replace("%reason%", args[1].toUpperCase()));
                            BanManager.sendNotify("REPORT", target.getName(), p.getName(), args[1].toUpperCase());
                            LogManager.createEntry(target.getUniqueId().toString(), p.getUniqueId().toString(), "REPORT", args[1].toUpperCase());
                        } else {
                            p.sendMessage(Main.Prefix+Main.messages.getString("wait"));
                        }
                    } else {
                        try{
                            File file = new File(Main.main.getDataFolder(), "config.yml");
                            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
                            if(cfg.getBoolean("REPORTS.OFFLINEREPORTS")){
                                String UUID = UUIDFetcher.getUUID(args[0]);
                                if(UUID != null){
                                    if(BanManager.playerExists(UUID)){
                                        if(!players.contains(p)){
                                            players.add(p);
                                            BanManager.createReport(UUID, p.getUniqueId().toString(), args[1].toUpperCase(), null);
                                            p.sendMessage(Main.Prefix+Main.messages.getString("report_success").replace("%player%", args[0]).replace("%reason%", args[1].toUpperCase()));
                                            LogManager.createEntry(UUID, p.getUniqueId().toString(), "REPORT_OFFLINE", args[1].toUpperCase());
                                        } else {
                                            p.sendMessage(Main.Prefix+Main.messages.getString("wait"));
                                        }
                                    } else {
                                        p.sendMessage(Main.Prefix+Main.messages.getString("player_404"));
                                    }
                                } else {
                                    p.sendMessage(Main.Prefix+Main.messages.getString("player_404"));
                                }
                            } else {
                                p.sendMessage(Main.Prefix+Main.messages.getString("player_404"));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    p.sendMessage(Main.Prefix+Main.messages.getString("report_reason_404"));
                }
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+Main.messages.getString("only_player_cmd"));
        }
    }
}
