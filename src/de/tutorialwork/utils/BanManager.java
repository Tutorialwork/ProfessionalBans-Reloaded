package de.tutorialwork.utils;

import de.tutorialwork.main.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class BanManager {

    //DATENBANK Struktur
    //UUID varchar(64) UNIQUE, NAME varchar(64), BANNED int(11), MUTED int(11), REASON varchar(64), END long(255), TEAMUUID varchar(64), BANS int(11), MUTES int(11)

    public static boolean playerExists(String UUID){
        try {

            ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE UUID='" + UUID + "'");
            if(rs.next()){
                return rs.getString("UUID") != null;
            }

        } catch (SQLException exc){

        }

        return false;

    }

    public static void createPlayer(String UUID, String Name){
        if(!playerExists(UUID)){
            Main.mysql.update("INSERT INTO bans(UUID, NAME, BANNED, MUTED, REASON, END, TEAMUUID, BANS, MUTES, FIRSTLOGIN, LASTLOGIN) " +
                    "VALUES ('" + UUID + "', '" + Name + "', '0', '0', 'null', 'null', 'null', '0', '0', '" + System.currentTimeMillis() + "', '" + System.currentTimeMillis() + "')");
        } else {
            updateName(UUID, Name);
            updateLastLogin(UUID);
        }
    }

    public static void getBanReasonsList(ProxiedPlayer p){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reasons ORDER BY SORTINDEX ASC");
            while(rs.next()){
                int id = rs.getInt("ID");
                if(BanManager.isBanReason(id)){
                    p.sendMessage("§7"+id+" §8| §e"+BanManager.getReasonByID(id));
                } else {
                    p.sendMessage("§7"+id+" §8| §e"+BanManager.getReasonByID(id)+" §8(§cMUTE§8)");
                }
            }
        } catch (SQLException exc){ }
    }

    public static String getNameByUUID(String UUID){
        if(playerExists(UUID)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE UUID='" + UUID + "'");
                if(rs.next()){
                    return rs.getString("NAME");
                }
            } catch (SQLException exc){

            }
        }
        return null;
    }

    public static String getUUIDByName(String Name){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE NAME='" + Name + "'");
            if(rs.next()){
                return rs.getString("UUID");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static void updateName(String UUID, String newName){
        if(playerExists(UUID)){
            Main.mysql.update("UPDATE bans SET NAME='" + newName + "' WHERE UUID='" + UUID + "'");
        }
    }

    public static void ban(String UUID, int GrundID, String TeamUUID, int Prozentsatz, boolean increaseBans){
        if(getReasonTime(GrundID) == -1){
            //Perma Ban
            Main.mysql.update("UPDATE bans SET BANNED='1', REASON='" + getReasonByID(GrundID) + "', END='-1', TEAMUUID='" + TeamUUID + "' WHERE UUID='" + UUID + "'");
        } else {
            //Temp Ban
            //Formel: 1.50 * Anzahl an Tagen = Ergebniss (50%)
            int bans = getBans(UUID);
            int defaultmins = getReasonTime(GrundID);
            long current = System.currentTimeMillis();
            long end = current + getReasonTime(GrundID) * 60000L;
            long increaseEnd = current + Prozentsatz / 100 + 1 * defaultmins * bans * 60000L; //Formel!!!!!
            if(increaseBans){
                if(bans > 0){
                    Main.mysql.update("UPDATE bans SET BANNED='1', REASON='" + getReasonByID(GrundID) + "', END='" + end + "', TEAMUUID='" + TeamUUID + "' WHERE UUID='" + UUID + "'");
                } else {
                    Main.mysql.update("UPDATE bans SET BANNED='1', REASON='" + getReasonByID(GrundID) + "', END='" + increaseEnd + "', TEAMUUID='" + TeamUUID + "' WHERE UUID='" + UUID + "'");
                }
            } else {
                Main.mysql.update("UPDATE bans SET BANNED='1', REASON='" + getReasonByID(GrundID) + "', END='" + end + "', TEAMUUID='" + TeamUUID + "' WHERE UUID='" + UUID + "'");
            }
        }
    }

    public static void mute(String UUID, int GrundID, String TeamUUID){
        long current = System.currentTimeMillis();
        long end = current + getReasonTime(GrundID) * 60000L;
        if(getReasonTime(GrundID) == -1){
            //Perma Mute
            Main.mysql.update("UPDATE bans SET MUTED='1', REASON='" + getReasonByID(GrundID) + "', END='-1', TEAMUUID='" + TeamUUID + "' WHERE UUID='" + UUID + "'");
        } else {
            //Temp Mute
            Main.mysql.update("UPDATE bans SET MUTED='1', REASON='" + getReasonByID(GrundID) + "', END='" + end + "', TEAMUUID='" + TeamUUID + "' WHERE UUID='" + UUID + "'");
        }
    }

    public static Long getRAWEnd(String UUID){
        if(playerExists(UUID)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE UUID='" + UUID + "'");
                if(rs.next()){
                    return rs.getLong("END");
                }
            } catch (SQLException exc){

            }
        }
        return null;
    }

    public static String getEnd(String UUID) {
        long uhrzeit = System.currentTimeMillis();
        long end = getRAWEnd(UUID);

        long millis = end - uhrzeit;

        long sekunden = 0L;
        long minuten = 0L;
        long stunden = 0L;
        long tage = 0L;
        while (millis > 1000L)
        {
            millis -= 1000L;
            sekunden += 1L;
        }
        while (sekunden > 60L)
        {
            sekunden -= 60L;
            minuten += 1L;
        }
        while (minuten > 60L)
        {
            minuten -= 60L;
            stunden += 1L;
        }
        while (stunden > 24L)
        {
            stunden -= 24L;
            tage += 1L;
        }
        if(tage != 0){
            return "§a" + tage + " §7Tag(e) §a" + stunden + " §7Stunde(n) §a" + minuten + " §7Minute(n)";
        } else if(tage == 0 && stunden != 0){
            return "§a" + stunden + " §7Stunde(n) §a" + minuten + " §7Minute(n) §a" + sekunden + " §7Sekunde(n)";
        } else if(tage == 0 && stunden == 0 && minuten != 0){
            return "§a" + minuten + " §7Minute(n) §a" + sekunden + " §7Sekunde(n)";
        } else if(tage == 0 && stunden == 0 && minuten == 0 && sekunden != 0) {
            return "§a" + sekunden + " §7Sekunde(n)";
        } else {
            return "§4Fehler in der Berechnung!";
        }
        //Alter Code
        //return "§a" + tage + " §7Tag(e) §a" + stunden + " §7Stunde(n) §a" + minuten + " §7Minute(n) §a" + sekunden + " §7Sekunde(n)";
    }

    public static boolean isBanned(String UUID){
        if(playerExists(UUID)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE UUID='" + UUID + "'");
                if(rs.next()){
                    if(rs.getInt("BANNED") == 1){
                        return true;
                    } else {
                        return false;
                    }
                }
            } catch (SQLException exc){

            }
        }
        return false;
    }

    public static boolean isMuted(String UUID){
        if(playerExists(UUID)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE UUID='" + UUID + "'");
                if(rs.next()){
                    if(rs.getInt("MUTED") == 1){
                        return true;
                    } else {
                        return false;
                    }
                }
            } catch (SQLException exc){

            }
        }
        return false;
    }

    public static void unban(String UUID){
        if(playerExists(UUID)){
            Main.mysql.update("UPDATE bans SET BANNED='0' WHERE UUID='" + UUID + "'");
        }
    }

    public static void unmute(String UUID){
        if(playerExists(UUID)){
            Main.mysql.update("UPDATE bans SET MUTED='0' WHERE UUID='" + UUID + "'");
        }
    }

    public static String getReasonString(String UUID){
        if(playerExists(UUID)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE UUID='" + UUID + "'");
                if(rs.next()){
                    return rs.getString("REASON");
                }
            } catch (SQLException exc){

            }
        }
        return null;
    }

    public static void sendNotify(String Type, String BannedName, String TeamName, String Grund){
        if(Type.toUpperCase().equals("BAN")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify")){
                    all.sendMessage(Main.Prefix+"§e§l"+BannedName+" §7wurde von §c§l"+TeamName+" §cgebannt §7wegen §a"+Grund);
                }
            }
        }
        if(Type.toUpperCase().equals("IPBAN")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify")){
                    all.sendMessage(Main.Prefix+"§7Die IP §e§l"+BannedName+" §7wurde von §c§l"+TeamName+" §cgebannt §7wegen §a"+Grund);
                }
            }
        }
        if(Type.toUpperCase().equals("MUTE")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.Prefix+"§e§l"+BannedName+" §7wurde von §c§l"+TeamName+" §cgemutet §7wegen §a"+Grund);
                }
            }
        }
        if(Type.toUpperCase().equals("AUTOMUTE")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.Prefix+"§e§l"+BannedName+" §7wurde §cautomatisch gemutet §7wegen §a"+Grund+" §8(§7"+TeamName+"§8)");
                }
            }
        }
        if(Type.toUpperCase().equals("KICK")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.Prefix+"§e§l"+BannedName+" §7wurde von §c§l"+TeamName+" §cgekickt §7wegen §a"+Grund);
                }
            }
        }
        if(Type.toUpperCase().equals("UNBAN")){
                for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                    if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.Prefix+"§c§l"+TeamName+" §7hat §e§l"+BannedName+" §aentbannt");
                }
            }
        }
        if(Type.toUpperCase().equals("UNBANIP")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.Prefix+"§c§l"+TeamName+" §7hat die IP-Adresse §e§l"+BannedName+" §aentbannt");
                }
            }
        }
        if(Type.toUpperCase().equals("UNMUTE")){
                for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                    if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.Prefix+"§c§l"+TeamName+" §7hat §e§l"+BannedName+" §aentmutet");
                }
            }
        }
        if(Type.toUpperCase().equals("REPORT")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.Prefix+"§c§l"+TeamName+" §7hat §e§l"+BannedName+" §7wegen §a"+Grund+" §7gemeldet");
                }
            }
        }
    }

    public static Integer getBans(String UUID){
        if(playerExists(UUID)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE UUID='" + UUID + "'");
                if(rs.next()){
                    return rs.getInt("BANS");
                }
            } catch (SQLException exc){

            }
        }
        return null;
    }

    public static void setBans(String UUID, int Bans){
        if(playerExists(UUID)){
            Main.mysql.update("UPDATE bans SET BANS='" + Bans + "' WHERE UUID='" + UUID + "'");
        }
    }

    public static Integer getMutes(String UUID){
        if(playerExists(UUID)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE UUID='" + UUID + "'");
                if(rs.next()){
                    return rs.getInt("MUTES");
                }
            } catch (SQLException exc){

            }
        }
        return null;
    }

    public static void setMutes(String UUID, int Mutes){
        if(playerExists(UUID)){
            Main.mysql.update("UPDATE bans SET MUTES='" + Mutes + "' WHERE UUID='" + UUID + "'");
        }
    }

    public static Integer countReasons(){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reasons");
            int i = 0;
            while (rs.next()){
                i++;
            }
            return i;
        } catch (SQLException exc){

        }
        return null;
    }

    public static String getReasonByID(int Reason){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reasons WHERE ID='" + Reason + "'");
            if(rs.next()){
                return rs.getString("REASON");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static Integer getReasonTime(int ID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reasons WHERE ID='" + ID + "'");
            if(rs.next()){
                return rs.getInt("TIME");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static boolean isBanReason(int ID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reasons WHERE ID='" + ID + "'");
            if(rs.next()){
                if(rs.getInt("TYPE") == 0){
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException exc){

        }
        return false;
    }

    public static Integer getReasonBans(int ReasonID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reasons WHERE ID='" + ReasonID + "'");
            if(rs.next()){
                return rs.getInt("BANS");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static void setReasonBans(int ReasonID, int Bans){
        Main.mysql.update("UPDATE reasons SET BANS='" + Bans + "' WHERE ID='" + ReasonID + "'");
    }

    public static boolean hasExtraPerms(int ReasonID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reasons WHERE ID='" + ReasonID + "'");
            if(rs.next()){
                if(rs.getString("PERMS").equals("null")){
                    return false;
                } else {
                    return true;
                }
            }
        } catch (SQLException exc){

        }
        return false;
    }

    public static String getExtraPerms(int ReasonID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reasons WHERE ID='" + ReasonID + "'");
            if(rs.next()){
                return rs.getString("PERMS");
            }
        } catch (SQLException exc){

        }
        return null;
    }


    public static boolean webaccountExists(String UUID){
        try {

            ResultSet rs = Main.mysql.query("SELECT * FROM accounts WHERE UUID='" + UUID + "'");
            if(rs.next()){
                return rs.getString("UUID") != null;
            }

        } catch (SQLException exc){

        }

        return false;

    }

    public static void createWebAccount(String UUID, String Name, int Rank, String PasswordHash){
        Main.mysql.update("INSERT INTO accounts(UUID, USERNAME, PASSWORD, RANK, GOOGLE_AUTH, AUTHCODE) " +
                "VALUES ('" + UUID + "', '" + Name + "', '" + PasswordHash + "', '" + Rank + "', 'null', 'initialpassword')");
    }

    public static void deleteWebAccount(String UUID){
        Main.mysql.update("DELETE FROM accounts WHERE UUID='"+UUID+"'");
    }

    public static boolean isWebaccountAdmin(String UUID){
        if(webaccountExists(UUID)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM accounts WHERE UUID='" + UUID + "'");
                if(rs.next()){
                    if(rs.getInt("RANK") == 3){
                        return true;
                    } else {
                        return false;
                    }
                }
            } catch (SQLException exc){

            }
        } else {
            return false;
        }
        return false;
    }

    public static boolean hasAuthToken(String UUID){
        if(webaccountExists(UUID)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM accounts WHERE UUID='" + UUID + "'");
                if(rs.next()){
                    if(rs.getString("AUTHCODE") == "null"){
                        return false;
                    } else {
                        return true;
                    }
                }
            } catch (SQLException exc){

            }
        } else {
            return false;
        }
        return false;
    }

    public static String getAuthCode(String UUID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM accounts WHERE UUID='" + UUID + "'");
            if(rs.next()){
                return rs.getString("AUTHCODE");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static void updateAuthStatus(String UUID){
        Main.mysql.update("UPDATE accounts SET AUTHSTATUS = 1 WHERE UUID = '"+UUID+"'");
    }

    public static void createReport(String UUID, String ReporterUUID, String Reason, String LogID){
        Main.mysql.update("INSERT INTO reports(UUID, REPORTER, TEAM, REASON, LOG, STATUS, CREATED_AT) " +
                "VALUES ('" + UUID + "', '" + ReporterUUID + "', 'null', '" + Reason  + "', '" + LogID + "', '0', '" + System.currentTimeMillis() + "')");
    }

    public static Integer countOpenReports(){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reports WHERE STATUS = 0");
            int i = 0;
            while (rs.next()){
                i++;
            }
            return i;
        } catch (SQLException exc){

        }
        return null;
    }

    public static ArrayList getIDsFromOpenReports(){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reports WHERE STATUS = 0");
            ArrayList<Integer> ids = new ArrayList<>();
            while (rs.next()){
               ids.add(rs.getInt("ID"));
            }
            return ids;
        } catch (SQLException exc){

        }
        return null;
    }

    public static String getNameByReportID(int ReportID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reports WHERE ID='" + ReportID + "'");
            if(rs.next()){
                return getNameByUUID(rs.getString("UUID"));
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static String getReasonByReportID(int ReportID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reports WHERE ID='" + ReportID + "'");
            if(rs.next()){
                return rs.getString("REASON");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static void setReportDone(int ID){
        Main.mysql.update("UPDATE reports SET STATUS = 1 WHERE ID = "+ID);
    }

    public static void setReportTeamUUID(int ID, String UUID){
        Main.mysql.update("UPDATE reports SET TEAM = '"+UUID+"' WHERE ID = "+ID);
    }

    public static boolean isChatlogAvailable(int ID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reports WHERE ID='" + ID + "'");
            if(rs.next()){
                if(rs.getString("LOG") != "null"){
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException exc){

        }
        return false;
    }

    public static String getChatlogbyReportID(int ReportID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM reports WHERE ID='" + ReportID + "'");
            if(rs.next()){
                return rs.getString("LOG");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static void updateLastLogin(String UUID){
        Main.mysql.update("UPDATE bans SET LASTLOGIN = '" + System.currentTimeMillis() + "' WHERE UUID = '"+UUID+"'");
    }

    public static String getLastLogin(String UUID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE UUID='" + UUID + "'");
            if(rs.next()){
                return rs.getString("LASTLOGIN");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static String getFirstLogin(String UUID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM bans WHERE UUID='" + UUID + "'");
            if(rs.next()){
                return rs.getString("FIRSTLOGIN");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static String formatTimestamp(long timestamp){
        Date date = new Date(timestamp);
        SimpleDateFormat jdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return jdf.format(date);
    }

}
