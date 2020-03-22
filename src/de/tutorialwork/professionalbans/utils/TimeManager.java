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

        if(diffHours != 0 && diffDays != 0){
            if(diffDays == 1){
                return diffDays+" "+Main.messages.getString("day")+", "+diffHours+" "+Main.messages.getString("hour")+", "+diffMinutes+" "+Main.messages.getString("minutes");
            } else if(diffHours == 1){
                return diffDays+" "+Main.messages.getString("days")+", "+diffHours+" "+Main.messages.getString("hour")+", "+diffMinutes+" "+Main.messages.getString("minutes");
            } else if(diffMinutes == 1){
                return diffDays+" "+Main.messages.getString("days")+", "+diffHours+" "+Main.messages.getString("hours")+", "+diffMinutes+" "+Main.messages.getString("minute");
            } else if(diffHours == 1 && diffMinutes == 1){
                return diffDays+" "+Main.messages.getString("days")+", "+diffHours+" "+Main.messages.getString("hour")+", "+diffMinutes+" "+Main.messages.getString("minute");
            } else if(diffHours == 1 && diffMinutes == 1 && diffDays == 1){
                return diffDays+" "+Main.messages.getString("day")+", "+diffHours+" "+Main.messages.getString("hour")+", "+diffMinutes+" "+Main.messages.getString("minute");
            } else {
                return diffDays+" "+Main.messages.getString("days")+", "+diffHours+" "+Main.messages.getString("hours")+", "+diffMinutes+" "+Main.messages.getString("minutes");
            }
        } else if(diffHours != 0){
            if(diffHours == 1){
                return diffHours+" "+Main.messages.getString("hour")+", "+diffMinutes+" "+Main.messages.getString("minutes");
            } else if(diffMinutes == 1){
                return diffHours+" "+Main.messages.getString("hours")+", "+diffMinutes+" "+Main.messages.getString("minute");
            } else if(diffHours == 1 && diffMinutes == 1){
                return diffHours+" "+Main.messages.getString("hour")+", "+diffMinutes+" "+Main.messages.getString("minute");
            } else {
                return diffHours+" "+Main.messages.getString("hours")+", "+diffMinutes+" "+Main.messages.getString("minutes");
            }
        } else {
            if(diffMinutes == 1){
                return diffMinutes+" "+Main.messages.getString("minute");
            } else {
                return diffMinutes+" "+Main.messages.getString("minutes");
            }
        }
    }

}
