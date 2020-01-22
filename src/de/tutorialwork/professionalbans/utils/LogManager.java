package de.tutorialwork.professionalbans.utils;

import de.tutorialwork.professionalbans.main.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LogManager {

    //DATABASE STRUCTURE
    //ID int(11) AUTO_INCREMENT UNIQUE, UUID varchar(255), BYUUID varchar(255), ACTION varchar(255), NOTE varchar(255), DATE varchar(255)

    //ACTION Codes
    //BAN, MUTE, ADD_WORD_BLACKLIST, DEL_WORD_BLACKLIST, CREATE_CHATLOG, IPBAN_IP, IPBAN_PLAYER, KICK, REPORT, REPORT_OFFLINE, REPORT_ACCEPT, UNBAN_IP, UNBAN_BAN, UNBAN_MUTE,
    // ADD_WEBACCOUNT, DEL_WEBACCOUNT, AUTOMUTE_ADBLACKLIST, AUTOMUTE_BLACKLIST
    //
    //UUID/BY_UUID = UUID des Spielers, null = keine Spieler verfügbar, "KONSOLE" = Befehl über Konsole ausgeführt

    public static void createEntry(String UUID, String ByUUID, String Action, String Note){
        Main.mysql.update("INSERT INTO log(UUID, BYUUID, ACTION, NOTE, DATE) " +
                "VALUES ('" + UUID + "', '" + ByUUID + "', '" + Action + "', '" + Note + "', '" + System.currentTimeMillis() + "')");
    }

    public static ArrayList getLog(String UUID){
        ArrayList<Integer> logs = new ArrayList<>();
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM log WHERE UUID='" + UUID + "' ORDER BY DATE DESC");
            while (rs.next()){
                logs.add(rs.getInt("ID"));
            }
            return logs;
        } catch (SQLException exc){

        }
        return null;
    }

    public static String getLogAction(int ID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM log WHERE ID='" + ID + "'");
            if (rs.next()){
                return rs.getString("ACTION");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static String getLogTeam(int ID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM log WHERE ID='" + ID + "'");
            if (rs.next()){
                return rs.getString("BYUUID");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static String getLogNote(int ID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM log WHERE ID='" + ID + "'");
            if (rs.next()){
                return rs.getString("NOTE");
            }
        } catch (SQLException exc){

        }
        return null;
    }

}
