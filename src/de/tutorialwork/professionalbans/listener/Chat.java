package de.tutorialwork.professionalbans.listener;

import de.tutorialwork.professionalbans.commands.SupportChat;
import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.BanManager;
import de.tutorialwork.professionalbans.utils.LogManager;
import de.tutorialwork.professionalbans.utils.MySQLConnect;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
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
            if(BanManager.isMuted(p.getUniqueId().toString())){
                File config = new File(Main.main.getDataFolder(), "config.yml");
                try {
                    Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);

                    if(BanManager.getRAWEnd(p.getUniqueId().toString()) == -1L){
                        e.setCancelled(true);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.MUTE").replace("%grund%", BanManager.getReasonString(p.getUniqueId().toString()))));
                    } else {
                        if(System.currentTimeMillis() < BanManager.getRAWEnd(p.getUniqueId().toString())){
                            e.setCancelled(true);
                            String MSG = configcfg.getString("LAYOUT.TEMPMUTE");
                            MSG = MSG.replace("%grund%", BanManager.getReasonString(p.getUniqueId().toString()));
                            MSG = MSG.replace("%dauer%", BanManager.getEnd(p.getUniqueId().toString()));
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG));
                        } else {
                            BanManager.unmute(p.getUniqueId().toString());
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
                            for(String blacklist : Main.blacklist){
                                if(e.getMessage().toUpperCase().contains(blacklist.toUpperCase())){
                                    e.setCancelled(true);
                                    BanManager.mute(p.getUniqueId().toString(), configcfg.getInt("AUTOMUTE.MUTEID"), "KONSOLE");
                                    LogManager.createEntry(p.getUniqueId().toString(), "KONSOLE", "AUTOMUTE_BLACKLIST", e.getMessage());
                                    BanManager.setMutes(p.getUniqueId().toString(), BanManager.getMutes(p.getUniqueId().toString()) + 1);
                                    BanManager.sendNotify("AUTOMUTE", BanManager.getNameByUUID(p.getUniqueId().toString()), e.getMessage(), BanManager.getReasonByID(configcfg.getInt("AUTOMUTE.MUTEID")));
                                    if(BanManager.getRAWEnd(p.getUniqueId().toString()) == -1L){
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.MUTE").replace("%grund%", BanManager.getReasonByID(configcfg.getInt("AUTOMUTE.MUTEID")))));
                                    } else {
                                        String MSG = configcfg.getString("LAYOUT.TEMPMUTE");
                                        MSG = MSG.replace("%grund%", BanManager.getReasonString(p.getUniqueId().toString()));
                                        MSG = MSG.replace("%dauer%", BanManager.getEnd(p.getUniqueId().toString()));
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG));
                                    }
                                    return;
                                }
                            }
                            for(String adblacklist : Main.adblacklist){
                                if(e.getMessage().toUpperCase().contains(adblacklist.toUpperCase())){
                                    if(!Main.adwhitelist.contains(e.getMessage().toUpperCase())){
                                        e.setCancelled(true);
                                        BanManager.mute(p.getUniqueId().toString(), configcfg.getInt("AUTOMUTE.ADMUTEID"), "KONSOLE");
                                        LogManager.createEntry(p.getUniqueId().toString(), "KONSOLE", "AUTOMUTE_ADBLACKLIST", e.getMessage());
                                        BanManager.setMutes(p.getUniqueId().toString(), BanManager.getMutes(p.getUniqueId().toString()) + 1);
                                        BanManager.sendNotify("AUTOMUTE", BanManager.getNameByUUID(p.getUniqueId().toString()), e.getMessage(), BanManager.getReasonByID(configcfg.getInt("AUTOMUTE.ADMUTEID")));
                                        if(BanManager.getRAWEnd(p.getUniqueId().toString()) == -1L){
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.MUTE").replace("%grund%", BanManager.getReasonByID(configcfg.getInt("AUTOMUTE.MUTEID")))));
                                        } else {
                                            String MSG = configcfg.getString("LAYOUT.TEMPMUTE");
                                            MSG = MSG.replace("%grund%", BanManager.getReasonString(p.getUniqueId().toString()));
                                            MSG = MSG.replace("%dauer%", BanManager.getEnd(p.getUniqueId().toString()));
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG));
                                        }
                                        return;
                                    }
                                }
                            }
                        } else {
                            insertMessage(p.getUniqueId().toString(), e.getMessage(), p.getServer().getInfo().getName());
                            if(configcfg.getBoolean("AUTOMUTE.AUTOREPORT")){
                                for(String blacklist : Main.blacklist){
                                    if(e.getMessage().toUpperCase().contains(blacklist.toUpperCase())){
                                        e.setCancelled(true);
                                        p.sendMessage(Main.Prefix+"§cAchte auf deine Wortwahl");
                                        String LogID = Chat.createChatlog(p.getUniqueId().toString(), "KONSOLE");
                                        BanManager.createReport(p.getUniqueId().toString(),"KONSOLE", "VERHALTEN", LogID);
                                        BanManager.sendNotify("REPORT", p.getName(), "KONSOLE", "VERHALTEN");
                                        return;
                                    }
                                }
                                for(String adblacklist : Main.adblacklist){
                                    if(e.getMessage().toUpperCase().contains(adblacklist.toUpperCase())){
                                        if(!Main.adwhitelist.contains(e.getMessage().toUpperCase())){
                                            e.setCancelled(true);
                                            p.sendMessage(Main.Prefix+"§cDu darfst keine Werbung machen");
                                            String LogID = Chat.createChatlog(p.getUniqueId().toString(), "KONSOLE");
                                            BanManager.createReport(p.getUniqueId().toString(),"KONSOLE", "WERBUNG", LogID);
                                            BanManager.sendNotify("REPORT", p.getName(), "KONSOLE", "WERBUNG");
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        insertMessage(p.getUniqueId().toString(), e.getMessage(), p.getServer().getInfo().getName());
                    }

                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public static void insertMessage(String UUID, String Message, String Server){
        try {
            File file = new File(Main.getInstance().getDataFolder().getPath(), "mysql.yml");
            Configuration mysql = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            Connection con = DriverManager.getConnection("jdbc:mysql://" +mysql.getString("HOST") + ":" + mysql.getInt("PORT") + "/" + mysql.getString("DATENBANK") + "?autoReconnect=true", mysql.getString("USER"), mysql.getString("PASSWORT"));
            PreparedStatement ps = con.prepareStatement("INSERT INTO chat(UUID, SERVER, MESSAGE, SENDDATE) VALUES (?, ?, ?, ?)");
            ps.setString(1, UUID);
            ps.setString(2, Server);
            ps.setString(3, Message);
            ps.setLong(4, System.currentTimeMillis());
            ps.execute();
        } catch (IOException e){
            e.printStackTrace();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static String createChatlog(String UUID, String CreatedUUID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM chat WHERE UUID='" + UUID + "'");
            String ID = randomString(20);
            Long now = System.currentTimeMillis();
            while (rs.next()){
                int TEN_MINUTES = 10 * 60 * 1000;
                long tenAgo = System.currentTimeMillis() - TEN_MINUTES;
                if (Long.valueOf(rs.getString("SENDDATE")) > tenAgo) {
                    Main.mysql.update("INSERT INTO chatlog(LOGID, UUID, CREATOR_UUID, SERVER, MESSAGE, SENDDATE, CREATED_AT) " +
                            "VALUES ('" + ID + "' ,'" + UUID + "', '" + CreatedUUID + "', '" + rs.getString("SERVER") + "', '" + rs.getString("MESSAGE") + "', '" + rs.getString("SENDDATE") + "', '" + now + "')");
                }
            }
            return ID;
        } catch (SQLException exc){

        }
        return null;
    }

    public static boolean hasMessages(String UUID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM chat WHERE UUID='" + UUID + "'");
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
