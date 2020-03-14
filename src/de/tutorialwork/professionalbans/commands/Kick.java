package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.BanManager;
import de.tutorialwork.professionalbans.utils.LogManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Kick extends Command {
    public Kick(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(p.hasPermission("professionalbans.kick") || p.hasPermission("professionalbans.*")){
                if(args.length == 0 || args.length == 1){
                    p.sendMessage(Main.Prefix+"/kick <"+Main.messages.getString("player")+"> <"+Main.messages.getString("reason")+">");
                } else {
                    ProxiedPlayer tokick = BungeeCord.getInstance().getPlayer(args[0]);
                    if(tokick != null){
                        File config = new File(Main.main.getDataFolder(), "config.yml");
                        try {
                            Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                            String grund = "";
                            for(int i = 1; i < args.length; i++){
                                grund = grund + " " + args[i];
                            }
                            BanManager.sendNotify("KICK", tokick.getName(), p.getName(), grund);
                            LogManager.createEntry(tokick.getUniqueId().toString(), p.getUniqueId().toString(), "KICK", grund);
                            tokick.disconnect(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.KICK").replace("%grund%", grund)));
                            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        p.sendMessage(Main.Prefix+Main.messages.getString("player_404"));
                    }
                }
            } else {
                p.sendMessage(Main.NoPerms);
            }
        } else {
            if(args.length == 0 || args.length == 1){
                BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"/kick <"+Main.messages.getString("player")+"> <"+Main.messages.getString("reason")+">");
            } else {
                ProxiedPlayer tokick = BungeeCord.getInstance().getPlayer(args[0]);
                if(tokick != null){
                    File config = new File(Main.main.getDataFolder(), "config.yml");
                    try {
                        Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                        String grund = "";
                        for(int i = 1; i < args.length; i++){
                            grund = grund + " " + args[i];
                        }
                        BanManager.sendNotify("KICK", tokick.getName(), "KONSOLE", grund);
                        LogManager.createEntry(tokick.getUniqueId().toString(), "KONSOLE", "KICK", grund);
                        tokick.disconnect(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.KICK").replace("%grund%", grund)));
                        ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+Main.messages.getString("player_404"));
                }
            }
        }
    }
}
