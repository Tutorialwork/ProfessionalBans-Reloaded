package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.LogManager;
import de.tutorialwork.professionalbans.utils.UUIDFetcher;
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
                    Main.ban.getBanReasonsList(p);
                    p.sendMessage(Main.data.Prefix+"/ban <"+Main.messages.getString("player")+"> <"+Main.messages.getString("reason")+"-ID>");
                } else {
                    String UUID = UUIDFetcher.getUUID(args[0]);
                    int ID;
                    try {
                        ID = Integer.valueOf(args[1]);
                    } catch (NumberFormatException e) {
                        p.sendMessage(Main.data.Prefix + Main.messages.getString("invalid_id"));
                        return;
                    }
                    if(Main.ban.playerExists(UUID)){
                        if(Main.ban.isWebaccountAdmin(UUID)){
                            p.sendMessage(Main.data.Prefix+Main.messages.getString("not_punishable"));
                            return;
                        }
                        if(Main.ban.getReasonByID(ID) != null){
                            Main.ban.setReasonBans(ID, Main.ban.getReasonBans(ID) + 1);
                            if(Main.ban.isBanReason(ID)){
                                if(Main.ban.hasExtraPerms(ID)){
                                    if(!p.hasPermission(Main.ban.getExtraPerms(ID))){
                                        p.sendMessage(Main.data.Prefix+Main.messages.getString("no_perm_ban"));
                                        return;
                                    }
                                }
                                Main.ban.ban(UUID, ID, p.getUniqueId().toString(), Main.data.increaseValue, Main.data.increaseBans);
                                LogManager.createEntry(UUID, p.getUniqueId().toString(), "BAN", String.valueOf(ID));
                                Main.ban.setBans(UUID, Main.ban.getBans(UUID) + 1);
                                Main.ban.sendNotify("BAN", Main.ban.getNameByUUID(UUID), p.getName(), Main.ban.getReasonByID(ID));
                                ProxiedPlayer banned = BungeeCord.getInstance().getPlayer(args[0]);
                                if(banned != null){
                                    File config = new File(Main.main.getDataFolder(), "config.yml");
                                    try {
                                        Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                                        if(Main.ban.getRAWEnd(banned.getUniqueId().toString()) == -1L){
                                            banned.disconnect(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.BAN").replace("%grund%", Main.ban.getReasonByID(ID)).replace("%ea-status%", Main.ban.getEAStatus(UUID))));
                                        } else {
                                            String MSG = configcfg.getString("LAYOUT.TEMPBAN");
                                            MSG = MSG.replace("%grund%", Main.ban.getReasonString(UUID));
                                            MSG = MSG.replace("%dauer%", Main.ban.getEnd(UUID));
                                            MSG = MSG.replace("%ea-status%", Main.ban.getEAStatus(UUID));
                                            banned.disconnect(ChatColor.translateAlternateColorCodes('&', MSG));
                                        }
                                        ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                if(Main.ban.hasExtraPerms(ID)){
                                    if(!p.hasPermission(Main.ban.getExtraPerms(ID))){
                                        p.sendMessage(Main.data.Prefix+Main.messages.getString("no_perm_mute"));
                                        return;
                                    }
                                }
                                Main.ban.mute(UUID, ID, p.getUniqueId().toString());
                                LogManager.createEntry(UUID, p.getUniqueId().toString(), "MUTE", String.valueOf(ID));
                                Main.ban.setMutes(UUID, Main.ban.getMutes(UUID) + 1);
                                Main.ban.sendNotify("MUTE", Main.ban.getNameByUUID(UUID), p.getName(), Main.ban.getReasonByID(ID));
                                ProxiedPlayer banned = BungeeCord.getInstance().getPlayer(args[0]);
                                if(banned != null){
                                    File config = new File(Main.main.getDataFolder(), "config.yml");
                                    try {
                                        Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                                        if(Main.ban.getRAWEnd(banned.getUniqueId().toString()) == -1L){
                                            banned.sendMessage(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.MUTE").replace("%grund%", Main.ban.getReasonByID(ID))));
                                        } else {
                                            String MSG = configcfg.getString("LAYOUT.TEMPMUTE");
                                            MSG = MSG.replace("%grund%", Main.ban.getReasonString(UUID));
                                            MSG = MSG.replace("%dauer%", Main.ban.getEnd(UUID));
                                            banned.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG));
                                        }
                                        ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            p.sendMessage(Main.data.Prefix+Main.messages.getString("reason_404"));
                        }
                    } else {
                        p.sendMessage(Main.data.Prefix+Main.messages.getString("player_404"));
                    }
                }
            } else {
                p.sendMessage(Main.data.NoPerms);
            }
        } else {
            if(args.length == 0 || args.length == 1){
                for(int zaehler = 1;zaehler < Main.ban.countReasons()+1;zaehler++) {
                    if(Main.ban.isBanReason(zaehler)){
                        BungeeCord.getInstance().getConsole().sendMessage("§7"+zaehler+" §8| §e"+Main.ban.getReasonByID(zaehler));
                    } else {
                        BungeeCord.getInstance().getConsole().sendMessage("§7"+zaehler+" §8| §e"+Main.ban.getReasonByID(zaehler)+" §8(§cMUTE§8)");
                    }
                }
                BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+"/ban <"+Main.messages.getString("player")+"> <"+Main.messages.getString("reason")+"-ID>");
            } else {
                String UUID = UUIDFetcher.getUUID(args[0]);
                int ID = Integer.valueOf(args[1]);
                if(Main.ban.playerExists(UUID)){
                    if(Main.ban.isWebaccountAdmin(UUID)){
                        BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("not_punishable"));
                        return;
                    }
                    if(Main.ban.getReasonByID(ID) != null){
                        Main.ban.setReasonBans(ID, Main.ban.getReasonBans(ID) + 1);
                        if(Main.ban.isBanReason(ID)){
                            Main.ban.ban(UUID, ID, "KONSOLE", Main.data.increaseValue, Main.data.increaseBans);
                            LogManager.createEntry(UUID, "KONSOLE", "BAN", String.valueOf(ID));
                            Main.ban.setBans(UUID, Main.ban.getBans(UUID) + 1);
                            Main.ban.sendNotify("BAN", Main.ban.getNameByUUID(UUID), "KONSOLE", Main.ban.getReasonByID(ID));
                            ProxiedPlayer banned = BungeeCord.getInstance().getPlayer(args[0]);
                            if(banned != null){
                                File config = new File(Main.main.getDataFolder(), "config.yml");
                                try {
                                    Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                                    if(Main.ban.getRAWEnd(banned.getUniqueId().toString()) == -1L){
                                        banned.disconnect(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.BAN").replace("%grund%", Main.ban.getReasonByID(ID)).replace("%ea-status%", Main.ban.getEAStatus(UUID))));
                                    } else {
                                        String MSG = configcfg.getString("LAYOUT.TEMPBAN");
                                        MSG = MSG.replace("%grund%", Main.ban.getReasonString(UUID));
                                        MSG = MSG.replace("%dauer%", Main.ban.getEnd(UUID));
                                        MSG = MSG.replace("%ea-status%", Main.ban.getEAStatus(UUID));
                                        banned.disconnect(ChatColor.translateAlternateColorCodes('&', MSG));
                                    }
                                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Main.ban.mute(UUID, ID, "KONSOLE");
                            LogManager.createEntry(UUID, "KONSOLE", "MUTE", String.valueOf(ID));
                            Main.ban.setMutes(UUID, Main.ban.getMutes(UUID) + 1);
                            Main.ban.sendNotify("MUTE", Main.ban.getNameByUUID(UUID), "KONSOLE", Main.ban.getReasonByID(ID));
                            ProxiedPlayer banned = BungeeCord.getInstance().getPlayer(args[0]);
                            if(banned != null){
                                File config = new File(Main.main.getDataFolder(), "config.yml");
                                try {
                                    Configuration configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                                    if(Main.ban.getRAWEnd(banned.getUniqueId().toString()) == -1L){
                                        banned.sendMessage(ChatColor.translateAlternateColorCodes('&', configcfg.getString("LAYOUT.MUTE").replace("%grund%", Main.ban.getReasonByID(ID))));
                                    } else {
                                        String MSG = configcfg.getString("LAYOUT.TEMPMUTE");
                                        MSG = MSG.replace("%grund%", Main.ban.getReasonString(UUID));
                                        MSG = MSG.replace("%dauer%", Main.ban.getEnd(UUID));
                                        banned.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG));
                                    }
                                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, config);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("reason_404"));
                    }
                } else {
                    BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("player_404"));
                }
            }
        }
    }
}
