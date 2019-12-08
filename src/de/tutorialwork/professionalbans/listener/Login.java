package de.tutorialwork.professionalbans.listener;

import de.tutorialwork.professionalbans.commands.SupportChat;
import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.IPManager;
import de.tutorialwork.professionalbans.utils.BanManager;
import de.tutorialwork.professionalbans.utils.MessagesManager;
import de.tutorialwork.professionalbans.utils.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;

public class Login implements Listener {

    @EventHandler
    public void onJoin(PreLoginEvent e){
        String UUID = UUIDFetcher.getUUID(e.getConnection().getName());
        String IP = e.getConnection().getAddress().getHostString();
        BanManager.createPlayer(UUID, e.getConnection().getName());
        IPManager.insertIP(IP, UUID);
        File config = new File(Main.main.getDataFolder(), "config.yml");
        try{
            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
            if(cfg.getBoolean("VPN.BLOCKED")){
                if(!Main.ipwhitelist.contains(IP)){
                    if(IPManager.isVPN(IP)){
                        if(cfg.getBoolean("VPN.KICK")){
                            e.setCancelled(true);
                            e.setCancelReason(ChatColor.translateAlternateColorCodes('&', cfg.getString("VPN.KICKMSG")));
                        }
                        if(cfg.getBoolean("VPN.BAN")){
                            int id = cfg.getInt("VPN.BANID");
                            BanManager.ban(UUID, id, "KONSOLE", Main.increaseValue, Main.increaseBans);
                            BanManager.sendNotify("IPBAN", e.getConnection().getAddress().getHostString(), "KONSOLE", BanManager.getReasonByID(id));
                            e.setCancelled(true);
                            if(BanManager.getRAWEnd(UUID) == -1L){
                                e.setCancelReason(ChatColor.translateAlternateColorCodes('&', cfg.getString("LAYOUT.IPBAN").replace("%grund%", BanManager.getReasonByID(id))));
                            } else {
                                String MSG = cfg.getString("LAYOUT.TEMPIPBAN");
                                MSG = MSG.replace("%grund%", BanManager.getReasonString(UUID));
                                MSG = MSG.replace("%dauer%", BanManager.getEnd(UUID));
                                e.setCancelReason(ChatColor.translateAlternateColorCodes('&', MSG));
                            }
                        }
                    }
                }
            }
        } catch (IOException er){
            er.printStackTrace();
        }
        if(IPManager.isBanned(IP)){
            try {
                Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);

                if(IPManager.getRAWEnd(IP) == -1L){
                    e.setCancelled(true);
                    e.setCancelReason(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.IPBAN").replace("%grund%", IPManager.getReasonString(IP))));
                } else {
                    if(System.currentTimeMillis() < IPManager.getRAWEnd(IP)){
                        e.setCancelled(true);
                        String MSG = configcfg.getString("LAYOUT.TEMPIPBAN");
                        MSG = MSG.replace("%grund%", IPManager.getReasonString(IP));
                        MSG = MSG.replace("%dauer%", IPManager.getEnd(IP));
                        e.setCancelReason(ChatColor.translateAlternateColorCodes('&', MSG));
                    } else {
                        IPManager.unban(IP);
                    }
                }

                ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        if(BanManager.isBanned(UUID)){
            try {
                Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);

                if(BanManager.getRAWEnd(UUID) == -1L){
                    e.setCancelled(true);
                    e.setCancelReason(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.BAN").replace("%grund%", BanManager.getReasonString(UUID))));
                } else {
                    if(System.currentTimeMillis() < BanManager.getRAWEnd(UUID)){
                        e.setCancelled(true);
                        String MSG = configcfg.getString("LAYOUT.TEMPBAN");
                        MSG = MSG.replace("%grund%", BanManager.getReasonString(UUID));
                        MSG = MSG.replace("%dauer%", BanManager.getEnd(UUID));
                        e.setCancelReason(ChatColor.translateAlternateColorCodes('&', MSG));
                    } else {
                        BanManager.unban(UUID);
                    }
                }

                ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onFinalLogin(PostLoginEvent e){
        ProxiedPlayer p = e.getPlayer();
        if(p.hasPermission("professionalbans.reports") || p.hasPermission("professionalbans.*")){
            if(BanManager.countOpenReports() != 0){
                p.sendMessage(Main.Prefix+"Derzeit sind noch §e§l"+BanManager.countOpenReports()+" Reports §7offen");
            }
        }
        if(p.hasPermission("professionalbans.supportchat") || p.hasPermission("professionalbans.*")){
            if(SupportChat.openchats.size() != 0){
                p.sendMessage(Main.Prefix+"Derzeit sind noch §e§l"+SupportChat.openchats.size()+" §7Support Chat Anfragen §aoffen");
            }
        }
        //Update Check
        if(p.hasPermission("professionalbans.*")){
            if(!Main.callURL("https://api.spigotmc.org/legacy/update.php?resource=63657").equals(Main.Version)){
                p.sendMessage("§8[]===================================[]");
                p.sendMessage("§e§lProfessionalBans §7Reloaded §8| §7Version §c"+Main.Version);
                p.sendMessage("§cDu benutzt eine §c§lVERALTETE §cVersion des Plugins!");
                p.sendMessage("§7Update: §4§lhttps://spigotmc.org/resources/63657");
                p.sendMessage("§8[]===================================[]");
            }
        }

        MessagesManager.updateOnlineStatus(p.getUniqueId().toString(), 1);
    }

}
