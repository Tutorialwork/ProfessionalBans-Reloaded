package de.tutorialwork.professionalbans.utils;

import de.tutorialwork.professionalbans.main.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeManager {

    public static void updateOnlineStatus(String UUID, Integer status){
        Main.mysql.update("UPDATE bans SET ONLINE_STATUS = "+status+" WHERE UUID = '" + UUID+"'");
    }

    public static Integer getOnlineStatus(String UUID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE UUID='" + UUID + "'");
            if(rs.next()){
                return rs.getInt("ONLINE_STATUS");
            }
        } catch (SQLException exc){

        }
        return 0;
    }

    public static void setOnlineTime(String UUID, long newTime){
        Main.mysql.update("UPDATE bans SET ONLINE_TIME = "+newTime+" WHERE UUID = '" + UUID+"'");
    }

    public static long getOnlineTime(String UUID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE UUID='" + UUID + "'");
            if(rs.next()){
                return rs.getLong("ONLINE_TIME");
            }
        } catch (SQLException exc){

        }
        return 0;
    }

    public static ArrayList<String> getTopOnlineTime(){
        ArrayList<String> players = new ArrayList<>();
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM bans ORDER BY ONLINE_TIME DESC LIMIT 10");
            while (rs.next()){
                players.add(rs.getString("UUID"));
            }
            return players;
        } catch (SQLException exc){

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

        if(diffHours != 0){
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
