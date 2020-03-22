package de.tutorialwork.professionalbans.listener;

import de.tutorialwork.professionalbans.commands.SupportChat;
import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.LogManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.*;

public class Chat implements Listener {

    @EventHandler
    public void onChat(ChatEvent e){
        ProxiedPlayer p = (ProxiedPlayer) e.getSender();
        if(!e.getMessage().startsWith("/")){
            if(SupportChat.activechats.containsKey(p)){
                e.setCancelled(true);
                ProxiedPlayer target = SupportChat.activechats.get(p);
                target.sendMessage("§9§lSUPPORT §8• §c"+p.getName()+" §8» "+e.getMessage());
                p.sendMessage("§9§lSUPPORT §8• §aDu §8» "+e.getMessage());
            }
            if(SupportChat.activechats.containsValue(p)){
                e.setCancelled(true);
                for(ProxiedPlayer key : SupportChat.activechats.keySet()){
                    //Key has started the support chat
                    key.sendMessage("§9§lSUPPORT §8• §c"+p.getName()+" §8» "+e.getMessage());
                }
                p.sendMessage("§9§lSUPPORT §8• §aDu §8» "+e.getMessage());
            }
            if(Main.ban.isMuted(p.getUniqueId().toString())){
                File config = new File(Main.main.getDataFolder(), "config.yml");
                try {
                    Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);

                    if(Main.ban.getRAWEnd(p.getUniqueId().toString()) == -1L){
                        e.setCancelled(true);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.MUTE").replace("%grund%", Main.ban.getReasonString(p.getUniqueId().toString()))));
                    } else {
                        if(System.currentTimeMillis() < Main.ban.getRAWEnd(p.getUniqueId().toString())){
                            e.setCancelled(true);
                            String MSG = configcfg.getString("LAYOUT.TEMPMUTE");
                            MSG = MSG.replace("%grund%", Main.ban.getReasonString(p.getUniqueId().toString()));
                            MSG = MSG.replace("%dauer%", Main.ban.getEnd(p.getUniqueId().toString()));
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG));
                        } else {
                            Main.ban.unmute(p.getUniqueId().toString());
                        }
                    }

                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            } else {
                File config = new File(Main.main.getDataFolder(), "config.yml");
                try {
                    Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);

                    if(!p.hasPermission("professionalbans.blacklist.bypass") || !p.hasPermission("professionalbans.*")){
                        if(configcfg.getBoolean("AUTOMUTE.ENABLED")){
                            insertMessage(p.getUniqueId().toString(), e.getMessage(), p.getServer().getInfo().getName());
                            for(String blacklist : Main.data.blacklist){
                                if(e.getMessage().toUpperCase().contains(blacklist.toUpperCase())){
                                    e.setCancelled(true);
                                    Main.ban.mute(p.getUniqueId().toString(), configcfg.getInt("AUTOMUTE.MUTEID"), "KONSOLE");
                                    LogManager.createEntry(p.getUniqueId().toString(), "KONSOLE", "AUTOMUTE_BLACKLIST", e.getMessage());
                                    Main.ban.setMutes(p.getUniqueId().toString(), Main.ban.getMutes(p.getUniqueId().toString()) + 1);
                                    Main.ban.sendNotify("AUTOMUTE", Main.ban.getNameByUUID(p.getUniqueId().toString()), e.getMessage(), Main.ban.getReasonByID(configcfg.getInt("AUTOMUTE.MUTEID")));
                                    if(Main.ban.getRAWEnd(p.getUniqueId().toString()) == -1L){
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.MUTE").replace("%grund%", Main.ban.getReasonByID(configcfg.getInt("AUTOMUTE.MUTEID")))));
                                    } else {
                                        String MSG = configcfg.getString("LAYOUT.TEMPMUTE");
                                        MSG = MSG.replace("%grund%", Main.ban.getReasonString(p.getUniqueId().toString()));
                                        MSG = MSG.replace("%dauer%", Main.ban.getEnd(p.getUniqueId().toString()));
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG));
                                    }
                                    return;
                                }
                            }
                            for(String adblacklist : Main.data.adblacklist){
                                if(e.getMessage().toUpperCase().contains(adblacklist.toUpperCase())){
                                    if(!Main.data.adwhitelist.contains(e.getMessage().toUpperCase())){
                                        e.setCancelled(true);
                                        Main.ban.mute(p.getUniqueId().toString(), configcfg.getInt("AUTOMUTE.ADMUTEID"), "KONSOLE");
                                        LogManager.createEntry(p.getUniqueId().toString(), "KONSOLE", "AUTOMUTE_ADBLACKLIST", e.getMessage());
                                        Main.ban.setMutes(p.getUniqueId().toString(), Main.ban.getMutes(p.getUniqueId().toString()) + 1);
                                        Main.ban.sendNotify("AUTOMUTE", Main.ban.getNameByUUID(p.getUniqueId().toString()), e.getMessage(), Main.ban.getReasonByID(configcfg.getInt("AUTOMUTE.ADMUTEID")));
                                        if(Main.ban.getRAWEnd(p.getUniqueId().toString()) == -1L){
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.MUTE").replace("%grund%", Main.ban.getReasonByID(configcfg.getInt("AUTOMUTE.MUTEID")))));
                                        } else {
                                            String MSG = configcfg.getString("LAYOUT.TEMPMUTE");
                                            MSG = MSG.replace("%grund%", Main.ban.getReasonString(p.getUniqueId().toString()));
                                            MSG = MSG.replace("%dauer%", Main.ban.getEnd(p.getUniqueId().toString()));
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG));
                                        }
                                        return;
                                    }
                                }
                            }
                        } else {
                            insertMessage(p.getUniqueId().toString(), e.getMessage(), p.getServer().getInfo().getName());
                            if(configcfg.getBoolean("AUTOMUTE.AUTOREPORT")){
                                for(String blacklist : Main.data.blacklist){
                                    if(e.getMessage().toUpperCase().contains(blacklist.toUpperCase())){
                                        e.setCancelled(true);
                                        p.sendMessage(Main.data.Prefix+"§cAchte auf deine Wortwahl");
                                        String LogID = Chat.createChatlog(p.getUniqueId().toString(), "KONSOLE");
                                        Main.ban.createReport(p.getUniqueId().toString(),"KONSOLE", "VERHALTEN", LogID);
                                        Main.ban.sendNotify("REPORT", p.getName(), "KONSOLE", "VERHALTEN");
                                        return;
                                    }
                                }
                                for(String adblacklist : Main.data.adblacklist){
                                    if(e.getMessage().toUpperCase().contains(adblacklist.toUpperCase())){
                                        if(!Main.data.adwhitelist.contains(e.getMessage().toUpperCase())){
                                            e.setCancelled(true);
                                            p.sendMessage(Main.data.Prefix+"§cDu darfst keine Werbung machen");
                                            String LogID = Chat.createChatlog(p.getUniqueId().toString(), "KONSOLE");
                                            Main.ban.createReport(p.getUniqueId().toString(),"KONSOLE", "WERBUNG", LogID);
                                            Main.ban.sendNotify("REPORT", p.getName(), "KONSOLE", "WERBUNG");
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        ProxyServer.getInstance().getScheduler().runAsync(Main.main, () -> {
                            insertMessage(p.getUniqueId().toString(), e.getMessage(), p.getServer().getInfo().getName());
                        });
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public static void insertMessage(String UUID, String Message, String Server){
        try {
            PreparedStatement ps = Main.mysql.getCon().prepareStatement("INSERT INTO chat(UUID, SERVER, MESSAGE, SENDDATE) VALUES (?, ?, ?, ?)");
            ps.setString(1, UUID);
            ps.setString(2, Server);
            ps.setString(3, Message);
            ps.setLong(4, System.currentTimeMillis());
            ps.execute();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static String createChatlog(String UUID, String CreatedUUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM chat WHERE UUID=?");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            String ID = randomString(20);
            Long now = System.currentTimeMillis();
            while (rs.next()){
                int TEN_MINUTES = 10 * 60 * 1000;
                long tenAgo = System.currentTimeMillis() - TEN_MINUTES;
                if (Long.valueOf(rs.getString("SENDDATE")) > tenAgo) {
                    ProxyServer.getInstance().getScheduler().runAsync(Main.main, () -> {
                        try{
                            PreparedStatement preparedStatement = Main.mysql.getCon()
                                    .prepareStatement("INSERT INTO chatlog(LOGID, UUID, CREATOR_UUID, SERVER, MESSAGE, SENDDATE, CREATED_AT) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?)");
                            preparedStatement.setString(1, ID);
                            preparedStatement.setString(2, UUID);
                            preparedStatement.setString(3, CreatedUUID);
                            preparedStatement.setString(4, rs.getString("SERVER"));
                            preparedStatement.setString(5, rs.getString("MESSAGE"));
                            preparedStatement.setString(6, rs.getString("SENDDATE"));
                            preparedStatement.setLong(7, now);
                            preparedStatement.executeUpdate();
                            preparedStatement.close();
                        } catch (SQLException e){
                            e.printStackTrace();
                        }
                    });
                }
            }
            return ID;
        } catch (SQLException exc){

        }
        return null;
    }

    public static boolean hasMessages(String UUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM chat WHERE UUID=?");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            int i = 0;
            while (rs.next()){
                i++;
            }
            if(i != 0){
                return true;
            } else {
                return false;
            }
        } catch (SQLException exc){

        }
        return false;
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    public static String randomString(int len){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

}
