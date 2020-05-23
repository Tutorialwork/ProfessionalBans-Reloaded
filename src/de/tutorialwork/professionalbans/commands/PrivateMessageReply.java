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

public class PrivateMessageReply extends Command {
    public PrivateMessageReply(String r) {
        super(r);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(!Main.ban.isMuted(p.getUniqueId().toString())){
                if(MessagesManager.getLastChatPlayer(p) != null){
                    if(args.length > 0){
                        String message = "";
                        for(int i = 0; i < args.length; i++){
                            message = message + " " + args[i];
                        }
                        if(!Main.ban.isMuted(MessagesManager.getLastChatPlayer(p).getUniqueId().toString())){
                            if(!MSGToggle.toggle.contains(MessagesManager.getLastChatPlayer(p))){
                                MessagesManager.sendMessage(p, MessagesManager.getLastChatPlayer(p), message);
                            } else {
                                p.sendMessage(Main.data.Prefix+Main.messages.getString("msg_toggled"));
                            }
                        } else {
                            p.sendMessage(Main.data.Prefix+Main.messages.getString("target_muted"));
                        }
                    } else {
                        p.sendMessage(Main.data.Prefix+"/r <"+Main.messages.getString("message")+"> - §8§oAntwortet §e§l"+MessagesManager.getLastChatPlayer(p).getName());
                    }
                } else {
                    p.sendMessage(Main.data.Prefix+Main.messages.getString("no_reply"));
                }
            } else {
                File config = new File(Main.main.getDataFolder(), "config.yml");
                try{
                    Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);

                    String MSG = configcfg.getString("LAYOUT.TEMPMUTE");
                    MSG = MSG.replace("%grund%", Main.ban.getReasonString(p.getUniqueId().toString()));
                    MSG = MSG.replace("%dauer%", Main.ban.getEnd(p.getUniqueId().toString()));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG));
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("only_player_cmd"));
        }
    }
}
