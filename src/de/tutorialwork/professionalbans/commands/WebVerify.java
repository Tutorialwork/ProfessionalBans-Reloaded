package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.BanManager;
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
                                p.sendMessage(Main.Prefix+Main.messages.getString("webverify_success"));
                            } else {
                                p.sendMessage(Main.Prefix+Main.messages.getString("token_invalid"));
                            }
                        } else {
                            p.sendMessage(Main.Prefix+Main.messages.getString("token_invalid"));
                        }
                    } else {
                        p.sendMessage(Main.Prefix+"§cEs wurde keine Verifizierungsanfrage von dir gefunden");
                    }
                } else {
                    p.sendMessage(Main.Prefix+"§cDu hast keinen Account im Webinterface");
                }
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+Main.messages.getString("only_player_cmd"));
        }
    }
}
