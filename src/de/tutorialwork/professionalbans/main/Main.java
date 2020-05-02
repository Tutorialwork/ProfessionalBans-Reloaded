package de.tutorialwork.professionalbans.main;

import de.tutorialwork.professionalbans.commands.*;
import de.tutorialwork.professionalbans.listener.Chat;
import de.tutorialwork.professionalbans.listener.Login;
import de.tutorialwork.professionalbans.listener.Quit;
import de.tutorialwork.professionalbans.utils.*;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class Main extends Plugin {

    public static Locale locale_en = new Locale("en");
    public static Locale locale_de = new Locale("de");
    public static ResourceBundle messages = ResourceBundle.getBundle("messages", locale_en);

    public static Main main;
    public static MySQLConnect mysql;
    public static BanManager ban;
    public static IPManager ip;
    public static Data data;

    //==============================================
    //Plugin Informationen
    public static final String Version = "2.9.2";
    //==============================================

    @Override
    public void onEnable() {
        init();
        data.sendConsoleStartupMessage();
        data.checkUpdateConsole();
        Metrics metrics = new Metrics(this);
    }

    private void init(){
        main = this;

        BanManager banManager = new BanManager();
        ban = banManager;

        IPManager ipManager = new IPManager();
        ip = ipManager;

        Data dataManager = new Data();
        data = dataManager;

        Config();
        MySQL();
        Commands();
        Listener();
        Schedulers();

        if(Language.getLanguage().equals("de")){
            Language.initLanguage(locale_de);
        }
    }

    private void Schedulers(){
        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                data.webChecker();
            }
        }, 5, 5, TimeUnit.SECONDS);

        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                Report.players.clear();
            }
        }, data.ReportCooldown, data.ReportCooldown, TimeUnit.MINUTES);
    }

    private void Config() {
        if(!getDataFolder().exists()){
            getDataFolder().mkdir();
        }
        File file = new File(getDataFolder().getPath(), "mysql.yml");
        File config = new File(getDataFolder().getPath(), "config.yml");
        File blacklistfile = new File(getDataFolder().getPath(), "blacklist.yml");
        try {
            if(!file.exists()){
                file.createNewFile();
                Configuration mysql = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
                mysql.set("HOST", "localhost");
                mysql.set("DATENBANK", "Bans");
                mysql.set("USER", "root");
                mysql.set("PASSWORT", "deinpasswort");
                mysql.set("PORT", 3306);
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(mysql, file);
            }
            if(!config.exists()){
                config.createNewFile();
                Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                configcfg.set("WEBINTERFACE.URL", "https://YourServer.com/Path/to/Webinterface");
                configcfg.set("Prefix", "&6&lP&e&lBANS &8• &7");
                configcfg.set("CHATFORMAT.MSG", "&5&lMSG &8• &7%from% » &e%message%");
                configcfg.set("CHATFORMAT.TEAMCHAT", "&e&lTEAM &8• &7%from% » &e%message%");
                configcfg.set("CHATFORMAT.BROADCAST", "&8• &c&lBROADCAST &8• \n &8» &7%message%");
                configcfg.set("LAYOUT.BAN", "&8[]===================================[] \n\n &4&lYou are BANNED \n\n &eReason: §c§l%grund% \n\n%ea-status% \n\n&8[]===================================[]");
                configcfg.set("LAYOUT.KICK", "&8[]===================================[] \n\n &e&lYou are KICKED \n\n &eReason: §c§l%grund% \n\n&8[]===================================[]");
                configcfg.set("LAYOUT.TEMPBAN", "&8[]===================================[] \n\n &4&lYou are temporarily BANNED \n\n &eGrund: §c§l%grund% \n &eTime remeaning: &c&l%dauer% \n\n%ea-status% \n\n&8[]===================================[]");
                configcfg.set("LAYOUT.MUTE", "&8[]===================================[] \n\n &4&lYou are MUTED \n\n &eReason: §c§l%grund% \n\n&8[]===================================[]");
                configcfg.set("LAYOUT.TEMPMUTE", "&8[]===================================[] \n\n &4&lYou are temporarily MUTED \n\n &eGrund: §c§l%grund% \n &eTime remeaning: &c&l%dauer% \n\n&8[]===================================[]");
                configcfg.set("LAYOUT.IPBAN", "&8[]===================================[] \n\n &4&lYour IP was BANNED \n\n &eReason: §c§l%grund% \n\n&8[]===================================[]");
                configcfg.set("LAYOUT.TEMPIPBAN", "&8[]===================================[] \n\n &4&lYour IP was temporarily BANNED \n\n &eReason: §c§l%grund% \n &eTime remeaning: &c&l%dauer% \n\n&8[]===================================[]");
                configcfg.set("VPN.BLOCKED", true);
                configcfg.set("VPN.KICK", true);
                configcfg.set("VPN.KICKMSG", "&7Using a &4VPN &7is &cDISALLOWED");
                configcfg.set("VPN.BAN", false);
                configcfg.set("VPN.BANID", 0);
                configcfg.set("VPN.WHITELIST", data.ipwhitelist);
                configcfg.set("VPN.APIKEY", "Go to https://proxycheck.io/dashboard and register with your email and enter here your API Key");
                configcfg.set("REPORTS.ENABLED", true);
                configcfg.set("REPORTS.REASONS", data.reportreasons);
                configcfg.set("REPORTS.OFFLINEREPORTS", false);
                configcfg.set("REPORTS.COOLDOWN_MIN", 1);
                configcfg.set("CHATLOG.ENABLED", true);
                configcfg.set("AUTOMUTE.ENABLED", false);
                configcfg.set("AUTOMUTE.AUTOREPORT", true);
                configcfg.set("AUTOMUTE.MUTEID", 0);
                configcfg.set("AUTOMUTE.ADMUTEID", 0);
                configcfg.set("BANTIME-INCREASE.ENABLED", true);
                configcfg.set("BANTIME-INCREASE.PERCENTRATE", 50);
                configcfg.set("COMMANDS.MSG", true);
                configcfg.set("COMMANDS.TEAMCHAT", true);
                configcfg.set("COMMANDS.BROADCAST", true);
                configcfg.set("COMMANDS.SUPPORT", true);
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
            } else {
                Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                if(configcfg.getBoolean("VPN.KICK") && configcfg.getBoolean("VPN.BAN")){
                    BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
                    BungeeCord.getInstance().getConsole().sendMessage("§c§lSINNLOSE EINSTELLUNG ENTDECKT");
                    BungeeCord.getInstance().getConsole().sendMessage("§7Wenn ein Spieler mit einer VPN das Netzwerk betritt kann er nicht gekickt UND gebannt werden.");
                    BungeeCord.getInstance().getConsole().sendMessage("§4§lÜberprüfe die VPN Einstellung in der CONFIG.YML");
                    BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
                    //Setze VPN Einstellung zurück!
                    configcfg.set("VPN.BLOCKED", true);
                    configcfg.set("VPN.KICK", true);
                    configcfg.set("VPN.KICKMSG", "&7Using a &4VPN &7is &cDISALLOWED");
                    configcfg.set("VPN.BAN", false);
                    configcfg.set("VPN.BANID", 0);
                }
                for(String reasons : configcfg.getStringList("REPORTS.REASONS")){
                    data.reportreasons.add(reasons.toUpperCase());
                }
                for(String ips : configcfg.getStringList("VPN.WHITELIST")){
                    data.ipwhitelist.add(ips);
                }
                data.Prefix = configcfg.getString("Prefix").replace("&", "§");
                data.increaseBans = configcfg.getBoolean("BANTIME-INCREASE.ENABLED");
                data.increaseValue = configcfg.getInt("BANTIME-INCREASE.PERCENTRATE");
                if(configcfg.getString("VPN.APIKEY").length() == 27){
                    data.APIKey = configcfg.getString("VPN.APIKEY");
                }
                if(!configcfg.getString("WEBINTERFACE.URL").equals("https://YourServer.com/Path/to/Webinterface")){
                    Main.data.WebURL = configcfg.getString("WEBINTERFACE.URL");
                    if(!data.WebURL.startsWith("https://") && !data.WebURL.startsWith("http://")){
                        data.WebURL = "https://" + data.WebURL;
                    }
                    if(!data.WebURL.endsWith("/")){
                        data.WebURL = data.WebURL + "/";
                    }
                } else {
                    //==============================================
                    //Warnung über fehlende Einstellung
                    BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
                    BungeeCord.getInstance().getConsole().sendMessage("§4§lAchtung!");
                    BungeeCord.getInstance().getConsole().sendMessage("§cEs wurde festgestellt das du noch nicht die Webinterface URL in der §8§oconfig.yml §c§leingestellt hast.");
                    BungeeCord.getInstance().getConsole().sendMessage("§7Folgende Features werden nicht planmäßig funktionieren");
                    BungeeCord.getInstance().getConsole().sendMessage("§c§lChatlog-System");
                    BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
                    //==============================================
                }
                if(configcfg.getInt("REPORTS.COOLDOWN_MIN") != 0){ //Is config file updated?
                    data.ReportCooldown = configcfg.getInt("REPORTS.COOLDOWN_MIN");
                }
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
            }
            if(!blacklistfile.exists()){
                blacklistfile.createNewFile();
                Configuration blacklistcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(blacklistfile);
                data.seedArrays();
                blacklistcfg.set("ADWHITELIST", data.adwhitelist);
                blacklistcfg.set("ADBLACKLIST", data.adblacklist);
                blacklistcfg.set("BLACKLIST", data.blacklist);
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(blacklistcfg, blacklistfile);
            } else {
                Configuration blacklistcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(blacklistfile);
                for(String congigstr : blacklistcfg.getStringList("BLACKLIST")){
                    data.blacklist.add(congigstr);
                }
                for(String congigstr : blacklistcfg.getStringList("ADBLACKLIST")){
                    data.adblacklist.add(congigstr);
                }
                for(String congigstr : blacklistcfg.getStringList("ADWHITELIST")){
                    data.adwhitelist.add(congigstr.toUpperCase());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void MySQL() {
        try {
            File file = new File(getDataFolder().getPath(), "mysql.yml");
            Configuration mysql = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            MySQLConnect.HOST = mysql.getString("HOST");
            MySQLConnect.DATABASE = mysql.getString("DATENBANK");
            MySQLConnect.USER = mysql.getString("USER");
            MySQLConnect.PASSWORD = mysql.getString("PASSWORT");
            if(mysql.getInt("PORT") != 0){
                MySQLConnect.PORT = mysql.getInt("PORT");
            } else {
                MySQLConnect.PORT = 3306;
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        mysql = new MySQLConnect(MySQLConnect.HOST, MySQLConnect.DATABASE, MySQLConnect.USER, MySQLConnect.PASSWORD, MySQLConnect.PORT);
        data.seedDatabase();
    }

    private void Commands() {
        try{
            File file = new File(getDataFolder().getPath(), "config.yml");
            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            getProxy().getPluginManager().registerCommand(this, new Ban("ban"));
            getProxy().getPluginManager().registerCommand(this, new Unban("unban"));
            getProxy().getPluginManager().registerCommand(this, new Kick("kick"));
            getProxy().getPluginManager().registerCommand(this, new WebAccount("webaccount"));
            getProxy().getPluginManager().registerCommand(this, new Check("check"));
            getProxy().getPluginManager().registerCommand(this, new ProfessionalBans("professionalbans"));
            getProxy().getPluginManager().registerCommand(this, new IPBan("ipban"));
            if(cfg.getBoolean("REPORTS.ENABLED")){
                getProxy().getPluginManager().registerCommand(this, new Report("report"));
                getProxy().getPluginManager().registerCommand(this, new Reports("reports"));
            }
            if(cfg.getBoolean("CHATLOG.ENABLED")){
                getProxy().getPluginManager().registerCommand(this, new Chatlog("chatlog"));
            }
            getProxy().getPluginManager().registerCommand(this, new Blacklist("blacklist"));
            getProxy().getPluginManager().registerCommand(this, new WebVerify("webverify"));
            if(cfg.getBoolean("COMMANDS.SUPPORT")){
                getProxy().getPluginManager().registerCommand(this, new SupportChat("support"));
            }
            if(cfg.getBoolean("COMMANDS.MSG")){
                getProxy().getPluginManager().registerCommand(this, new PrivateMessage("msg"));
                getProxy().getPluginManager().registerCommand(this, new PrivateMessageReply("r"));
            }
            if(cfg.getBoolean("COMMANDS.TEAMCHAT")){
                getProxy().getPluginManager().registerCommand(this, new TeamChat("tc"));
            }
            if(cfg.getBoolean("COMMANDS.BROADCAST")){
                getProxy().getPluginManager().registerCommand(this, new Broadcast("bc"));
            }
            getProxy().getPluginManager().registerCommand(this, new PlayerHistory("history"));
            getProxy().getPluginManager().registerCommand(this, new Onlinezeit("onlinezeit"));
            getProxy().getPluginManager().registerCommand(this, new Onlinezeit("onlinetime"));
            getProxy().getPluginManager().registerCommand(this, new Language("language"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Listener() {
        getProxy().getPluginManager().registerListener(this, new Login());
        getProxy().getPluginManager().registerListener(this, new Chat());
        getProxy().getPluginManager().registerListener(this, new Quit());
    }

    public static Main getInstance(){
        return main;
    }

    public static String callURL(String myURL) {
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn = null;
        InputStreamReader in = null;
        try {
            URL url = new URL(myURL);
            urlConn = url.openConnection();
            if (urlConn != null)
                urlConn.setReadTimeout(60 * 1000);
            if (urlConn != null && urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(),
                        Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                if (bufferedReader != null) {
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    bufferedReader.close();
                }
            }
            in.close();
        } catch (Exception e) {
            throw new RuntimeException("Exception while calling URL:"+ myURL, e);
        }

        return sb.toString();
    }
}
