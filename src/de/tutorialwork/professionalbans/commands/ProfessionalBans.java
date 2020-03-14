package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ProfessionalBans extends Command {
    public ProfessionalBans(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer p = (ProxiedPlayer) sender;
        p.sendMessage("");
        p.sendMessage("§8[]===================================[]");
        p.sendMessage("§e§lProfessionalBans §7§oReloaded §8• §7Version §8» §c"+ Main.Version);
        p.sendMessage("§7Developer §8» §e§lTutorialwork");
        p.sendMessage("§5YT §7"+Main.messages.getString("channel")+" §8» §cyoutube.com/Tutorialwork");
        p.sendMessage("§8[]===================================[]");
        p.sendMessage("");
    }
}
