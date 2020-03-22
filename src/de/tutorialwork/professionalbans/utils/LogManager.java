package de.tutorialwork.professionalbans.utils;

import de.tutorialwork.professionalbans.main.Main;

import java.sql.PreparedStatement;
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
        try {
            PreparedStatement ps = Main.mysql.getCon()
                .prepareStatement("INSERT INTO log(UUID, BYUUID, ACTION, NOTE, DATE) " +
                    "VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, UUID);
            ps.setString(2, ByUUID);
            ps.setString(3, Action);
            ps.setString(4, Note);
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static ArrayList getLog(String UUID){
        ArrayList<Integer> logs = new ArrayList<>();
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM log WHERE UUID=? ORDER BY DATE DESC");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                logs.add(rs.getInt("ID"));
            }
            ps.close();
            rs.close();
            return logs;
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public static String getLogAction(int ID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM log WHERE ID=?");
            ps.setInt(1, ID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return rs.getString("ACTION");
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public static String getLogTeam(int ID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM log WHERE ID=?");
            ps.setInt(1, ID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return rs.getString("BYUUID");
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public static String getLogNote(int ID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM log WHERE ID=?");
            ps.setInt(1, ID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return rs.getString("NOTE");
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public static String getLogDate(int ID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM log WHERE ID=?");
            ps.setInt(1, ID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return rs.getString("DATE");
            }
            rs.close();
            ps.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

}
