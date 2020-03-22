package de.tutorialwork.professionalbans.utils;

import de.tutorialwork.professionalbans.main.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class MessagesManager {

    public static HashMap<ProxiedPlayer, ProxiedPlayer> lastchat = new HashMap<>();

    public static void sendMessage(ProxiedPlayer p, ProxiedPlayer target, String message){
        try{
            File file = new File(Main.main.getDataFolder(), "config.yml");
            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

            target.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getString("CHATFORMAT.MSG").replace("%from%", p.getName()).replace("%message%", message)));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getString("CHATFORMAT.MSG").replace("%from%", "Du").replace("%message%", message)));
            MessagesManager.insertMessage(p.getUniqueId().toString(), target.getUniqueId().toString(), message);
            MessagesManager.updateLastChat(p, target);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendOpenMessages(){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM privatemessages WHERE STATUS = 0");
            ResultSet rs = ps.executeQuery();
            while (rs.next()){

                ProxiedPlayer target = BungeeCord.getInstance().getPlayer(Main.ban.getNameByUUID(rs.getString("RECEIVER")));
                if(target != null){
                    target.sendMessage(getRowMSGFormat().replace("%from%", Main.ban.getNameByUUID(rs.getString("SENDER"))).replace("%message%", rs.getString("MESSAGE")));
                    setDoneMessage(rs.getInt("ID"));
                }

            }
            ps.close();
            rs.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void sendOpenBroadcasts(){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM privatemessages WHERE STATUS = 0 AND RECEIVER = 'BROADCAST'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()){

                try{
                    File file = new File(Main.main.getDataFolder(), "config.yml");
                    Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

                    BungeeCord.getInstance().broadcast("");
                    BungeeCord.getInstance().broadcast("§8[]===================================[]");
                    BungeeCord.getInstance().broadcast("");
                    BungeeCord.getInstance().broadcast(ChatColor.translateAlternateColorCodes('&', cfg.getString("CHATFORMAT.BROADCAST").replace("%message%", rs.getString("MESSAGE"))));
                    BungeeCord.getInstance().broadcast("");
                    BungeeCord.getInstance().broadcast("§8[]===================================[]");
                    BungeeCord.getInstance().broadcast("");

                    setDoneMessage(rs.getInt("ID"));

                } catch(IOException e){ }

            }
        } catch (SQLException exc){ }
    }

    public static String getRowMSGFormat(){
        try{
            File file = new File(Main.main.getDataFolder(), "config.yml");
            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

            return ChatColor.translateAlternateColorCodes('&', cfg.getString("CHATFORMAT.MSG"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setDoneMessage(int ID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("UPDATE privatemessages SET STATUS = 1 WHERE ID = ?");
            ps.setInt(1, ID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void insertMessage(String SenderUUID, String ReceiverUUID, String Message){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("INSERT INTO privatemessages(SENDER, RECEIVER, MESSAGE, STATUS, DATE) "+
                            "VALUES (?, ?, ?, '1', ?)");
            ps.setString(1, SenderUUID);
            ps.setString(2, ReceiverUUID);
            ps.setString(3, Message);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void updateLastChat(ProxiedPlayer player, ProxiedPlayer to){
        if(lastchat.containsKey(player)){
            lastchat.remove(player);
        }
        lastchat.put(player, to);
    }

    public static ProxiedPlayer getLastChatPlayer(ProxiedPlayer p){
        if(lastchat.containsKey(p)){
            return lastchat.get(p);
        } else {
            return null;
        }
    }

    public static boolean hasApp(String UUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM apptokens WHERE UUID=?");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                if(rs.getString("UUID") == null){
                    return false;
                } else {
                    return true;
                }
            }
        } catch (SQLException exc){ }
        return false;
    }

    public static String getFirebaseToken(String UUID){
        try {
            PreparedStatement ps = Main.mysql.getCon()
                    .prepareStatement("SELECT * FROM apptokens WHERE UUID=?");
            ps.setString(1, UUID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("FIREBASE_TOKEN");
            }
        } catch (SQLException exc){ }
        return null;
    }

    public static void sendPushNotify(String to, String title, String message) {
        try{
            String url = "https://tutorialwork.de/api/cloudmessaging.php";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Content-Type","application/json");

            String postJsonData = "{\n" +
                    "\t\"to\": \""+to+"\",\n" +
                    "\t\"title\": \""+title+"\",\n" +
                    "\t\"message\": \""+message+"\"\n" +
                    "}";

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postJsonData);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            /*
            Debug
            System.out.println("nSending 'POST' request to URL : " + url);
            System.out.println("Post Data : " + postJsonData);
            System.out.println("Response Code : " + responseCode);
             */

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String output;
            StringBuffer response = new StringBuffer();

            while ((output = in.readLine()) != null) {
                response.append(output);
            }
            in.close();

            //printing result from response
            //System.out.println(response.toString());
        } catch (IOException e){ }
    }

}
