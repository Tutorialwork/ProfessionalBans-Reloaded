package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.BCrypt;
import de.tutorialwork.professionalbans.utils.BanManager;
import de.tutorialwork.professionalbans.utils.LogManager;
import de.tutorialwork.professionalbans.utils.UUIDFetcher;
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
                    p.sendMessage(Main.Prefix+"/webaccount <"+Main.messages.getString("create")+", "+Main.messages.getString("delete")+"> <"+Main.messages.getString("player")+"> ["+Main.messages.getString("rank")+"]");
                } else {
                    if(args[0].equalsIgnoreCase("erstellen") || args[0].equalsIgnoreCase("create")){
                        if(args.length == 2){
                            p.sendMessage(Main.Prefix+Main.messages.getString("webaccount_syntax"));
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
                                    } else if(args[2].equalsIgnoreCase("Mod")){
                                        BanManager.createWebAccount(UUID, BanManager.getNameByUUID(UUID), 2, Hash);
                                    } else if(args[2].equalsIgnoreCase("Sup")){
                                        BanManager.createWebAccount(UUID, BanManager.getNameByUUID(UUID), 1, Hash);
                                    }
                                    p.sendMessage(Main.Prefix+Main.messages.getString("webaccount_created").replace("%player%", BanManager.getNameByUUID(UUID)));
                                    target.sendMessage(Main.Prefix+"§e§l"+p.getName()+" "+Main.messages.getString("webaccount_created_success"));
                                    target.sendMessage(Main.Prefix+Main.messages.getString("password")+": §c§l"+rowPW);
                                    LogManager.createEntry(UUID, p.getUniqueId().toString(), "ADD_WEBACCOUNT", args[2]);
                                } else {
                                    p.sendMessage(Main.Prefix+Main.messages.getString("player_404"));
                                }
                            } else {
                                p.sendMessage(Main.Prefix+Main.messages.getString("webaccount_already"));
                            }
                        } else {
                            p.sendMessage(Main.Prefix+Main.messages.getString("player_404"));
                        }
                    } else if(args[0].equalsIgnoreCase("löschen") || args[0].equalsIgnoreCase("delete")){
                        String UUID = UUIDFetcher.getUUID(args[1]);
                        if(BanManager.playerExists(UUID)){
                            if(BanManager.webaccountExists(UUID)){
                                BanManager.deleteWebAccount(UUID);
                                p.sendMessage(Main.Prefix+Main.messages.getString("webaccount_deleted").replace("%player%", BanManager.getNameByUUID(UUID)));
                                LogManager.createEntry(UUID, p.getUniqueId().toString(), "DEL_WEBACCOUNT", null);
                            } else {
                                p.sendMessage(Main.Prefix+Main.messages.getString("webaccount_404"));
                            }
                        } else {
                            p.sendMessage(Main.Prefix+Main.messages.getString("player_404"));
                        }
                    } else {
                        p.sendMessage(Main.Prefix+Main.messages.getString("webaccount_syntax"));
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
