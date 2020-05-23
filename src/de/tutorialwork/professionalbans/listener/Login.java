package de.tutorialwork.professionalbans.listener;

import de.tutorialwork.professionalbans.commands.Language;
import de.tutorialwork.professionalbans.commands.SupportChat;
import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.*;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Login implements Listener {

    public static HashMap<ProxiedPlayer, Long> logintimes = new HashMap<>();

    @EventHandler
    public void onJoin(LoginEvent e){
        String UUID = UUIDFetcher.getUUID(e.getConnection().getName());
        String IP = e.getConnection().getAddress().getHostString();
        Main.ban.createPlayer(UUID, e.getConnection().getName());
        Main.ip.insertIP(IP, UUID);
        File config = new File(Main.main.getDataFolder(), "config.yml");
        try{
            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
            if(cfg.getBoolean("VPN.BLOCKED")){
                if(!Main.data.ipwhitelist.contains(IP)){
                    if(Main.ip.isVPN(IP)){
                        if(cfg.getBoolean("VPN.KICK")){
                            e.setCancelled(true);
                            e.setCancelReason(ChatColor.translateAlternateColorCodes('&', cfg.getString("VPN.KICKMSG")));
                        }
                        if(cfg.getBoolean("VPN.BAN")){
                            int id = cfg.getInt("VPN.BANID");
                            Main.ban.ban(UUID, id, "KONSOLE", Main.data.increaseValue, Main.data.increaseBans);
                            Main.ban.sendNotify("IPBAN", e.getConnection().getAddress().getHostString(), "KONSOLE", Main.ban.getReasonByID(id));
                            e.setCancelled(true);
                            if(Main.ban.getRAWEnd(UUID) == -1L){
                                e.setCancelReason(ChatColor.translateAlternateColorCodes('&', cfg.getString("LAYOUT.IPBAN").replace("%grund%", Main.ban.getReasonByID(id))));
                            } else {
                                String MSG = cfg.getString("LAYOUT.TEMPIPBAN");
                                MSG = MSG.replace("%grund%", Main.ban.getReasonString(UUID));
                                MSG = MSG.replace("%dauer%", Main.ban.getEnd(UUID));
                                e.setCancelReason(ChatColor.translateAlternateColorCodes('&', MSG));
                            }
                        }
                    }
                }
            }
        } catch (IOException er){
            er.printStackTrace();
        }
        if(Main.ip.isBanned(IP)){
            try {
                Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);

                if(Main.ip.getRAWEnd(IP) == -1L){
                    e.setCancelled(true);
                    e.setCancelReason(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.IPBAN").replace("%grund%", Main.ip.getReasonString(IP))));
                } else {
                    if(System.currentTimeMillis() < Main.ip.getRAWEnd(IP)){
                        e.setCancelled(true);
                        String MSG = configcfg.getString("LAYOUT.TEMPIPBAN");
                        MSG = MSG.replace("%grund%", Main.ip.getReasonString(IP));
                        MSG = MSG.replace("%dauer%", Main.ip.getEnd(IP));
                        e.setCancelReason(ChatColor.translateAlternateColorCodes('&', MSG));
                    } else {
                        Main.ip.unban(IP);
                    }
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        if(Main.ban.isBanned(UUID)){
            try {
                Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);

                if(Main.ban.getRAWEnd(UUID) == -1L){
                    e.setCancelled(true);
                    e.setCancelReason(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.BAN").replace("%grund%", Main.ban.getReasonString(UUID)).replace("%ea-status%", Main.ban.getEAStatus(UUID))));
                } else {
                    if(System.currentTimeMillis() < Main.ban.getRAWEnd(UUID)){
                        e.setCancelled(true);
                        String MSG = configcfg.getString("LAYOUT.TEMPBAN");
                        MSG = MSG.replace("%grund%", Main.ban.getReasonString(UUID));
                        MSG = MSG.replace("%dauer%", Main.ban.getEnd(UUID));
                        MSG = MSG.replace("%ea-status%", Main.ban.getEAStatus(UUID));
                        e.setCancelReason(ChatColor.translateAlternateColorCodes('&', MSG));
                    } else {
                        Main.ban.unban(UUID);
                    }
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onFinalLogin(PostLoginEvent e){
        ProxiedPlayer p = e.getPlayer();
        if(p.hasPermission("professionalbans.reports") || p.hasPermission("professionalbans.*")){
            if(Main.ban.countOpenReports() != 0){
                p.sendMessage(Main.data.Prefix+Main.messages.getString("open_report_notify").replace("%count%", Main.ban.countOpenReports()+""));
            }
        }
        if(p.hasPermission("professionalbans.supportchat") || p.hasPermission("professionalbans.*")){
            if(SupportChat.openchats.size() != 0){
                p.sendMessage(Main.data.Prefix+Main.messages.getString("open_support_notify").replace("%count%", SupportChat.openchats.size()+""));
            }
        }
        //Update Check
        if(p.hasPermission("professionalbans.*")){
            if(!Main.callURL("https://api.spigotmc.org/legacy/update.php?resource=63657").equals(Main.Version)){
                p.sendMessage("§8[]===================================[]");
                p.sendMessage("§e§lProfessionalBans §7Reloaded §8| §7Version §c"+Main.Version);
                p.sendMessage(Main.messages.getString("update"));
                p.sendMessage("§7Update: §4§lhttps://spigotmc.org/resources/63657");
                p.sendMessage("§8[]===================================[]");
            }
        }
        //WebURL Conf Check
        if(Main.data.WebURL == null || Main.data.WebURL == "https://bans.YourServer.com"){
            p.sendMessage("§8[]===================================[]");
            p.sendMessage(Main.messages.getString("config_notify"));
            p.sendMessage("§8[]===================================[]");
        }
        //Language Check
        if(!Language.isLanguageSet()){
            p.sendMessage("§8[]===================================[]");

            p.sendMessage("§6§lProfessional§e§lBans §7by §bTutorialwork");
            p.sendMessage("§7Please select a language.");

            TextComponent en = new TextComponent();
            en.setText("§8• §cEnglish");
            en.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/language en"));
            en.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click to set language to §cEnglish").create()));

            TextComponent de = new TextComponent();
            de.setText("§8• §aGerman");
            de.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/language de"));
            de.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click to set language to §aGerman").create()));

            p.sendMessage(en);
            p.sendMessage(de);

            p.sendMessage("§8[]===================================[]");
        }

        TimeManager.updateOnlineStatus(p.getUniqueId().toString(), 1);
        logintimes.put(p, System.currentTimeMillis());
    }
}
