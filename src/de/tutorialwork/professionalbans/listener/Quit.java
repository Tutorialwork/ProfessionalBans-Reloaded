package de.tutorialwork.professionalbans.listener;

import de.tutorialwork.professionalbans.commands.SupportChat;
import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.MessagesManager;
import de.tutorialwork.professionalbans.utils.TimeManager;
import net.md_5.bungee.BungeeCord;
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
        TimeManager.updateOnlineStatus(p.getUniqueId().toString(), 0);
        try{
            long onlinetime = TimeManager.getOnlineTime(p.getUniqueId().toString());
            long logintime = Login.logintimes.get(p);
            long currentonlinetime = System.currentTimeMillis() - logintime;
            TimeManager.setOnlineTime(p.getUniqueId().toString(), onlinetime + currentonlinetime);
        } catch (NullPointerException e1){
            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§c§lFEHLER: §cOnlinezeit konnte für "+p.getName()+" nicht aktualisiert werden");
        }
    }

}
