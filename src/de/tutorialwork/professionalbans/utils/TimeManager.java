package de.tutorialwork.professionalbans.utils;

import de.tutorialwork.professionalbans.main.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
            while (rs.next()){
                players.add(rs.getString("UUID"));
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
        long diff = time;

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        String timeStr = "";

        if(diffDays != 0){
            timeStr += buildTimeSnippet(diffDays, "day", "days") + " ";
        }
        if(diffHours != 0){
            timeStr += buildTimeSnippet(diffHours, "hour", "hours") + " ";
        }
        if(diffMinutes != 0){
            timeStr += buildTimeSnippet(diffMinutes, "minute", "minutes") + " ";
        }
        if(diffSeconds != 0){
            timeStr += buildTimeSnippet(diffSeconds, "second", "seconds") + " ";
        }

        return timeStr;
    }

    private static String buildTimeSnippet(long diffUnit, String messageSingular, String messagePlural){
        return (diffUnit != 1) ? diffUnit + " " + Main.messages.getString(messagePlural) : diffUnit + " " + Main.messages.getString(messageSingular);
    }

}
