package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.BanManager;
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

public class TeamChat extends Command {
    public TeamChat(String tc) {
        super(tc);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(BanManager.webaccountExists(p.getUniqueId().toString())){
                if(args.length > 0){
                    String message = "";
                    for(int i = 0; i < args.length; i++){
                        message = message + " " + args[i];
                    }

                    for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                        if(BanManager.webaccountExists(all.getUniqueId().toString())){
                            try{
                                File file = new File(Main.main.getDataFolder(), "config.yml");
                                Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

                                if(!all.getUniqueId().toString().equals(p.getUniqueId().toString())){
                                    all.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getString("CHATFORMAT.TEAMCHAT").replace("%from%", p.getName()).replace("%message%", message)));
                                } else {
                                    all.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getString("CHATFORMAT.TEAMCHAT").replace("%from%", Main.messages.getString("you")).replace("%message%", message)));
                                }
                                MessagesManager.insertMessage(p.getUniqueId().toString(), "TEAM", message);
                                if(MessagesManager.getFirebaseToken(all.getUniqueId().toString()) != null){
                                    MessagesManager.sendPushNotify(MessagesManager.getFirebaseToken(all.getUniqueId().toString()), Main.messages.getString("message_from")+" "+p.getName(), message);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    p.sendMessage(Main.Prefix+"/tc <"+Main.messages.getString("message")+">");
                }
            } else {
                p.sendMessage(Main.NoPerms);
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+Main.messages.getString("only_player_cmd"));
        }
    }
}
