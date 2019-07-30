package de.tutorialwork.commands;

import de.tutorialwork.main.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;

public class SupportChat extends Command {
    public SupportChat(String name) {
        super(name);
    }

    public static HashMap<ProxiedPlayer, String> openchats = new HashMap<>();
    public static HashMap<ProxiedPlayer, ProxiedPlayer> activechats = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(args.length != 0){
                if(args[0].equalsIgnoreCase("end")){
                    if(activechats.containsValue(p) || activechats.containsKey(p)){
                        for(ProxiedPlayer key : SupportChat.activechats.keySet()){
                            //Key has started the support chat
                            if(key == p){
                                SupportChat.activechats.get(p).sendMessage(Main.Prefix+"§e§l"+p.getName()+" §7hat den Support Chat §cbeeendet");
                                activechats.remove(key);
                            } else {
                                key.sendMessage(Main.Prefix+"§e§l"+p.getName()+" §7hat den Support Chat §cbeeendet");
                                activechats.remove(key);
                            }
                        }
                        p.sendMessage(Main.Prefix+"§cDu hast den Support Chat beendet");
                        return;
                    } else {
                        p.sendMessage(Main.Prefix+"§cDu hast derzeit keinen offenen Support Chat");
                        return;
                    }
                }
            }
            if(p.hasPermission("professionalbans.supportchat") || p.hasPermission("professionalbans.*")){
                //Team Member
                if(args.length > 0){
                    for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                        if(all.getName().equals(args[0])){
                            if (openchats.containsKey(all)) {
                                activechats.put(all, p);
                                openchats.remove(all);
                                all.sendMessage(Main.Prefix+"§e§l"+p.getName()+" §7ist jetzt mit dir im Support Chat");
                                all.sendMessage(Main.Prefix+"§8§oDu kannst in den Support Chat schreiben in dem du einfach eine normale Nachricht schreibst");
                                all.sendMessage(Main.Prefix+"§8§oDu kannst den Support Chat mit §7§o/support end §8§obeenden");
                                p.sendMessage(Main.Prefix+"§e§l"+all.getName()+" §7ist jetzt im Support Chat mit dir");
                                p.sendMessage(Main.Prefix+"§8§oDu kannst den Support Chat mit §7§o/support end §8§obeenden");
                            } else {
                                p.sendMessage(Main.Prefix+"§cDiese Anfrage ist ausgelaufen");
                            }
                        }
                    }
                } else {
                    if(SupportChat.openchats.size() != 0){
                        p.sendMessage("§8[]===================================[]");
                        int i = 0;
                        for(ProxiedPlayer key : SupportChat.openchats.keySet()){
                            p.sendMessage("§e§l"+key+" §8• §9"+SupportChat.openchats.get(key));
                            TextComponent tc = new TextComponent();
                            tc.setText("§aSupport Chat starten");
                            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/support "+key));
                            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Klicken um den Chat mit §e§l"+key+" §7zu starten").create()));
                            p.sendMessage(tc);
                            i++;
                        }
                        p.sendMessage("§8[]===================================[]");
                        p.sendMessage(Main.Prefix+"Es sind derzeit §e§l"+i+" Support Chats §7Anfragen §aoffen");
                    } else {
                        p.sendMessage(Main.Prefix+"§cDerzeit sind keine Support Chats Anfragen offen");
                    }
                }
            } else {
                //Normal Member
                if(args.length == 0){
                    p.sendMessage(Main.Prefix+"Wenn du den §e§lSupport Chat §7starten möchtest gebe ein §8§oBetreff §7ein");
                    p.sendMessage(Main.Prefix+"Möchtest du eine Anfrage abbrechen? §8§o/support cancel");
                } else {
                    int supporter = 0;
                    for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                        if(all.hasPermission("professionalbans.supportchat") || all.hasPermission("professionalbans.*")){
                            supporter++;
                        }
                    }
                    if(!args[0].equalsIgnoreCase("cancel")){
                        String subject = "";
                        for(int i = 0; i < args.length; i++){
                            subject = subject + " " + args[i];
                        }
                        if(!openchats.containsKey(p)){
                            if(supporter > 0){
                                openchats.put(p, subject);
                                p.sendMessage(Main.Prefix+"Du hast eine Anfrage mit dem Betreff §e§l"+subject+" §7gestartet");
                                for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                                    if(all.hasPermission("professionalbans.supportchat") || all.hasPermission("professionalbans.*")){
                                        all.sendMessage(Main.Prefix+"§e§l"+p.getName()+" §7benötigt Support §8(§e§o"+subject+"§8)");
                                        TextComponent tc = new TextComponent();
                                        tc.setText("§aSupport Chat starten");
                                        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/support "+p.getName()));
                                        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Klicken um den Chat mit §e§l"+p.getName()+" §7zu starten").create()));
                                        all.sendMessage(tc);
                                    }
                                }
                            } else {
                                p.sendMessage(Main.Prefix+"§cDerzeit ist kein Supporter online");
                            }
                        } else {
                            p.sendMessage(Main.Prefix+"Du hast bereits eine §e§lSupport Chat §7Anfrage gestellt");
                            p.sendMessage(Main.Prefix+"Möchtest du diese Anfrage §cabbrechen §7benutze §c§l/support cancel");
                        }
                    } else {
                        if(!openchats.containsKey(p)){
                            openchats.remove(p);
                            p.sendMessage(Main.Prefix+"Deine Anfrage wurde erfolgreich §cgelöscht");
                        } else {
                            p.sendMessage(Main.Prefix+"§cDu hast derzeit keine offene Anfrage");
                        }
                    }
                }
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.Prefix+"Der §e§lSupport Chat §7ist nur als Spieler verfügbar");
        }
    }
}
