package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.LogManager;
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

public class Blacklist extends Command {
    public Blacklist(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(p.hasPermission("professionalbans.blacklist") || p.hasPermission("professionalbans.*")){
                if(args.length == 0 || args.length == 1){
                    p.sendMessage(Main.data.Prefix+Main.messages.getString("wordblacklist").replace("%count%", Main.data.blacklist.size()+""));
                    p.sendMessage(Main.data.Prefix+"/blacklist <add/del> <"+Main.messages.getString("word")+">");
                } else if(args.length == 2){
                    File blacklist = new File(Main.main.getDataFolder(), "blacklist.yml");
                    try {
                        Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(blacklist);

                        ArrayList<String> tempblacklist = new ArrayList<>();

                        if(args[0].equalsIgnoreCase("add")){
                            String Wort = args[1];
                            for(String congigstr : cfg.getStringList("BLACKLIST")){
                                tempblacklist.add(congigstr);
                            }
                            tempblacklist.add(Wort);
                            Main.data.blacklist.add(Wort);
                            cfg.set("BLACKLIST", tempblacklist);
                            p.sendMessage(Main.data.Prefix+"§e§l"+Wort+" "+Main.messages.getString("wordblacklist_add"));
                            LogManager.createEntry(null, p.getUniqueId().toString(), "ADD_WORD_BLACKLIST", Wort);
                        } else if(args[0].equalsIgnoreCase("del")){
                            String Wort = args[1];
                            if(Main.data.blacklist.contains(Wort)){
                                for(String congigstr : cfg.getStringList("BLACKLIST")){
                                    tempblacklist.add(congigstr);
                                }
                                tempblacklist.remove(Wort);
                                Main.data.blacklist.remove(Wort);
                                cfg.set("BLACKLIST", tempblacklist);
                                p.sendMessage(Main.data.Prefix+"§e§l"+Wort+" "+Main.messages.getString("wordblacklist_del"));
                                LogManager.createEntry(null, p.getUniqueId().toString(), "DEL_WORD_BLACKLIST", Wort);
                            } else {
                                p.sendMessage(Main.data.Prefix+Main.messages.getString("word_404"));
                            }
                        } else {
                            p.sendMessage(Main.data.Prefix+Main.messages.getString("wordblacklist").replace("%count%", Main.data.blacklist.size()+""));
                            p.sendMessage(Main.data.Prefix+"/blacklist <add/del> <"+Main.messages.getString("word")+">");
                        }

                        ConfigurationProvider.getProvider(YamlConfiguration.class).save(cfg, blacklist);
                        tempblacklist.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    p.sendMessage(Main.data.Prefix+Main.messages.getString("wordblacklist").replace("%count%", Main.data.blacklist.size()+""));
                    p.sendMessage(Main.data.Prefix+"/blacklist <add/del> <"+Main.messages.getString("word")+">");
                }
            } else {
                p.sendMessage(Main.data.NoPerms);
            }
        } else {
            //KONSOLE
            if(args.length == 0 || args.length == 1){
                BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("wordblacklist").replace("%count%", Main.data.blacklist.size()+""));
                BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+"/blacklist <add/del> <"+Main.messages.getString("word")+">");
            } else if(args.length == 2){
                File blacklist = new File(Main.main.getDataFolder(), "blacklist.yml");
                try {
                    Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(blacklist);

                    ArrayList<String> tempblacklist = new ArrayList<>();

                    if(args[0].equalsIgnoreCase("add")){
                        String Wort = args[1];
                        for(String congigstr : cfg.getStringList("BLACKLIST")){
                            tempblacklist.add(congigstr);
                        }
                        tempblacklist.add(Wort);
                        Main.data.blacklist.add(Wort);
                        cfg.set("BLACKLIST", tempblacklist);
                        BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+"§e§l"+Wort+" "+Main.messages.getString("wordblacklist_add"));
                        LogManager.createEntry(null, "KONSOLE", "ADD_WORD_BLACKLIST", Wort);
                    } else if(args[0].equalsIgnoreCase("del")){
                        String Wort = args[1];
                        if(Main.data.blacklist.contains(Wort)){
                            for(String congigstr : cfg.getStringList("BLACKLIST")){
                                tempblacklist.add(congigstr);
                            }
                            tempblacklist.remove(Wort);
                            Main.data.blacklist.remove(Wort);
                            cfg.set("BLACKLIST", tempblacklist);
                            BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+"§e§l"+Wort+" "+Main.messages.getString("wordblacklist_del"));
                            LogManager.createEntry(null, "KONSOLE", "DEL_WORD_BLACKLIST", Wort);
                        } else {
                            BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("word_404"));
                        }
                    } else {
                        BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("wordblacklist").replace("%count%", Main.data.blacklist.size()+""));
                        BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+"/blacklist <add/del> <"+Main.messages.getString("word")+">");
                    }

                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(cfg, blacklist);
                    tempblacklist.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("wordblacklist").replace("%count%", Main.data.blacklist.size()+""));
                BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+"/blacklist <add/del> <"+Main.messages.getString("word")+">");
            }
        }
    }
}
