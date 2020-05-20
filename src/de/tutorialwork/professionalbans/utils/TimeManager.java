package de.tutorialwork.professionalbans.utils;

import de.tutorialwork.professionalbans.main.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TimeManager {

    public static void updateOnlineStatus(String UUID, Integer status){
        try{
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("UPDATE bans SET ONLINE_STATUS = ? WHERE UUID = ?");
            ps.setInt(1, status);
            ps.setString(2, UUID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static Integer getOnlineStatus(String UUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM bans WHERE UUID=?");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt("ONLINE_STATUS");
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return 0;
    }

    public static void setOnlineTime(String UUID, long newTime){
        try{
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("UPDATE bans SET ONLINE_TIME = ? WHERE UUID = ?");
            ps.setLong(1, newTime);
            ps.setString(2, UUID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static long getOnlineTime(String UUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM bans WHERE UUID=?");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getLong("ONLINE_TIME");
            }
            rs.close();
            ps.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return 0;
    }

    public static ArrayList<String> getTopOnlineTime(){
        ArrayList<String> players = new ArrayList<>();
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM bans ORDER BY ONLINE_TIME DESC LIMIT 10");
            ResultSet rs = ps.executeQuery();
            try{
                File file = new File(Main.main.getDataFolder(), "config.yml");
                Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

                while (rs.next()){
                    if(cfg.getBoolean("ONLINETIME.BYPASSTEAM")){
                        if(!Main.ban.webaccountExists(rs.getString("UUID"))){
                            players.add(rs.getString("UUID"));
                        }
                    } else {
                        players.add(rs.getString("UUID"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            ps.close();
            rs.close();
            return players;
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return new ArrayList<String>();
    }

    public static String formatDate(long time){
        Date date = new Date(time);
        SimpleDateFormat df2 = new SimpleDateFormat(Main.messages.getString("date_format"));
        String dateText = df2.format(date);
        return dateText;
    }

    public static String formatOnlineTime(long time){
        long diffMinutes = ( time ) % 60;
        long diffHours = ( time / 60 ) % 24;
        long diffDays = ( time / 60 / 24 ) % 365;

        String timeString = "";

        if ( diffDays != 0 )
            timeString += buildTimeSnippet( diffDays, "day", "days" ) + " ";

        if ( diffHours != 0 )
            timeString += buildTimeSnippet( diffHours, "hour", "hours" ) + " ";

        if ( diffMinutes != 0 )
            timeString += buildTimeSnippet( diffMinutes, "minute", "minutes" ) + " ";

        return timeString;
    }

    private static String buildTimeSnippet(long diffUnit, String messageSingular, String messagePlural){
        return (diffUnit != 1) ? diffUnit + " " + Main.messages.getString(messagePlural) : diffUnit + " " + Main.messages.getString(messageSingular);
    }

}
