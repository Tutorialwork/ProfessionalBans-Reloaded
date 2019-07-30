package de.tutorialwork.commands;

import de.tutorialwork.main.Main;
import de.tutorialwork.utils.BanManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class WebVerify extends Command {
    public WebVerify(String cmd) {
        super(cmd);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(args.length == 0){
                p.sendMessage(Main.Prefix+"/webverify <Token>");
            } else {
                String UUID = p.getUniqueId().toString();
                if(BanManager.webaccountExists(UUID)){
                    if(BanManager.hasAuthToken(UUID)){
                        if(args[0].length() == 25){
                            if(BanManager.getAuthCode(UUID).equals(args[0])){
                                BanManager.updateAuthStatus(UUID);
                                p.sendMessage(Main.Prefix+"§a§lErfolgreich! §7Du kannst jetzt dein Passwort festlegen.");
                            } else {
                                p.sendMessage(Main.Prefix+"§cDer eingegebene Token ist ungültig");
                            }
                        } else {
                            p.sendMessage(Main.Prefix+"§cDer eingegebene Token ist ungültig");
                        }
                    } else {
                        p.sendMessage(Main.Prefix+"§cEs wurde keine Verifizierungsanfrage von dir gefunden");
                    }
                } else {
                    p.sendMessage(Main.Prefix+"§cDu hast keinen Account im Webinterface");
                }
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§cDieser Befehl ist nur als Spieler nutzbar");
        }
    }
}
