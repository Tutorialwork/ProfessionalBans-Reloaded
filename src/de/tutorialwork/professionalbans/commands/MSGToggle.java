package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;

public class MSGToggle extends Command {
    public MSGToggle(String name) {
        super(name);
    }

    public static ArrayList<ProxiedPlayer> toggle = new ArrayList<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(toggle.contains(p)){
                toggle.remove(p);
                p.sendMessage(Main.data.Prefix+Main.messages.getString("msg_toggle_on"));
            } else {
                toggle.add(p);
                p.sendMessage(Main.data.Prefix+Main.messages.getString("msg_toggle_off"));
            }
        } else {
            System.out.println(Main.messages.getString("only_player_cmd"));
        }
    }
}
