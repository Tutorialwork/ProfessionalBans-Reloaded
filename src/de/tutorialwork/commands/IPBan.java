package de.tutorialwork.commands;

import de.tutorialwork.main.Main;
import de.tutorialwork.utils.BanManager;
import de.tutorialwork.utils.IPManager;
import de.tutorialwork.utils.LogManager;
import de.tutorialwork.utils.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class IPBan extends Command {
    public IPBan(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(p.hasPermission("professionalbans.ipban") || p.hasPermission("professionalbans.*")){
                if(args.length == 0 || args.length == 1){
                    for(int zaehler = 1; zaehler < BanManager.countReasons()+1; zaehler++) {
                        if(BanManager.isBanReason(zaehler)){
                            p.sendMessage("§7"+zaehler+" §8| §e"+BanManager.getReasonByID(zaehler));
                        }
                    }
                    p.sendMessage(Main.Prefix+"/ipban <IP/Spieler> <Grund-ID>");
                } else {
                    String IP = args[0];
                    int ID = Integer.valueOf(args[1]);
                    if(validate(IP)){
                        if(BanManager.getReasonByID(ID) != null){
                            if(IPManager.IPExists(IP)){
                                IPManager.ban(IP, ID, p.getUniqueId().toString());
                                IPManager.addBan(IP);
                                BanManager.sendNotify("IPBAN", IP, p.getName(), BanManager.getReasonByID(ID));
                            } else {
                                IPManager.insertIP(IP, null);
                                IPManager.ban(IP, ID, p.getUniqueId().toString());
                                IPManager.addBan(IP);
                                BanManager.sendNotify("IPBAN", IP, p.getName(), BanManager.getReasonByID(ID));
                            }
                            disconnectIPBannedPlayers(IP);
                            LogManager.createEntry(null, p.getUniqueId().toString(), "IPBAN_IP", IP);
                        } else {
                            p.sendMessage(Main.Prefix+"§cDieser Grund existiert nicht");
                        }
                    } else {
                        String UUID = UUIDFetcher.getUUID(args[0]);
                        if(IPManager.getIPFromPlayer(UUID) != null){
                            String DBIP = IPManager.getIPFromPlayer(UUID);
                            IPManager.ban(DBIP, ID, p.getUniqueId().toString());
                            IPManager.addBan(DBIP);
                            BanManager.sendNotify("IPBAN", DBIP, p.getName(), BanManager.getReasonByID(ID));
                            disconnectIPBannedPlayers(DBIP);
                            LogManager.createEntry(UUID, p.getUniqueId().toString(), "IPBAN_PLAYER", String.valueOf(ID));
                        } else {
                            p.sendMessage(Main.Prefix+"§cZu diesem Spieler ist keine IP-Adresse gespeichert");
                        }
                    }
                }
            } else {
                p.sendMessage(Main.NoPerms);
            }
        }
    }

    public static void disconnectIPBannedPlayers(String IP){
        for(ProxiedPlayer all : Main.getInstance().getProxy().getPlayers()){
            if(all.getAddress().getHostString().equals(IP)){
                File config = new File(Main.main.getDataFolder(), "config.yml");
                try {
                    Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);

                    if(IPManager.getRAWEnd(IP) == -1L){
                        all.disconnect(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.IPBAN").replace("%grund%", IPManager.getReasonString(IP))));
                    } else {
                        if(System.currentTimeMillis() < IPManager.getRAWEnd(IP)){
                            String MSG = configcfg.getString("LAYOUT.TEMPIPBAN");
                            MSG = MSG.replace("%grund%", IPManager.getReasonString(IP));
                            MSG = MSG.replace("%dauer%", IPManager.getEnd(IP));
                            all.disconnect(ChatColor.translateAlternateColorCodes('&', MSG));
                        } else {
                            IPManager.unban(IP);
                        }
                    }

                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }
}
