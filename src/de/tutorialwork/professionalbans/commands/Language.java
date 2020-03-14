package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class Language extends Command {
    public Language(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(p.hasPermission("professionalbans.*")){
                if(args.length == 0){
                    p.sendMessage(Main.Prefix+"/language <name>");
                } else {
                    setLanguage(args[0]);
                    switch (args[0]){
                        case "en":
                            p.sendMessage(Main.Prefix+"The language was set to §e§lEnglish");
                            Language.initLanguage(Main.locale_en);
                            break;
                        case "de":
                            p.sendMessage(Main.Prefix+"Die Sprache wurde zu §e§lDeutsch §7gesetzt");
                            Language.initLanguage(Main.locale_de);
                            break;
                    }
                }
            } else {
                p.sendMessage(Main.NoPerms);
            }
        }
    }

    public static boolean isLanguageSet(){
        try{
            File file = new File(Main.getInstance().getDataFolder().getPath(), "config.yml");
            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

            if(cfg.getString("LANGUAGE").equalsIgnoreCase("de") || cfg.getString("LANGUAGE").equalsIgnoreCase("en")){
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setLanguage(String lang){
        try{
            File file = new File(Main.getInstance().getDataFolder().getPath(), "config.yml");
            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

            cfg.set("LANGUAGE", lang);

            ConfigurationProvider.getProvider(YamlConfiguration.class).save(cfg, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLanguage(){
        try{
            File file = new File(Main.getInstance().getDataFolder().getPath(), "config.yml");
            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

            return cfg.getString("LANGUAGE");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void initLanguage(Locale locale){
        Main.messages = ResourceBundle.getBundle("messages", locale);
    }
}
