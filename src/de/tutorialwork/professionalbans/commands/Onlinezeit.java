package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;

import de.tutorialwork.professionalbans.utils.BanManager;
import de.tutorialwork.professionalbans.utils.TimeManager;
import de.tutorialwork.professionalbans.utils.UUIDFetcher;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;

public class Onlinezeit extends Command {
    public Onlinezeit(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(args.length == 0){
                ArrayList<String> players = TimeManager.getTopOnlineTime();
                p.sendMessage("§8[]============§8[§e§l Top "+players.size()+" "+Main.messages.getString("ontime")+" §8]============[]");
                for(int i = 0; i < players.size(); i++){
                    int rank = i + 1;
                    String name = BanManager.getNameByUUID(players.get(i));
                    p.sendMessage("§8#§7"+rank+" » §e"+name+" §7- "+TimeManager.formatOnlineTime(TimeManager.getOnlineTime(players.get(i))));
                }
            } else {
                if(args.length == 1){
                    String UUID = UUIDFetcher.getUUID(args[0]);
                    if(UUID != null && BanManager.playerExists(UUID)){
                        ProxiedPlayer target = BungeeCord.getInstance().getPlayer(args[0]);
                        p.sendMessage("§8[]=================§8[§e§l "+BanManager.getNameByUUID(UUID)+" §8]=================[]");

                        if(target != null){
                            p.sendMessage(Main.messages.getString("ontime_on_msg")+TimeManager.formatOnlineTime(TimeManager.getOnlineTime(UUID)));
                        } else {
                            p.sendMessage(Main.messages.getString("ontime_off_msg").replace("%date%", BanManager.formatTimestamp(Long.valueOf(BanManager.getLastLogin(UUID))))+TimeManager.formatOnlineTime(TimeManager.getOnlineTime(UUID)));
                        }

                        String spaces = "";
                        for(int i = 0; i < BanManager.getNameByUUID(UUID).length() + 4; i++){
                            spaces = spaces + "=";
                        }
                        p.sendMessage("§8[]=================================="+spaces+"[]");
                    } else {
                        p.sendMessage(Main.Prefix+Main.messages.getString("player_404"));
                    }
                } else {
                    p.sendMessage(Main.Prefix+"/onlinezeit <§e"+Main.messages.getString("player")+"§7>");
                }
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+Main.messages.getString("only_player_cmd"));
        }
    }
}
