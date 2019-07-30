package de.tutorialwork.utils;

import de.tutorialwork.main.Main;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IPManager {

    //ips(IP varchar(64) UNIQUE, USED_BY varchar(64), USED_AT varchar(64), BANNED int(11), REASON varchar(64), END long, TEAMUUID varchar(64), BANS int(11));

    public static boolean IPExists(String IP){
        try {

            ResultSet rs = Main.mysql.query("SELECT * FROM ips WHERE IP='" + IP + "'");
            if(rs.next()){
                return rs.getString("IP") != null;
            }

        } catch (SQLException exc){

        }

        return false;

    }

    public static void insertIP(String IP, String UUID){
        if(!IPExists(IP)){
            Main.mysql.update("INSERT INTO ips(IP, USED_BY, USED_AT, BANNED, REASON, END, TEAMUUID, BANS) " +
                    "VALUES ('" + IP + "', '" + UUID + "', '" + System.currentTimeMillis() + "', '0', 'null', 'null', 'null', '0')");
        } else {
            updateIPInfos(IP, UUID);
        }
    }

    public static void updateIPInfos(String IP, String newUUID){
        if(IPExists(IP)){
            Main.mysql.update("UPDATE ips SET USED_BY = '"+newUUID+"', USED_AT='" + System.currentTimeMillis() + "' WHERE IP='" + IP + "'");
        }
    }

    public static void ban(String IP, int GrundID, String TeamUUID){
        long current = System.currentTimeMillis();
        long end = current + BanManager.getReasonTime(GrundID) * 60000L;
        if(BanManager.getReasonTime(GrundID) == -1){
            //Perma Ban
            Main.mysql.update("UPDATE ips SET BANNED='1', REASON='" + BanManager.getReasonByID(GrundID) + "', END='-1', TEAMUUID='" + TeamUUID + "' WHERE IP='" + IP + "'");
        } else {
            //Temp Ban
            Main.mysql.update("UPDATE ips SET BANNED='1', REASON='" + BanManager.getReasonByID(GrundID) + "', END='" + end + "', TEAMUUID='" + TeamUUID + "' WHERE IP='" + IP + "'");
        }
    }


    public static boolean isBanned(String IP){
        if(IPExists(IP)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM ips WHERE IP='" + IP + "'");
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

    public static String getReasonString(String IP){
        if(IPExists(IP)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM ips WHERE IP='" + IP + "'");
                if(rs.next()){
                    return rs.getString("REASON");
                }
            } catch (SQLException exc){

            }
        }
        return null;
    }

    public static Long getRAWEnd(String IP){
        if(IPExists(IP)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM ips WHERE IP='" + IP + "'");
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

    public static void unban(String IP){
        if(IPExists(IP)){
            Main.mysql.update("UPDATE ips SET BANNED='0' WHERE IP='" + IP + "'");
        }
    }

    public static boolean isVPN(String IP){
        if(!IP.equals("127.0.0.1")){
            if(Main.APIKey != null){
                String json = Main.callURL("http://proxycheck.io/v2/"+IP+"?key="+Main.APIKey);
                json = json.replace("{\n" +
                        "    \"status\": \"ok\",\n" +
                        "    \""+IP+"\": {\n" +
                        "        \"proxy\": \"", "");
                json = json.replace("\"\n" +
                        "    }\n" +
                        "}", "");
                if(json.equals("yes")){
                    return true;
                } else {
                    return false;
                }
            } else {
                String json = Main.callURL("http://proxycheck.io/v2/"+IP+"?key=318n07-0o7054-y9y82a-75o3hr");
                json = json.replace("{\n" +
                        "    \"status\": \"ok\",\n" +
                        "    \""+IP+"\": {\n" +
                        "        \"proxy\": \"", "");
                json = json.replace("\"\n" +
                        "    }\n" +
                        "}", "");
                if(json.equals("yes")){
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public static String getIPFromPlayer(String UUID){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM ips WHERE USED_BY='" + UUID + "'");
            if(rs.next()){
                return rs.getString("IP");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static String getPlayerFromIP(String IP){
        try {
            ResultSet rs = Main.mysql.query("SELECT * FROM ips WHERE IP='" + IP + "'");
            if(rs.next()){
                return rs.getString("USED_BY");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public static Integer getBans(String IP){
        if(IPExists(IP)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM ips WHERE IP='" + IP + "'");
                if(rs.next()){
                    return rs.getInt("BANS");
                }
            } catch (SQLException exc){

            }
        }
        return null;
    }

    public static void setBans(String IP, int Bans){
        if(IPExists(IP)){
            Main.mysql.update("UPDATE ips SET BANS='" + Bans + "' WHERE IP='" + IP + "'");
        }
    }

    public static void addBan(String IP){
        setBans(IP, getBans(IP) + 1);
    }

    public static long getLastUseLong(String IP){
        if(IPExists(IP)){
            try {
                ResultSet rs = Main.mysql.query("SELECT * FROM ips WHERE IP='" + IP + "'");
                if(rs.next()){
                    return Long.valueOf(rs.getString("USED_AT"));
                }
            } catch (SQLException exc){

            }
        }
        return 0;
    }

}
