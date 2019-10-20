package de.tutorialwork.main;

import de.tutorialwork.commands.Blacklist;
import de.tutorialwork.commands.*;
import de.tutorialwork.listener.Chat;
import de.tutorialwork.listener.Login;
import de.tutorialwork.listener.Quit;
import de.tutorialwork.utils.BanManager;
import de.tutorialwork.utils.MessagesManager;
import de.tutorialwork.utils.Metrics;
import de.tutorialwork.utils.MySQLConnect;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Main extends Plugin {

    public static Main main;
    public static MySQLConnect mysql;
    public static String Prefix = "§e§lBANS §8• §7";
    public static String NoPerms = Prefix + "§cDu hast keine Berechtigung diesen Befehl zu nutzen";

    public static ArrayList<String> reportreasons = new ArrayList<>();
    public static ArrayList<String> blacklist = new ArrayList<>();
    public static ArrayList<String> adblacklist = new ArrayList<>();
    public static ArrayList<String> adwhitelist = new ArrayList<>();
    public static ArrayList<String> ipwhitelist = new ArrayList<>();

    public static boolean increaseBans = true;
    public static Integer increaseValue = 50;

    public static String APIKey = null;
    public static String WebURL = null;

    //==============================================
    //Plugin Informationen
    public static String Version = "2.5";
    //==============================================

    @Override
    public void onEnable() {
        main = this;
        Config();
        MySQL();
        Commands();
        Listener();
        //==============================================
        //Konsolen Nachricht über das Plugin
        BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
        BungeeCord.getInstance().getConsole().sendMessage("§e§lProfessionalBans §7§oReloaded §8| §7Version: §c"+Version);
        BungeeCord.getInstance().getConsole().sendMessage("§7Developer: §e§lTutorialwork");
        BungeeCord.getInstance().getConsole().sendMessage("§5YT §7Kanal: §cyoutube.com/Tutorialwork");
        BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
        //==============================================
        //==============================================
        //Überprüft auf Bans aus dem Webinterface
        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                File config = new File(Main.main.getDataFolder(), "config.yml");
                try {
                    Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                    for(ProxiedPlayer all : getProxy().getPlayers()){
                        if(BanManager.isBanned(all.getUniqueId().toString())){
                            if(BanManager.getRAWEnd(all.getUniqueId().toString()) == -1L){
                                all.disconnect(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.BAN").replace("%grund%", BanManager.getReasonString(all.getUniqueId().toString()))));
                            } else {
                                String MSG = configcfg.getString("LAYOUT.TEMPBAN");
                                MSG = MSG.replace("%grund%", BanManager.getReasonString(all.getUniqueId().toString()));
                                MSG = MSG.replace("%dauer%", BanManager.getEnd(all.getUniqueId().toString()));
                                all.disconnect(ChatColor.translateAlternateColorCodes('&', MSG));
                            }
                        }
                        MessagesManager.sendOpenMessages();
                        MessagesManager.sendOpenBroadcasts();
                    }
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
        //==============================================
        //==============================================
        //Updater
        if(!callURL("https://api.spigotmc.org/legacy/update.php?resource=63657").equals(Version)){
            BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
            BungeeCord.getInstance().getConsole().sendMessage("§e§lProfessionalBans §7Reloaded §8| §7Version §c"+Version);
            BungeeCord.getInstance().getConsole().sendMessage("§cDu benutzt eine §c§lVERALTETE §cVersion des Plugins!");
            BungeeCord.getInstance().getConsole().sendMessage("§7Update: §4§lhttps://spigotmc.org/resources/63657");
            BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
        }
        //==============================================
        Metrics metrics = new Metrics(this);
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
                configcfg.set("WEBINTERFACE.URL", "https://DeinServer.de/Webinterface");
                configcfg.set("PREFIX", "&e&lBANS &8• &7");
                configcfg.set("CHATFORMAT.MSG", "&5&lMSG &8• &7%from% » &e%message%");
                configcfg.set("CHATFORMAT.TEAMCHAT", "&e&lTEAM &8• &7%from% » &e%message%");
                configcfg.set("CHATFORMAT.BROADCAST", "&8• &c&lMITTEILUNG &8• \n &8» &7%message%");
                configcfg.set("LAYOUT.BAN", "&8[]===================================[] \n\n &4&lDu wurdest GEBANNT \n\n &eGrund: §c§l%grund% \n\n&8[]===================================[]");
                configcfg.set("LAYOUT.KICK", "&8[]===================================[] \n\n &e&lDu wurdest GEKICKT \n\n &eGrund: §c§l%grund% \n\n&8[]===================================[]");
                configcfg.set("LAYOUT.TEMPBAN", "&8[]===================================[] \n\n &4&lDu wurdest temporär GEBANNT \n\n &eGrund: §c§l%grund% \n &eRestzeit: &c&l%dauer% \n\n&8[]===================================[]");
                configcfg.set("LAYOUT.MUTE", "&8[]===================================[] \n\n &4&lDu wurdest GEMUTET \n\n &eGrund: §c§l%grund% \n\n&8[]===================================[]");
                configcfg.set("LAYOUT.TEMPMUTE", "&8[]===================================[] \n\n &4&lDu wurdest temporär GEMUTET \n\n &eGrund: §c§l%grund% \n &eRestzeit: &c&l%dauer% \n\n&8[]===================================[]");
                configcfg.set("LAYOUT.IPBAN", "&8[]===================================[] \n\n &4&lDeine IP-Adresse wurde GEBANNT \n\n &eGrund: §c§l%grund% \n\n&8[]===================================[]");
                configcfg.set("LAYOUT.TEMPIPBAN", "&8[]===================================[] \n\n &4&lDeine IP-Adresse wurde temporär GEBANNT \n\n &eGrund: §c§l%grund% \n &eRestzeit: &c&l%dauer% \n\n&8[]===================================[]");
                configcfg.set("VPN.BLOCKED", true);
                configcfg.set("VPN.KICK", true);
                configcfg.set("VPN.KICKMSG", "&7Das benutzen einer &4VPN &7ist auf unserem Netzwerk &cUNTERSAGT");
                configcfg.set("VPN.BAN", false);
                configcfg.set("VPN.BANID", 0);
                ipwhitelist.add("8.8.8.8");
                configcfg.set("VPN.WHITELIST", ipwhitelist);
                configcfg.set("VPN.APIKEY", "Go to https://proxycheck.io/dashboard and register with your email and enter here your API Key");
                configcfg.set("REPORTS.ENABLED", true);
                reportreasons.add("Hacking");
                reportreasons.add("Verhalten");
                reportreasons.add("Teaming");
                reportreasons.add("TPA-Falle");
                reportreasons.add("Werbung");
                configcfg.set("REPORTS.REASONS", reportreasons);
                configcfg.set("REPORTS.OFFLINEREPORTS", false);
                configcfg.set("CHATLOG.ENABLED", true);
                configcfg.set("AUTOMUTE.ENABLED", false);
                configcfg.set("AUTOMUTE.AUTOREPORT", true);
                //configcfg.set("AUTOMUTE.AUTOREPORT.REASON", "Automatischer Report");
                configcfg.set("AUTOMUTE.MUTEID", 0);
                configcfg.set("AUTOMUTE.ADMUTEID", 0);
                configcfg.set("BANTIME-INCREASE.ENABLED", true);
                configcfg.set("BANTIME-INCREASE.PERCENTRATE", 50);
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
                    configcfg.set("VPN.KICKMSG", "&7Das benutzen einer &4VPN &7ist auf unserem Netzwerk &cUNTERSAGT");
                    configcfg.set("VPN.BAN", false);
                    configcfg.set("VPN.BANID", 0);
                }
                for(String reasons : configcfg.getStringList("REPORTS.REASONS")){
                    reportreasons.add(reasons.toUpperCase());
                }
                for(String ips : configcfg.getStringList("VPN.WHITELIST")){
                    ipwhitelist.add(ips);
                }
                Prefix = configcfg.getString("PREFIX").replace("&", "§");
                increaseBans = configcfg.getBoolean("BANTIME-INCREASE.ENABLED");
                increaseValue = configcfg.getInt("BANTIME-INCREASE.PERCENTRATE");
                if(configcfg.getString("VPN.APIKEY").length() == 27){
                    APIKey = configcfg.getString("VPN.APIKEY");
                }
                if(!configcfg.getString("WEBINTERFACE.URL").equals("https://DeinServer.de/Webinterface")){
                    WebURL = configcfg.getString("WEBINTERFACE.URL");
                    if(!WebURL.startsWith("https://") || !WebURL.startsWith("http://")){
                        WebURL = "https://" + WebURL;
                    }
                    if(!WebURL.endsWith("/")){
                        WebURL = WebURL + "/";
                    }
                } else {
                    //==============================================
                    //Warnung über fehlende Einstellung
                    BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
                    BungeeCord.getInstance().getConsole().sendMessage("§4§lAchtung!");
                    BungeeCord.getInstance().getConsole().sendMessage("§cEs wurde festgestellt das du noch nicht die Webinterface URL in der §8§oconfig.yml §c§leingestellt hast.");
                    BungeeCord.getInstance().getConsole().sendMessage("§7Folgende Features werden nicht planmäßig funktionieren");
                    BungeeCord.getInstance().getConsole().sendMessage("§c§lChatlog-System und MSG-System");
                    BungeeCord.getInstance().getConsole().sendMessage("§8[]===================================[]");
                    //==============================================
                }
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
            }
            if(!blacklistfile.exists()){
                blacklistfile.createNewFile();
                Configuration blacklistcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(blacklistfile);
                adwhitelist.add("DeinServer.net");
                adwhitelist.add("forum.DeinServer.net");
                adwhitelist.add("ts.DeinServer.net");
                blacklistcfg.set("ADWHITELIST", adwhitelist);
                adblacklist.add(".com");
                adblacklist.add(".org");
                adblacklist.add(".net");
                adblacklist.add(".us");
                adblacklist.add(".co");
                adblacklist.add(".de");
                adblacklist.add(".biz");
                adblacklist.add(".info");
                adblacklist.add(".name");
                adblacklist.add(".yt");
                adblacklist.add(".tv");
                adblacklist.add(".xyz");
                adblacklist.add(".fr");
                adblacklist.add(".ch");
                adblacklist.add(".au");
                adblacklist.add(".at");
                adblacklist.add(".in");
                adblacklist.add(".jp");
                adblacklist.add(".nl");
                adblacklist.add(".uk");
                adblacklist.add(".no");
                adblacklist.add(".ru");
                adblacklist.add(".br");
                adblacklist.add(".tk");
                adblacklist.add(".ml");
                adblacklist.add(".ga");
                adblacklist.add(".cf");
                adblacklist.add(".gq");
                adblacklist.add(".ip");
                adblacklist.add(".dee");
                adblacklist.add(".d e");
                adblacklist.add("[punkt]");
                adblacklist.add("(punkt)");
                adblacklist.add("join now");
                adblacklist.add("join");
                adblacklist.add("mein server");
                adblacklist.add("mein netzwerk");
                adblacklist.add("www");
                adblacklist.add("[.]");
                adblacklist.add("(,)");
                adblacklist.add("(.)");
                blacklistcfg.set("ADBLACKLIST", adblacklist);
                blacklist.add("anal");
                blacklist.add("anus");
                blacklist.add("b1tch");
                blacklist.add("bang");
                blacklist.add("banger");
                blacklist.add("bastard");
                blacklist.add("biatch");
                blacklist.add("bitch");
                blacklist.add("bitches");
                blacklist.add("blow job");
                blacklist.add("blow");
                blacklist.add("blowjob");
                blacklist.add("boob");
                blacklist.add("boobs");
                blacklist.add("bullshit");
                blacklist.add("bull shit");
                blacklist.add("c0ck");
                blacklist.add("cock");
                blacklist.add("d1ck");
                blacklist.add("d1ld0");
                blacklist.add("d1ldo");
                blacklist.add("dick");
                blacklist.add("doggie-style");
                blacklist.add("doggy-style");
                blacklist.add("f.u.c.k");
                blacklist.add("fack");
                blacklist.add("faggit");
                blacklist.add("faggot");
                blacklist.add("fagot");
                blacklist.add("fuck");
                blacklist.add("f-u-c-k");
                blacklist.add("ficken");
                blacklist.add("fick");
                blacklist.add("fuckoff");
                blacklist.add("fucks");
                blacklist.add("fuk");
                blacklist.add("fvck");
                blacklist.add("fxck");
                blacklist.add("gai");
                blacklist.add("gay");
                blacklist.add("schwul");
                blacklist.add("schwuchtel");
                blacklist.add("h0m0");
                blacklist.add("h0mo");
                blacklist.add("hitler");
                blacklist.add("homo");
                blacklist.add("lesbe");
                blacklist.add("nigga");
                blacklist.add("niggah");
                blacklist.add("nigger");
                blacklist.add("nippel");
                blacklist.add("pedo");
                blacklist.add("pedo");
                blacklist.add("penis");
                blacklist.add("porn");
                blacklist.add("porno");
                blacklist.add("pornografie");
                blacklist.add("sex");
                blacklist.add("sh1t");
                blacklist.add("s-h-1-t");
                blacklist.add("shit");
                blacklist.add("s-h-i-t");
                blacklist.add("scheiße");
                blacklist.add("scheisse");
                blacklist.add("xxx");
                blacklist.add("Fotze");
                blacklist.add("Hackfresse");
                blacklist.add("Hurensohn");
                blacklist.add("Huso");
                blacklist.add("Hure");
                blacklist.add("hirnamputiert");
                blacklist.add("Honk");
                blacklist.add("kek");
                blacklist.add("Loser");
                blacklist.add("Mongo");
                blacklist.add("Pimmel");
                blacklist.add("Pimmelfresse");
                blacklist.add("Schlampe");
                blacklist.add("Spastard");
                blacklist.add("abspritzer");
                blacklist.add("afterlecker");
                blacklist.add("arschficker");
                blacklist.add("arschgeburt");
                blacklist.add("arschgeige");
                blacklist.add("arschgesicht");
                blacklist.add("arschlecker");
                blacklist.add("arschloch");
                blacklist.add("arschlöcher");
                blacklist.add("assi");
                blacklist.add("beklopter");
                blacklist.add("bummsen");
                blacklist.add("bumsen");
                blacklist.add("drecksack");
                blacklist.add("drecksau");
                blacklist.add("drecksfotze");
                blacklist.add("drecksnigger");
                blacklist.add("drecksnutte");
                blacklist.add("dreckspack");
                blacklist.add("dreckvotze");
                blacklist.add("fagette");
                blacklist.add("fagitt");
                blacklist.add("ficker");
                blacklist.add("fickfehler");
                blacklist.add("fickfresse");
                blacklist.add("fickgesicht");
                blacklist.add("ficknudel");
                blacklist.add("ficksau");
                blacklist.add("hackfresse");
                blacklist.add("lusche");
                blacklist.add("heil");
                blacklist.add("missgeburt");
                blacklist.add("mißgeburt");
                blacklist.add("miststück");
                blacklist.add("nazi");
                blacklist.add("nazis");
                blacklist.add("penner");
                blacklist.add("scheisser");
                blacklist.add("sieg heil");
                blacklist.add("vollidiot");
                blacklist.add("volldepp");
                blacklist.add("wanker");
                blacklist.add("wichser");
                blacklist.add("wichsvorlage");
                blacklist.add("wixa");
                blacklist.add("wixen");
                blacklist.add("wixer");
                blacklist.add("wixxer");
                blacklist.add("wixxxer");
                blacklist.add("wixxxxer");
                blacklistcfg.set("BLACKLIST", blacklist);
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(blacklistcfg, blacklistfile);
            } else {
                Configuration blacklistcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(blacklistfile);
                for(String congigstr : blacklistcfg.getStringList("BLACKLIST")){
                    blacklist.add(congigstr);
                }
                for(String congigstr : blacklistcfg.getStringList("ADBLACKLIST")){
                    adblacklist.add(congigstr);
                }
                for(String congigstr : blacklistcfg.getStringList("ADWHITELIST")){
                    adwhitelist.add(congigstr.toUpperCase());
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
        mysql.update("CREATE TABLE IF NOT EXISTS accounts(UUID varchar(64) UNIQUE, USERNAME varchar(255), PASSWORD varchar(255), RANK int(11), GOOGLE_AUTH varchar(255), AUTHCODE varchar(255));");
        mysql.update("CREATE TABLE IF NOT EXISTS reasons(ID int(11) UNIQUE, REASON varchar(255), TIME int(255), TYPE int(11), ADDED_AT varchar(11), BANS int(11), PERMS varchar(255));");
        mysql.update("CREATE TABLE IF NOT EXISTS bans(UUID varchar(64) UNIQUE, NAME varchar(64), BANNED int(11), MUTED int(11), REASON varchar(64), END long, TEAMUUID varchar(64), BANS int(11), MUTES int(11), FIRSTLOGIN varchar(255), LASTLOGIN varchar(255));");
        mysql.update("CREATE TABLE IF NOT EXISTS ips(IP varchar(64) UNIQUE, USED_BY varchar(64), USED_AT varchar(64), BANNED int(11), REASON varchar(64), END long, TEAMUUID varchar(64), BANS int(11));");
        mysql.update("CREATE TABLE IF NOT EXISTS reports(ID int(11) AUTO_INCREMENT UNIQUE, UUID varchar(64), REPORTER varchar(64), TEAM varchar(64), REASON varchar(64), LOG varchar(64), STATUS int(11), CREATED_AT long);");
        mysql.update("CREATE TABLE IF NOT EXISTS chat(ID int(11) AUTO_INCREMENT UNIQUE, UUID varchar(64), SERVER varchar(64), MESSAGE varchar(2500), SENDDATE varchar(255));");
        mysql.update("CREATE TABLE IF NOT EXISTS chatlog(ID int(11) AUTO_INCREMENT UNIQUE, LOGID varchar(255), UUID varchar(64), CREATOR_UUID varchar(64), SERVER varchar(64), MESSAGE varchar(2500), SENDDATE varchar(255), CREATED_AT varchar(255));");
        mysql.update("CREATE TABLE IF NOT EXISTS log(ID int(11) AUTO_INCREMENT UNIQUE, UUID varchar(255), BYUUID varchar(255), ACTION varchar(255), NOTE varchar(255), DATE varchar(255));");
        mysql.update("CREATE TABLE IF NOT EXISTS unbans(ID int(11) AUTO_INCREMENT UNIQUE, UUID varchar(255), FAIR int(11), MESSAGE varchar(10000), DATE varchar(255), STATUS int(11));");
        mysql.update("CREATE TABLE IF NOT EXISTS apptokens(UUID varchar(36) UNIQUE, TOKEN varchar(555));");
        mysql.update("CREATE TABLE IF NOT EXISTS privatemessages(ID int(11) AUTO_INCREMENT UNIQUE, SENDER varchar(255), RECEIVER varchar(255), MESSAGE varchar(2500), STATUS int(11), DATE varchar(255));");
        //SQL Update 2.0
        mysql.update("ALTER TABLE accounts ADD IF NOT EXISTS AUTHSTATUS int(11);");
        //SQL Update 2.2
        mysql.update("ALTER TABLE bans ADD IF NOT EXISTS FIRSTLOGIN varchar(255);");
        mysql.update("ALTER TABLE bans ADD IF NOT EXISTS LASTLOGIN varchar(255);");
        //SQL Update 2.4
        mysql.update("ALTER TABLE reasons ADD COLUMN SORTINDEX int(11)");
        //SQL Update 2.5
        mysql.update("ALTER TABLE apptokens ADD COLUMN FIREBASE_TOKEN varchar(255)");
        mysql.update("ALTER TABLE bans ADD ONLINE_STATUS int(11) NULL DEFAULT '0'");
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
            getProxy().getPluginManager().registerCommand(this, new SupportChat("support"));
            getProxy().getPluginManager().registerCommand(this, new PrivateMessage("msg"));
            getProxy().getPluginManager().registerCommand(this, new PrivateMessageReply("r"));
            getProxy().getPluginManager().registerCommand(this, new TeamChat("tc"));
            getProxy().getPluginManager().registerCommand(this, new Broadcast("bc"));
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
