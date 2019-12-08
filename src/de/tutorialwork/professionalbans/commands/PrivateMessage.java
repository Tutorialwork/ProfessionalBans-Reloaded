package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.MessagesManager;
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

public class PrivateMessage extends Command {
    public PrivateMessage(String msg) {
        super(msg);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(args.length > 1){
                ProxiedPlayer target = BungeeCord.getInstance().getPlayer(args[0]);
                if(target != null){
                    String message = "";
                    for(int i = 1; i < args.length; i++){
                        message = message + " " + args[i];
                    }
                    MessagesManager.sendMessage(p, target, message);
                } else {
                    if(MessagesManager.hasApp(UUIDFetcher.getUUID(args[0]))){
                        try{
                            File file = new File(Main.main.getDataFolder(), "config.yml");
                            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

                            String message = "";
                            for(int i = 1; i < args.length; i++){
                                message = message + " " + args[i];
                            }
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getString("CHATFORMAT.MSG").replace("%from%", "Du").replace("%message%", message)));
                            MessagesManager.insertMessage(p.getUniqueId().toString(), UUIDFetcher.getUUID(args[0]), message);
                            if(MessagesManager.getFirebaseToken(UUIDFetcher.getUUID(args[0])) != null){
                                MessagesManager.sendPushNotify(MessagesManager.getFirebaseToken(UUIDFetcher.getUUID(args[0])), "Nachricht von "+p.getName(), message);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        p.sendMessage(Main.Prefix+"§cDieser Spieler ist zur Zeit offline");
                    }
                }
            } else {
                p.sendMessage(Main.Prefix+"/msg <Spieler> <Nachricht>");
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§cDieser Befehl kann nur als Spieler genutzt werden");
        }
    }
}
