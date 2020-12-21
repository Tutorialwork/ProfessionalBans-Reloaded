package de.tutorialwork.professionalbans.utils;

import de.tutorialwork.professionalbans.main.Main;
import net.md_5.bungee.api.ProxyServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IPManager {

    //ips(IP varchar(64) UNIQUE, USED_BY varchar(64), USED_AT varchar(64), BANNED int(11), REASON varchar(64), END long, TEAMUUID varchar(64), BANS int(11));

    public boolean IPExists(String IP){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM ips WHERE IP = ?");

            ps.setString(1, IP);
            ResultSet rSet = ps.executeQuery();

            if(rSet.next()){
                return rSet.getString("IP") != null;
            }

            rSet.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void insertIP(String IP, String UUID){
        ProxyServer.getInstance().getScheduler().runAsync(Main.main, () -> {
            try{
                if(!IPExists(IP)){
                    PreparedStatement ps = Main.mysql.getCon()
                            .prepareStatement("INSERT INTO ips(IP, USED_BY, USED_AT, BANNED, REASON, END, TEAMUUID, BANS) "+
                                    "VALUES (?, ?, ?, '0', 'null', 'null', 'null', '0')");
                    ps.setString(1, IP);
                    ps.setString(2, UUID);
                    ps.setLong(3, System.currentTimeMillis());
                    ps.executeUpdate();
                    ps.close();
                } else {
                    updateIPInfos(IP, UUID);
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        });
    }

    public void updateIPInfos(String IP, String newUUID){
        ProxyServer.getInstance().getScheduler().runAsync(Main.main, () -> {
            try{
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("UPDATE ips SET USED_BY = ?, USED_AT= ? WHERE IP= ?");
                ps.setString(1, newUUID);
                ps.setLong(2, System.currentTimeMillis());
                ps.setString(3, IP);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        });
    }

    public void ban(String IP, int GrundID, String TeamUUID){
        try {
            long current = System.currentTimeMillis();
            long end = current + Main.ban.getReasonTime(GrundID) * 60000L;
            if(Main.ban.getReasonTime(GrundID) == -1){
                //Perma Ban
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("UPDATE ips SET BANNED='1', REASON=?, END='-1', TEAMUUID=? WHERE IP=?");
                ps.setString(1, Main.ban.getReasonByID(GrundID));
                ps.setString(2, TeamUUID);
                ps.setString(3, IP);
                ps.executeUpdate();
                ps.close();
            } else {
                //Temp Ban
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("UPDATE ips SET BANNED='1', REASON=?, END=?, TEAMUUID=? WHERE IP=?");
                ps.setString(1, Main.ban.getReasonByID(GrundID));
                ps.setLong(2, end);
                ps.setString(3, TeamUUID);
                ps.setString(4, IP);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }


    public boolean isBanned(String IP){
        if(IPExists(IP)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM ips WHERE IP=?");
                ps.setString(1, IP);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    if(rs.getInt("BANNED") == 1){
                        return true;
                    } else {
                        return false;
                    }
                }
                ps.close();
                rs.close();
            } catch (SQLException exc){
                exc.printStackTrace();
            }
        }
        return false;
    }

    public String getReasonString(String IP){
        if(IPExists(IP)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM ips WHERE IP=?");
                ps.setString(1, IP);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getString("REASON");
                }
                rs.close();
                ps.close();
            } catch (SQLException exc){
                exc.printStackTrace();
            }
        }
        return null;
    }

    public Long getRAWEnd(String IP){
        if(IPExists(IP)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM ips WHERE IP=?");
                ps.setString(1, IP);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getLong("END");
                }
                rs.close();
                ps.close();
            } catch (SQLException exc){
                exc.printStackTrace();
            }
        }
        return null;
    }

    public String getEnd(String UUID) {
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

    public void unban(String IP){
        if(IPExists(IP)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("UPDATE ips SET BANNED='0' WHERE IP=?");
                ps.setString(1, IP);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    public boolean isVPN(String IP){
        if(!IP.equals("127.0.0.1")){
            if(Main.data.APIKey != null){
                String json = Main.callURL("http://proxycheck.io/v2/"+IP.replace("%0", "")+"?key="+Main.data.APIKey);
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
                String json = Main.callURL("http://proxycheck.io/v2/"+IP.replace("%0", "")+"?key=318n07-0o7054-y9y82a-75o3hr");
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

    public String getIPFromPlayer(String UUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM ips WHERE USED_BY=?");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("IP");
            }
        } catch (SQLException exc){

        }
        return null;
    }

    public String getPlayerFromIP(String IP){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM ips WHERE IP=?");
            ps.setString(1, IP);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("USED_BY");
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public Integer getBans(String IP){
        if(IPExists(IP)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM ips WHERE IP=?");
                ps.setString(1, IP);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getInt("BANS");
                }
                rs.close();
                ps.close();
            } catch (SQLException exc){
                exc.printStackTrace();
            }
        }
        return null;
    }

    public void setBans(String IP, int Bans){
        try{
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("UPDATE ips SET BANS=? WHERE IP=?");
            ps.setInt(1, Bans);
            ps.setString(2, IP);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void addBan(String IP){
        setBans(IP, getBans(IP) + 1);
    }

    public long getLastUseLong(String IP){
        if(IPExists(IP)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM ips WHERE IP=?");
                ps.setString(1, IP);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return Long.valueOf(rs.getString("USED_AT"));
                }
                ps.close();
                rs.close();
            } catch (SQLException exc){
                exc.printStackTrace();
            }
        }
        return 0;
    }

}
