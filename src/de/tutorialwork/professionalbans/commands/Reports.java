package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
import de.tutorialwork.professionalbans.utils.BanManager;
import de.tutorialwork.professionalbans.utils.LogManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;

public class Reports extends Command {

    public static ArrayList<ProxiedPlayer> not_logged = new ArrayList<>();

    public Reports(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(p.hasPermission("professionalbans.reports") || p.hasPermission("professionalbans.*")){
                if(args.length == 0){
                    if(BanManager.countOpenReports() != 0){
                        p.sendMessage("§8[]===================================[]");
                        p.sendMessage("§e§loffene Reports §7(§8"+BanManager.countOpenReports()+"§7)");
                        int offline = 0;
                        for(int i = 0; i < BanManager.getIDsFromOpenReports().size(); ++i) {
                            int id = (int) BanManager.getIDsFromOpenReports().get(i);
                            ProxiedPlayer target = BungeeCord.getInstance().getPlayer(BanManager.getNameByReportID(id));
                            if(target != null){
                                TextComponent tc = new TextComponent();
                                tc.setText("§e§l"+target.getName()+" §7gemeldet wegen §c§l "+BanManager.getReasonByReportID(id)+" §8| §7Online auf §e§l"+target.getServer().getInfo().getName());
                                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reports jump "+target.getName()+" "+id));
                                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Klicken um §e§l"+target.getName()+" §7nachzuspringen").create()));
                                p.sendMessage(tc);
                            } else {
                                offline++;
                            }
                        }
                        if(offline != 0){
                            p.sendMessage("§4§o"+offline+" Reports §7§ovon Spieler die offline sind ausgeblendet");
                        }
                        p.sendMessage("§8[]===================================[]");
                    } else {
                        p.sendMessage(Main.Prefix+"§cEs sind derzeit keine Reports offen");
                    }
                } else if(args[0].equalsIgnoreCase("jump")){
                    ProxiedPlayer target = BungeeCord.getInstance().getPlayer(args[1]);
                    if(target != null){
                        p.connect(target.getServer().getInfo());
                        int id = Integer.parseInt(args[2]);
                        BanManager.setReportDone(id);
                        BanManager.setReportTeamUUID(id, p.getUniqueId().toString());
                        p.sendMessage(Main.Prefix+"Du hast den Report von §e§l"+BanManager.getNameByReportID(id)+" §7wegen §c§l"+BanManager.getReasonByReportID(id)+" §aangenommen");
                        LogManager.createEntry(p.getUniqueId().toString(), null, "REPORT_ACCEPT", String.valueOf(id));
                    } else {
                        p.sendMessage(Main.Prefix+"§cDieser Spieler ist nicht mehr online");
                    }
                } else if(args[0].equalsIgnoreCase("toggle")){
                    if(not_logged.contains(p)){
                        not_logged.remove(p);
                        p.sendMessage(Main.Prefix+"§a§lDu wirst nun wieder von dem System über ankommende Reports benachrichtigt");
                    } else {
                        not_logged.add(p);
                        p.sendMessage(Main.Prefix+"§cDu wirst nun temporär über §c§lkeine §cneuen Reports benachrichtigt");
                    }
                }
            } else {
                p.sendMessage(Main.NoPerms);
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"§e§lReports §7sind nur als Spieler verfügbar");
        }
    }
}
