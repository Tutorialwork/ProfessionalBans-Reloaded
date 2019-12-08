package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.MessagesManager;
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

public class Broadcast extends Command {
    public Broadcast(String bc) {
        super(bc);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(p.hasPermission("professionalbans.broadcast") || p.hasPermission("professionalbans.*")){
                if(args.length > 0){
                    String message = "";
                    for(int i = 0; i < args.length; i++){
                        message = message + " " + args[i];
                    }
                    try{
                        File file = new File(Main.main.getDataFolder(), "config.yml");
                        Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

                        BungeeCord.getInstance().broadcast("");
                        BungeeCord.getInstance().broadcast("§8[]===================================[]");
                        BungeeCord.getInstance().broadcast("");
                        BungeeCord.getInstance().broadcast(ChatColor.translateAlternateColorCodes('&', cfg.getString("CHATFORMAT.BROADCAST").replace("%message%", message)));
                        BungeeCord.getInstance().broadcast("");
                        BungeeCord.getInstance().broadcast("§8[]===================================[]");
                        BungeeCord.getInstance().broadcast("");

                        MessagesManager.insertMessage(p.getUniqueId().toString(), "BROADCAST", message);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    p.sendMessage(Main.Prefix+"/bc <Nachricht>");
                }
            } else {
                p.sendMessage(Main.NoPerms);
            }
        } else {

        }
    }
}
