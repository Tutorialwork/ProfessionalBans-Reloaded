package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.listener.Chat;
import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.LogManager;
import de.tutorialwork.professionalbans.utils.UUIDFetcher;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Chatlog extends Command {
    public Chatlog(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(args.length == 0){
                p.sendMessage(Main.data.Prefix+"/chatlog <"+Main.messages.getString("player")+">");
            } else {
                String UUID = UUIDFetcher.getUUID(args[0]);
                if(UUID != null){
                    if(Main.ban.playerExists(UUID)){
                        if(!p.getUniqueId().toString().equals(UUID)){
                            if(Chat.hasMessages(UUID)){
                                String ID = Chat.createChatlog(UUID, p.getUniqueId().toString());
                                p.sendMessage(Main.data.Prefix+Main.messages.getString("chatlog_success").replace("%player%", Main.ban.getNameByUUID(UUID)));
                                if(Main.data.WebURL != null){
                                    p.sendMessage(Main.data.Prefix+"Link: §e§l"+Main.data.WebURL+"/chatlogs/"+ID);
                                } else {
                                    p.sendMessage(Main.data.Prefix+Main.messages.getString("link_err"));
                                }
                                LogManager.createEntry(UUID, p.getUniqueId().toString(), "CREATE_CHATLOG", ID);
                            } else {
                                p.sendMessage(Main.data.Prefix+Main.messages.getString("chatlog_no_msg"));
                            }
                        } else {
                            p.sendMessage(Main.data.Prefix+Main.messages.getString("chatlog_self_err"));
                        }
                    } else {
                        p.sendMessage(Main.data.Prefix+Main.messages.getString("player_404"));
                    }
                } else {
                    p.sendMessage(Main.data.Prefix+Main.messages.getString("player_404"));
                }
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("only_player_cmd"));
        }
    }
}
