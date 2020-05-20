package de.tutorialwork.professionalbans.utils;

import de.tutorialwork.professionalbans.commands.Reports;
import de.tutorialwork.professionalbans.main.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BanManager {

    public boolean playerExists(String UUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM bans WHERE UUID = ?");

            ps.setString(1, UUID);
            ResultSet rSet = ps.executeQuery();

            if(rSet.next()){
                return rSet.getString("UUID") != null;
            }

            rSet.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createPlayer(String UUID, String Name){
        ProxyServer.getInstance().getScheduler().runAsync(Main.main, () -> {
            if(!playerExists(UUID)){
                try{
                    PreparedStatement ps = Main.mysql.getCon()
                            .prepareStatement("INSERT INTO bans (UUID, NAME, BANNED, MUTED, REASON, END, TEAMUUID, BANS, MUTES, FIRSTLOGIN, LASTLOGIN) "
                            + "VALUES (?, ?, '0', '0', 'null', 'null', 'null', '0', '0', ?, ?)");
                    ps.setString(1, UUID);
                    ps.setString(2, Name);
                    ps.setLong(3, System.currentTimeMillis());
                    ps.setLong(4, System.currentTimeMillis());
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e){
                    e.printStackTrace();
                }
            } else {
                updateName(UUID, Name);
                updateLastLogin(UUID);
            }
        });
    }

    public void getBanReasonsList(ProxiedPlayer p){
        ArrayList<Integer> bans = new ArrayList<>();
        ArrayList<Integer> mutes = new ArrayList<>();

        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM reasons");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                int id = rs.getInt("ID");
                /*
                if(isBanReason(id)){
                    p.sendMessage("§7"+id+" §8| §e"+getReasonByID(id));
                } else {
                    p.sendMessage("§7"+id+" §8| §e"+getReasonByID(id)+" §8(§cMUTE§8)");
                }
                */
                if(isBanReason(id)){
                    bans.add(id);
                } else {
                    mutes.add(id);
                }
            }

            if(bans.size() != 0 || mutes.size() != 0){
                p.sendMessage("");
                p.sendMessage(Main.data.Prefix+Main.messages.getString("ban_reasons"));
                for (Integer id : bans){
                    p.sendMessage(Main.data.Prefix+id+" §8| §a"+getReasonByID(id)+" §8- §c"+getFormattedReasonTime(id));
                }
                p.sendMessage(Main.data.Prefix+Main.messages.getString("mute_reasons"));
                for (Integer id : mutes){
                    p.sendMessage(Main.data.Prefix+id+" §8| §a"+getReasonByID(id)+" §8- §c"+getFormattedReasonTime(id));
                }
                p.sendMessage("");
            } else {
                p.sendMessage(Main.data.Prefix+Main.messages.getString("no_reasons_created"));
            }

            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
    }

    public String getNameByUUID(String UUID){
        if(playerExists(UUID)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM bans WHERE UUID=?");
                ps.setString(1, UUID);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getString("NAME");
                }
                ps.close();
                rs.close();
            } catch (SQLException exc){
                exc.printStackTrace();
            }
        }
        return null;
    }

    public static String getUUIDByName(String Name){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM bans WHERE NAME=?");
            ps.setString(1, Name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("UUID");
            }
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public void updateName(String UUID, String newName){
        ProxyServer.getInstance().getScheduler().runAsync(Main.main, () -> {
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("UPDATE bans SET NAME=? WHERE UUID=?");
                ps.setString(1, newName);
                ps.setString(2, UUID);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        });
    }

    public void ban(String UUID, int GrundID, String TeamUUID, int Prozentsatz, boolean increaseBans){
        try{
            if(getReasonTime(GrundID) == -1){
                //Perma Ban
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("UPDATE bans SET BANNED='1', REASON=?, END='-1', TEAMUUID=? WHERE UUID=?");
                ps.setString(1, getReasonByID(GrundID));
                ps.setString(2, TeamUUID);
                ps.setString(3, UUID);
                ps.executeUpdate();
                ps.close();
            } else {
                //Temp Ban
                //Formel: 1.50 * Anzahl an Tagen = Ergebniss (50%)
                int bans = getBans(UUID);
                int defaultmins = getReasonTime(GrundID);
                long current = System.currentTimeMillis();
                long end = current + getReasonTime(GrundID) * 60000L;
                long increaseEnd = current + Prozentsatz / 100 + 1 * defaultmins * bans * 60000L; //Formel!!!!!
                if(increaseBans){
                    if(bans == 0){
                        PreparedStatement ps = Main.mysql.getCon()
                                .prepareStatement("UPDATE bans SET BANNED='1', REASON=?, END=?, TEAMUUID=? WHERE UUID=?");
                        ps.setString(1, getReasonByID(GrundID));
                        ps.setLong(2, end);
                        ps.setString(3, TeamUUID);
                        ps.setString(4, UUID);
                        ps.executeUpdate();
                        ps.close();
                    } else {
                        PreparedStatement ps = Main.mysql.getCon()
                                .prepareStatement("UPDATE bans SET BANNED='1', REASON=?, END=?, TEAMUUID=? WHERE UUID=?");
                        ps.setString(1, getReasonByID(GrundID));
                        ps.setLong(2, increaseEnd);
                        ps.setString(3, TeamUUID);
                        ps.setString(4, UUID);
                        ps.executeUpdate();
                        ps.close();
                    }
                } else {
                    PreparedStatement ps = Main.mysql.getCon()
                            .prepareStatement("UPDATE bans SET BANNED='1', REASON=?, END=?, TEAMUUID=? WHERE UUID=?");
                    ps.setString(1, getReasonByID(GrundID));
                    ps.setLong(2, end);
                    ps.setString(3, TeamUUID);
                    ps.setString(4, UUID);
                    ps.executeUpdate();
                    ps.close();
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void mute(String UUID, int GrundID, String TeamUUID){
        try{
            long current = System.currentTimeMillis();
            long end = current + getReasonTime(GrundID) * 60000L;
            if(getReasonTime(GrundID) == -1){
                //Perma Mute
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("UPDATE bans SET MUTED='1', REASON=?, END='-1', TEAMUUID=? WHERE UUID=?");
                ps.setString(1, getReasonByID(GrundID));
                ps.setString(2, TeamUUID);
                ps.setString(3, UUID);
                ps.executeUpdate();
                ps.close();
            } else {
                //Temp Mute
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("UPDATE bans SET MUTED='1', REASON=?, END=?, TEAMUUID=? WHERE UUID=?");
                ps.setString(1, getReasonByID(GrundID));
                ps.setLong(2, end);
                ps.setString(3, TeamUUID);
                ps.setString(4, UUID);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public Long getRAWEnd(String UUID){
        if(playerExists(UUID)){
            try {

                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM bans WHERE UUID=?");
                ps.setString(1, UUID);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getLong("END");
                }
                ps.close();
                rs.close();
            } catch (SQLException e){
                e.printStackTrace();
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

    public boolean isBanned(String UUID){
        if(playerExists(UUID)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM bans WHERE UUID=?");
                ps.setString(1, UUID);
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

    public boolean isMuted(String UUID){
        if(playerExists(UUID)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM bans WHERE UUID=?");
                ps.setString(1, UUID);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    if(rs.getInt("MUTED") == 1){
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

    public void unban(String UUID){
        try{
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("UPDATE bans SET BANNED='0' WHERE UUID=?");
            ps.setString(1, UUID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void unmute(String UUID){
        try{
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("UPDATE bans SET MUTED='0' WHERE UUID=?");
            ps.setString(1, UUID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public String getReasonString(String UUID){
        if(playerExists(UUID)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM bans WHERE UUID=?");
                ps.setString(1, UUID);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getString("REASON");
                }
                ps.close();
                rs.close();
            } catch (SQLException exc){

            }
        }
        return null;
    }

    public void sendNotify(String Type, String BannedName, String TeamName, String Grund){
        if(Type.toUpperCase().equals("BAN")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify")){
                    all.sendMessage(Main.data.Prefix+"§e§l"+BannedName+" §7wurde von §c§l"+TeamName+" §cgebannt §7wegen §a"+Grund);
                }
            }
        }
        if(Type.toUpperCase().equals("IPBAN")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify")){
                    all.sendMessage(Main.data.Prefix+"§7Die IP §e§l"+BannedName+" §7wurde von §c§l"+TeamName+" §cgebannt §7wegen §a"+Grund);
                }
            }
        }
        if(Type.toUpperCase().equals("MUTE")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.data.Prefix+"§e§l"+BannedName+" §7wurde von §c§l"+TeamName+" §cgemutet §7wegen §a"+Grund);
                }
            }
        }
        if(Type.toUpperCase().equals("AUTOMUTE")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.data.Prefix+"§e§l"+BannedName+" §7wurde §cautomatisch gemutet §7wegen §a"+Grund+" §8(§7"+TeamName+"§8)");
                }
            }
        }
        if(Type.toUpperCase().equals("KICK")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.data.Prefix+"§e§l"+BannedName+" §7wurde von §c§l"+TeamName+" §cgekickt §7wegen §a"+Grund);
                }
            }
        }
        if(Type.toUpperCase().equals("UNBAN")){
                for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                    if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.data.Prefix+"§c§l"+TeamName+" §7hat §e§l"+BannedName+" §aentbannt");
                }
            }
        }
        if(Type.toUpperCase().equals("UNBANIP")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.data.Prefix+"§c§l"+TeamName+" §7hat die IP-Adresse §e§l"+BannedName+" §aentbannt");
                }
            }
        }
        if(Type.toUpperCase().equals("UNMUTE")){
                for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                    if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    all.sendMessage(Main.data.Prefix+"§c§l"+TeamName+" §7hat §e§l"+BannedName+" §aentmutet");
                }
            }
        }
        if(Type.toUpperCase().equals("REPORT")){
            for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                if(all.hasPermission("professionalbans.notify") || all.hasPermission("professionalbans.*")){
                    if(!Reports.not_logged.contains(all)){
                        TextComponent tc = new TextComponent();
                        tc.setText(Main.data.Prefix+"§c§l"+TeamName+" §7hat §e§l"+BannedName+" §7wegen §a"+Grund+" §7gemeldet");
                        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reports"));
                        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Alle §eoffenen §7Reports anzeigen").create()));
                        all.sendMessage(tc);
                    }
                }
            }
        }
    }

    public Integer getBans(String UUID){
        if(playerExists(UUID)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM bans WHERE UUID=?");
                ps.setString(1, UUID);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getInt("BANS");
                }
                ps.close();
                rs.close();
            } catch (SQLException exc){
                exc.printStackTrace();
            }
        }
        return null;
    }

    public void setBans(String UUID, int Bans){
        try{
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("UPDATE bans SET BANS=? WHERE UUID=?");
            ps.setInt(1, Bans);
            ps.setString(2, UUID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public Integer getMutes(String UUID){
        if(playerExists(UUID)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM bans WHERE UUID=?");
                ps.setString(1, UUID);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getInt("MUTES");
                }
                rs.close();
                ps.close();
            } catch (SQLException exc){
                exc.printStackTrace();
            }
        }
        return null;
    }

    public void setMutes(String UUID, int Mutes){
        try{
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("UPDATE bans SET MUTES=? WHERE UUID=?");
            ps.setInt(1, Mutes);
            ps.setString(2, UUID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public Integer countReasons(){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM reasons");
            ResultSet rs = ps.executeQuery();
            int i = 0;
            while (rs.next()){
                i++;
            }
            ps.close();
            rs.close();
            return i;
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public String getReasonByID(int Reason){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM reasons WHERE ID=?");
            ps.setInt(1, Reason);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("REASON");
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public Integer getReasonTime(int ID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM reasons WHERE ID=?");
            ps.setInt(1, ID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt("TIME");
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public String getFormattedReasonTime(int id){
        int minutes = getReasonTime(id);
        if(minutes != -1){
            return TimeManager.formatOnlineTime(minutes);
        } else {
            return "§4§lPERMANENT";
        }
    }

    public boolean isBanReason(int ID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM reasons WHERE ID=?");
            ps.setInt(1, ID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                if(rs.getInt("TYPE") == 0){
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
        return false;
    }

    public Integer getReasonBans(int ReasonID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM reasons WHERE ID=?");
            ps.setInt(1, ReasonID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt("BANS");
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public void setReasonBans(int ReasonID, int Bans){
        try{
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("UPDATE reasons SET BANS=? WHERE ID=?");
            ps.setInt(1, Bans);
            ps.setInt(2, ReasonID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public boolean hasExtraPerms(int ReasonID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM reasons WHERE ID=?");
            ps.setInt(1, ReasonID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                if(rs.getString("PERMS") == null){
                    return false;
                } else {
                    return true;
                }
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return false;
    }

    public String getExtraPerms(int ReasonID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM reasons WHERE ID=?");
            ps.setInt(1, ReasonID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("PERMS");
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }


    public boolean webaccountExists(String UUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM user WHERE uuid=?");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("UUID") != null;
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return false;
    }

    public boolean isWebaccountAdmin(String UUID){
        if(webaccountExists(UUID)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM user WHERE uuid=?");
                ps.setString(1, UUID);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    if(rs.getString("roles").contains("ROLE_SUPER_ADMIN")){
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
        } else {
            return false;
        }
        return false;
    }

    public boolean hasAuthToken(String UUID){
        if(webaccountExists(UUID)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM accounts WHERE UUID=?");
                ps.setString(1, UUID);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    if(rs.getString("AUTHCODE") == "null"){
                        return false;
                    } else {
                        return true;
                    }
                }
                ps.close();
                rs.close();
            } catch (SQLException exc){
                exc.printStackTrace();
            }
        } else {
            return false;
        }
        return false;
    }

    public String getAuthCode(String UUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM accounts WHERE UUID=?");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("AUTHCODE");
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public void updateAuthStatus(String UUID){
        try{
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("UPDATE accounts SET AUTHSTATUS = 1 WHERE UUID =?");
            ps.setString(1, UUID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void createReport(String UUID, String ReporterUUID, String Reason, String LogID){
        try{
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("INSERT INTO reports(UUID, REPORTER, TEAM, REASON, LOG, STATUS, CREATED_AT) "+
                            "VALUES (?, ?, 'null', ?, ?, '0', ?)");
            ps.setString(1, UUID);
            ps.setString(2, ReporterUUID);
            ps.setString(3, Reason);
            ps.setString(4, LogID);
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public Integer countOpenReports(){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM reports WHERE STATUS = 0");
            ResultSet rs = ps.executeQuery();
            int i = 0;
            while (rs.next()){
                i++;
            }
            ps.close();
            rs.close();
            return i;
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public ArrayList getIDsFromOpenReports(){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM reports WHERE STATUS = 0");
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> ids = new ArrayList<>();
            while (rs.next()){
               ids.add(rs.getInt("ID"));
            }
            ps.close();
            rs.close();
            return ids;
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public String getNameByReportID(int ReportID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM reports WHERE ID = ?");
            ps.setInt(1, ReportID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return getNameByUUID(rs.getString("UUID"));
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public String getReasonByReportID(int ReportID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM reports WHERE ID = ?");
            ps.setInt(1, ReportID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("REASON");
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public void setReportDone(int ID){
        try{
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("UPDATE reports SET STATUS = 1 WHERE ID = ?");
            ps.setInt(1, ID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void setReportTeamUUID(int ID, String UUID){
        try{
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("UPDATE reports SET TEAM = ? WHERE ID = ?");
            ps.setString(1, UUID);
            ps.setInt(2, ID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void updateLastLogin(String UUID){
        ProxyServer.getInstance().getScheduler().runAsync(Main.main, () -> {
            try{
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("UPDATE bans SET LASTLOGIN = ? WHERE UUID = ?");
                ps.setLong(1, System.currentTimeMillis());
                ps.setString(2, UUID);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        });
    }

    public String getLastLogin(String UUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM bans WHERE UUID=?");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("LASTLOGIN");
            }
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public String getFirstLogin(String UUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM bans WHERE UUID=?");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("FIRSTLOGIN");
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public String formatTimestamp(long timestamp){
        Date date = new Date(timestamp);
        SimpleDateFormat jdf = new SimpleDateFormat(Main.messages.getString("date_format"));
        return jdf.format(date);
    }

    public boolean hasEA(String UUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM unbans WHERE UUID=?");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("UUID") != null;
            }
            ps.close();
            rs.close();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return false;
    }

    public String getEAStatus(String UUID){
        if(hasEA(UUID)){
            try {
                PreparedStatement ps = Main.mysql.getCon()
                        .prepareStatement("SELECT * FROM unbans WHERE UUID=? ORDER BY DATE DESC");
                ps.setString(1, UUID);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    long date = Long.valueOf(rs.getInt("DATE")*1000);
                    if(getRAWEnd(UUID) > date){
                        if(rs.getInt("STATUS") == 0){
                            return "§eDein Entbannunsantrag wird gerade bearbeitet";
                        } else if(rs.getInt("STATUS") == 2){
                            return "§eDein Ban wurde aufgrund deines Entbannungsantrags verkürzt";
                        } else if(rs.getInt("STATUS") == 3){
                            return "§eDein Entbannungsantrag wurde abgelehnt";
                        } else {
                            return "§7Du kannst einen Entbannungsantrag stellen auf \n §e"+Main.data.WebURL+"public/unban.php";
                        }
                    } else {
                        return "§7Du kannst einen Entbannungsantrag stellen auf \n §e"+Main.data.WebURL+"public/unban.php";
                    }
                }
                ps.close();
                rs.close();
            } catch (SQLException exc){
                exc.printStackTrace();
            }
        }
        return "§7Du kannst einen Entbannungsantrag stellen auf \n §e"+Main.data.WebURL+"public/unban.php";
    }

}
