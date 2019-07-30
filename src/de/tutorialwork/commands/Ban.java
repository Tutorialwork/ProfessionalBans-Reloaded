package de.tutorialwork.commands;

import de.tutorialwork.main.Main;
import de.tutorialwork.utils.BanManager;
import de.tutorialwork.utils.LogManager;
import de.tutorialwork.utils.UUIDFetcher;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Ban extends Command {
    public Ban(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(p.hasPermission("professionalbans.ban") || p.hasPermission("professionalbans.*")){
                if(args.length == 0 || args.length == 1){
                    for(int zaehler = 1;zaehler < BanManager.countReasons()+1;zaehler++) {
                        if(BanManager.isBanReason(zaehler)){
                            p.sendMessage("§7"+zaehler+" §8| §e"+BanManager.getReasonByID(zaehler));
                        } else {
                            p.sendMessage("§7"+zaehler+" §8| §e"+BanManager.getReasonByID(zaehler)+" §8(§cMUTE§8)");
                        }
                    }
                    p.sendMessage(Main.Prefix+"/ban <Spieler> <Grund-ID>");
                } else {
                    String UUID = UUIDFetcher.getUUID(args[0]);
                    int ID = Integer.valueOf(args[1]);
                    if(BanManager.playerExists(UUID)){
                        if(BanManager.isWebaccountAdmin(UUID)){
                            p.sendMessage(Main.Prefix+"§cDiesen Spieler kannst du nicht bannen/muten");
                            return;
                        }
                        if(BanManager.getReasonByID(ID) != null){
                            BanManager.setReasonBans(ID, BanManager.getReasonBans(ID) + 1);
                            if(BanManager.isBanReason(ID)){
                                if(BanManager.hasExtraPerms(ID)){
                                    if(!p.hasPermission(BanManager.getExtraPerms(ID))){
                                        p.sendMessage(Main.Prefix+"§cDu hast keine Berechtigung diesen Bangrund zu nutzen");
                                        return;
                                    }
                                }
                                BanManager.ban(UUID, ID, p.getUniqueId().toString(), Main.increaseValue, Main.increaseBans);
                                LogManager.createEntry(UUID, p.getUniqueId().toString(), "BAN", String.valueOf(ID));
                                BanManager.setBans(UUID, BanManager.getBans(UUID) + 1);
                                BanManager.sendNotify("BAN", BanManager.getNameByUUID(UUID), p.getName(), BanManager.getReasonByID(ID));
                                ProxiedPlayer banned = BungeeCord.getInstance().getPlayer(args[0]);
                                if(banned != null){
                                    File config = new File(Main.main.getDataFolder(), "config.yml");
                                    try {
                                        Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                                        if(BanManager.getRAWEnd(banned.getUniqueId().toString()) == -1L){
                                            banned.disconnect(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.BAN").replace("%grund%", BanManager.getReasonByID(ID))));
                                        } else {
                                            String MSG = configcfg.getString("LAYOUT.TEMPBAN");
                                            MSG = MSG.replace("%grund%", BanManager.getReasonString(UUID));
                                            MSG = MSG.replace("%dauer%", BanManager.getEnd(UUID));
                                            banned.disconnect(ChatColor.translateAlternateColorCodes('&', MSG));
                                        }
                                        ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                if(BanManager.hasExtraPerms(ID)){
                                    if(!p.hasPermission(BanManager.getExtraPerms(ID))){
                                        p.sendMessage(Main.Prefix+"§cDu hast keine Berechtigung diesen Mutegrund zu nutzen");
                                        return;
                                    }
                                }
                                BanManager.mute(UUID, ID, p.getUniqueId().toString());
                                LogManager.createEntry(UUID, p.getUniqueId().toString(), "MUTE", String.valueOf(ID));
                                BanManager.setMutes(UUID, BanManager.getMutes(UUID) + 1);
                                BanManager.sendNotify("MUTE", BanManager.getNameByUUID(UUID), p.getName(), BanManager.getReasonByID(ID));
                                ProxiedPlayer banned = BungeeCord.getInstance().getPlayer(args[0]);
                                if(banned != null){
                                    File config = new File(Main.main.getDataFolder(), "config.yml");
                                    try {
                                        Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                                        if(BanManager.getRAWEnd(banned.getUniqueId().toString()) == -1L){
                                            banned.sendMessage(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.MUTE").replace("%grund%", BanManager.getReasonByID(ID))));
                                        } else {
                                            String MSG = configcfg.getString("LAYOUT.TEMPMUTE");
                                            MSG = MSG.replace("%grund%", BanManager.getReasonString(UUID));
                                            MSG = MSG.replace("%dauer%", BanManager.getEnd(UUID));
                                            banned.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG));
                                        }
                                        ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            p.sendMessage(Main.Prefix+"§cDieser Grund existiert nicht");
                        }
                    } else {
                        p.sendMessage(Main.Prefix+"§cDieser Spieler hat den Server noch nie betreten");
                    }
                }
            } else {
                p.sendMessage(Main.NoPerms);
            }
        } else {
            if(args.length == 0 || args.length == 1){
                for(int zaehler = 1;zaehler < BanManager.countReasons()+1;zaehler++) {
                    if(BanManager.isBanReason(zaehler)){
                        BungeeCord.getInstance().getConsole().sendMessage("§7"+zaehler+" §8| §e"+BanManager.getReasonByID(zaehler));
                    } else {
                        BungeeCord.getInstance().getConsole().sendMessage("§7"+zaehler+" §8| §e"+BanManager.getReasonByID(zaehler)+" §8(§cMUTE§8)");
                    }
                }
                BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"/ban <Spieler> <Grund-ID>");
            } else {
                String UUID = UUIDFetcher.getUUID(args[0]);
                int ID = Integer.valueOf(args[1]);
                if(BanManager.playerExists(UUID)){
                    if(BanManager.isWebaccountAdmin(UUID)){
                        BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§cDiesen Spieler kannst du nicht bannen/muten");
                        return;
                    }
                    if(BanManager.getReasonByID(ID) != null){
                        BanManager.setReasonBans(ID, BanManager.getReasonBans(ID) + 1);
                        if(BanManager.isBanReason(ID)){
                            BanManager.ban(UUID, ID, "KONSOLE", Main.increaseValue, Main.increaseBans);
                            LogManager.createEntry(UUID, "KONSOLE", "BAN", String.valueOf(ID));
                            BanManager.setBans(UUID, BanManager.getBans(UUID) + 1);
                            BanManager.sendNotify("BAN", BanManager.getNameByUUID(UUID), "KONSOLE", BanManager.getReasonByID(ID));
                            ProxiedPlayer banned = BungeeCord.getInstance().getPlayer(args[0]);
                            if(banned != null){
                                File config = new File(Main.main.getDataFolder(), "config.yml");
                                try {
                                    Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                                    if(BanManager.getRAWEnd(banned.getUniqueId().toString()) == -1L){
                                        banned.disconnect(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.BAN").replace("%grund%", BanManager.getReasonByID(ID))));
                                    } else {
                                        String MSG = configcfg.getString("LAYOUT.TEMPBAN");
                                        MSG = MSG.replace("%grund%", BanManager.getReasonString(UUID));
                                        MSG = MSG.replace("%dauer%", BanManager.getEnd(UUID));
                                        banned.disconnect(ChatColor.translateAlternateColorCodes('&', MSG));
                                    }
                                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            BanManager.mute(UUID, ID, "KONSOLE");
                            LogManager.createEntry(UUID, "KONSOLE", "MUTE", String.valueOf(ID));
                            BanManager.setMutes(UUID, BanManager.getMutes(UUID) + 1);
                            BanManager.sendNotify("MUTE", BanManager.getNameByUUID(UUID), "KONSOLE", BanManager.getReasonByID(ID));
                            ProxiedPlayer banned = BungeeCord.getInstance().getPlayer(args[0]);
                            if(banned != null){
                                File config = new File(Main.main.getDataFolder(), "config.yml");
                                try {
                                    Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                                    if(BanManager.getRAWEnd(banned.getUniqueId().toString()) == -1L){
                                        banned.sendMessage(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.MUTE").replace("%grund%", BanManager.getReasonByID(ID))));
                                    } else {
                                        String MSG = configcfg.getString("LAYOUT.TEMPMUTE");
                                        MSG = MSG.replace("%grund%", BanManager.getReasonString(UUID));
                                        MSG = MSG.replace("%dauer%", BanManager.getEnd(UUID));
                                        banned.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG));
                                    }
                                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§cDieser Grund existiert nicht");
                    }
                } else {
                    BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§cDieser Spieler hat den Server noch nie betreten");
                }
            }
        }
    }
}
