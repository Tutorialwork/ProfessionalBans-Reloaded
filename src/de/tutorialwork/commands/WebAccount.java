package de.tutorialwork.commands;

import de.tutorialwork.main.Main;
import de.tutorialwork.utils.BCrypt;
import de.tutorialwork.utils.BanManager;
import de.tutorialwork.utils.LogManager;
import de.tutorialwork.utils.UUIDFetcher;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Random;

public class WebAccount extends Command {
    public WebAccount(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(p.hasPermission("professionalbans.webaccount") || p.hasPermission("professionalbans.*")){
                if(args.length == 0 || args.length == 1){
                    p.sendMessage(Main.Prefix+"/webaccount <erstellen, löschen> <Spieler> [Rang]");
                } else {
                    if(args[0].equalsIgnoreCase("erstellen")){
                        if(args.length == 2){
                            p.sendMessage(Main.Prefix+"Du musst noch ein Rang des Accounts angeben §4Admin§7, §cMod§7, §9Sup");
                            return;
                        }
                        String UUID = UUIDFetcher.getUUID(args[1]);
                        if(BanManager.playerExists(UUID)){
                            if(!BanManager.webaccountExists(UUID)){
                                ProxiedPlayer target = BungeeCord.getInstance().getPlayer(args[1]);
                                if(target != null){
                                    String rowPW = randomString(7);
                                    String Hash = BCrypt.hashpw(rowPW, BCrypt.gensalt());
                                    if(args[2].equalsIgnoreCase("Admin")){
                                        BanManager.createWebAccount(UUID, BanManager.getNameByUUID(UUID), 3, Hash);
                                        p.sendMessage(Main.Prefix+"Ein §4§lAdmin §7Account für §e§l"+BanManager.getNameByUUID(UUID)+" §7wurde §aerstellt");
                                    } else if(args[2].equalsIgnoreCase("Mod")){
                                        BanManager.createWebAccount(UUID, BanManager.getNameByUUID(UUID), 2, Hash);
                                        p.sendMessage(Main.Prefix+"Ein §c§lMod §7Account für §e§l"+BanManager.getNameByUUID(UUID)+" §7wurde §aerstellt");
                                    } else if(args[2].equalsIgnoreCase("Sup")){
                                        BanManager.createWebAccount(UUID, BanManager.getNameByUUID(UUID), 1, Hash);
                                        p.sendMessage(Main.Prefix+"Ein §9§lSup §7Account für §e§l"+BanManager.getNameByUUID(UUID)+" §7wurde §aerstellt");
                                    }
                                    target.sendMessage(Main.Prefix+"§e§l"+p.getName()+" §7hat einen Webaccount für dich erstellt");
                                    target.sendMessage(Main.Prefix+"Passwort: §c§l"+rowPW);
                                    LogManager.createEntry(UUID, p.getUniqueId().toString(), "ADD_WEBACCOUNT", args[2]);
                                } else {
                                    p.sendMessage(Main.Prefix+"§e§l"+args[1]+" §7ist derzeit nicht online");
                                }
                            } else {
                                p.sendMessage(Main.Prefix+"§cDieser Spieler hat bereits einen Zugang zum Webinterface");
                            }
                        } else {
                            p.sendMessage(Main.Prefix+"§cDieser Spieler hat den Server noch nie betreten");
                        }
                    } else if(args[0].equalsIgnoreCase("löschen")){
                        String UUID = UUIDFetcher.getUUID(args[1]);
                        if(BanManager.playerExists(UUID)){
                            if(BanManager.webaccountExists(UUID)){
                                BanManager.deleteWebAccount(UUID);
                                p.sendMessage(Main.Prefix+"Der Zugang von dem Spieler §e§l"+BanManager.getNameByUUID(UUID)+" §7wurde erfolgreich §agelöscht");
                                LogManager.createEntry(UUID, p.getUniqueId().toString(), "DEL_WEBACCOUNT", null);
                            } else {
                                p.sendMessage(Main.Prefix+"§cDieser Spieler hat keinen Zugang zum Webinterface");
                            }
                        } else {
                            p.sendMessage(Main.Prefix+"§cDieser Spieler hat den Server noch nie betreten");
                        }
                    } else {
                        p.sendMessage(Main.Prefix+"§cDiese Aktion ist nicht gültig");
                    }
                }
            } else {
                p.sendMessage(Main.NoPerms);
            }
        }
    }
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
    static Random rnd = new Random();

    private static String randomString(int length){
        StringBuilder sb = new StringBuilder(length);
        for(int i = 0; i < length; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }
}
