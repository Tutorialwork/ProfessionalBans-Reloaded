package de.tutorialwork.listener;

import de.tutorialwork.commands.SupportChat;
import de.tutorialwork.main.Main;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class Quit implements Listener {

    @EventHandler
    public static void onQuit(PlayerDisconnectEvent e){
        ProxiedPlayer p = e.getPlayer();
        if(SupportChat.activechats.containsKey(p) || SupportChat.activechats.containsValue(p)){
            for(ProxiedPlayer key : SupportChat.activechats.keySet()){
                //Key has started the support chat
                if(key == p){
                    SupportChat.activechats.get(p).sendMessage(Main.Prefix+"§e§l"+p.getName()+" §7hat den Support hat §cbeeendet");
                    SupportChat.activechats.remove(p);
                } else {
                    key.sendMessage(Main.Prefix+"§e§l"+p.getName()+" §7hat den Support Chat §cbeeendet");
                    SupportChat.activechats.remove(key);
                }
            }
        }
    }

}
