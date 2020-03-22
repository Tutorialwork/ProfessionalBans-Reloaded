package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.MessagesManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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

    private String chatformat;
    private String message = "";

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(Main.ban.webaccountExists(p.getUniqueId().toString())){
                if(args.length > 0){
                    message = "";
                    for(int i = 0; i < args.length; i++){
                        message = message + " " + args[i];
                    }

                    try{
                        File file = new File(Main.main.getDataFolder(), "config.yml");
                        Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
                        chatformat = ChatColor.translateAlternateColorCodes('&', cfg.getString("CHATFORMAT.TEAMCHAT"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                        ProxyServer.getInstance().getScheduler().runAsync(Main.main, () -> {
                            if(Main.ban.webaccountExists(all.getUniqueId().toString())){
                                if(!all.getUniqueId().toString().equals(p.getUniqueId().toString())){
                                    all.sendMessage(chatformat.replace("%from%", p.getName()).replace("%message%", message));
                                } else {
                                    all.sendMessage(chatformat.replace("%from%", Main.messages.getString("you")).replace("%message%", message));
                                }
                                MessagesManager.insertMessage(p.getUniqueId().toString(), "TEAM", message);
                                if(MessagesManager.getFirebaseToken(all.getUniqueId().toString()) != null){
                                    MessagesManager.sendPushNotify(MessagesManager.getFirebaseToken(all.getUniqueId().toString()), Main.messages.getString("message_from")+" "+p.getName(), message);
                                }
                            }
                        });
                    }
                } else {
                    p.sendMessage(Main.data.Prefix+"/tc <"+Main.messages.getString("message")+">");
                }
            } else {
                p.sendMessage(Main.data.NoPerms);
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("only_player_cmd"));
        }
    }
}
