package de.tutorialwork.professionalbans.utils;

import de.tutorialwork.professionalbans.main.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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

    public static String formatDate(long time){
        Date date = new Date(time);
        SimpleDateFormat df2 = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String dateText = df2.format(date);
        return dateText;
    }

    public static String formatOnlineTime(long time){
        return String.format("%h Stunden, %d Minuten und %d Sekunden",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
        );
    }

}
