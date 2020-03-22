package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.LogManager;
import de.tutorialwork.professionalbans.utils.UUIDFetcher;
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
                    for(int zaehler = 1; zaehler < Main.ban.countReasons()+1; zaehler++) {
                        if(Main.ban.isBanReason(zaehler)){
                            p.sendMessage("§7"+zaehler+" §8| §e"+Main.ban.getReasonByID(zaehler));
                        }
                    }
                    p.sendMessage(Main.data.Prefix+"/ipban <IP/"+Main.messages.getString("player")+"> <"+Main.messages.getString("reason")+"-ID>");
                } else {
                    String IP = args[0];
                    int ID = Integer.valueOf(args[1]);
                    if(validate(IP)){
                        if(Main.ban.getReasonByID(ID) != null){
                            if(Main.ip.IPExists(IP)){
                                Main.ip.ban(IP, ID, p.getUniqueId().toString());
                                Main.ip.addBan(IP);
                                Main.ban.sendNotify("IPBAN", IP, p.getName(), Main.ban.getReasonByID(ID));
                            } else {
                                Main.ip.insertIP(IP, null);
                                Main.ip.ban(IP, ID, p.getUniqueId().toString());
                                Main.ip.addBan(IP);
                                Main.ban.sendNotify("IPBAN", IP, p.getName(), Main.ban.getReasonByID(ID));
                            }
                            disconnectIPBannedPlayers(IP);
                            LogManager.createEntry(null, p.getUniqueId().toString(), "IPBAN_IP", IP);
                        } else {
                            p.sendMessage(Main.data.Prefix+Main.messages.getString("reason_404"));
                        }
                    } else {
                        String UUID = UUIDFetcher.getUUID(args[0]);
                        if(Main.ip.getIPFromPlayer(UUID) != null){
                            String DBIP = Main.ip.getIPFromPlayer(UUID);
                            Main.ip.ban(DBIP, ID, p.getUniqueId().toString());
                            Main.ip.addBan(DBIP);
                            Main.ban.sendNotify("IPBAN", DBIP, p.getName(), Main.ban.getReasonByID(ID));
                            disconnectIPBannedPlayers(DBIP);
                            LogManager.createEntry(UUID, p.getUniqueId().toString(), "IPBAN_PLAYER", String.valueOf(ID));
                        } else {
                            p.sendMessage(Main.data.Prefix+Main.messages.getString("player_ip_404"));
                        }
                    }
                }
            } else {
                p.sendMessage(Main.data.NoPerms);
            }
        }
    }

    public static void disconnectIPBannedPlayers(String IP){
        for(ProxiedPlayer all : Main.getInstance().getProxy().getPlayers()){
            if(all.getAddress().getHostString().equals(IP)){
                File config = new File(Main.main.getDataFolder(), "config.yml");
                try {
                    Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);

                    if(Main.ip.getRAWEnd(IP) == -1L){
                        all.disconnect(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.IPBAN").replace("%grund%", Main.ip.getReasonString(IP))));
                    } else {
                        if(System.currentTimeMillis() < Main.ip.getRAWEnd(IP)){
                            String MSG = configcfg.getString("LAYOUT.TEMPIPBAN");
                            MSG = MSG.replace("%grund%", Main.ip.getReasonString(IP));
                            MSG = MSG.replace("%dauer%", Main.ip.getEnd(IP));
                            all.disconnect(ChatColor.translateAlternateColorCodes('&', MSG));
                        } else {
                            Main.ip.unban(IP);
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
