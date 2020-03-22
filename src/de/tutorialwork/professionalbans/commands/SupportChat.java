package de.tutorialwork.professionalbans.commands;

import de.tutorialwork.professionalbans.main.Main;
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
                                SupportChat.activechats.get(p).sendMessage(Main.data.Prefix+"§e§l"+p.getName()+" "+Main.messages.getString("supportchat_end"));
                                activechats.remove(key);
                            } else {
                                key.sendMessage(Main.data.Prefix+"§e§l"+p.getName()+" "+Main.messages.getString("supportchat_end"));
                                activechats.remove(key);
                            }
                        }
                        p.sendMessage(Main.data.Prefix+"§e§l"+Main.messages.getString("you")+" "+Main.messages.getString("supportchat_end"));
                        return;
                    } else {
                        p.sendMessage(Main.data.Prefix+Main.messages.getString("no_support_chat"));
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
                                all.sendMessage(Main.data.Prefix+"§e§l"+p.getName()+" "+Main.messages.getString("start_supportchat"));
                                all.sendMessage(Main.data.Prefix+Main.messages.getString("stop_supportchat_notify"));
                                p.sendMessage(Main.data.Prefix+"§e§l"+all.getName()+" "+Main.messages.getString("start_supportchat"));
                                p.sendMessage(Main.data.Prefix+Main.messages.getString("stop_supportchat_notify"));
                            } else {
                                p.sendMessage(Main.data.Prefix+Main.messages.getString("expire_supportchat"));
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
                            tc.setText(Main.messages.getString("start_supportchat_hover"));
                            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/support "+key));
                            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Main.messages.getString("start_supportchat_hover_text").replace("%player%", key.getName())).create()));
                            p.sendMessage(tc);
                            i++;
                        }
                        p.sendMessage("§8[]===================================[]");
                        p.sendMessage(Main.data.Prefix+Main.messages.getString("open_supportchats").replace("%count%", i+""));
                    } else {
                        p.sendMessage(Main.data.Prefix+Main.messages.getString("no_support_chat"));
                    }
                }
            } else {
                //Normal Member
                if(args.length == 0){
                    p.sendMessage(Main.data.Prefix+Main.messages.getString("supportchat_syntax"));
                    p.sendMessage(Main.data.Prefix+Main.messages.getString("supportchat_syntax_cancel"));
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
                                p.sendMessage(Main.data.Prefix+Main.messages.getString("supportchat_started").replace("%subject%", subject));
                                for(ProxiedPlayer all : BungeeCord.getInstance().getPlayers()){
                                    if(all.hasPermission("professionalbans.supportchat") || all.hasPermission("professionalbans.*")){
                                        all.sendMessage(Main.data.Prefix+"§e§l"+p.getName()+" "+Main.messages.getString("supportchat_notify")+" §8(§e§o"+subject+"§8)");
                                        TextComponent tc = new TextComponent();
                                        tc.setText(Main.messages.getString("supportchat_start"));
                                        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/support "+p.getName()));
                                        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Main.messages.getString("start_supportchat_hover_text").replace("%player%", p.getName())).create()));
                                        all.sendMessage(tc);
                                    }
                                }
                            } else {
                                p.sendMessage(Main.data.Prefix+Main.messages.getString("supportchat_no_online"));
                            }
                        } else {
                            p.sendMessage(Main.data.Prefix+Main.messages.getString("supportchat_already"));
                            p.sendMessage(Main.data.Prefix+Main.messages.getString("supportchat_syntax_cancel"));
                        }
                    } else {
                        if(!openchats.containsKey(p)){
                            openchats.remove(p);
                            p.sendMessage(Main.data.Prefix+Main.messages.getString("supportchat_deleted"));
                        } else {
                            p.sendMessage(Main.data.Prefix+Main.messages.getString("no_support_chat"));
                        }
                    }
                }
            }
        } else {
            BungeeCord.getInstance().getConsole().sendMessage(Main.data.Prefix+Main.messages.getString("only_player_cmd"));
        }
    }
}
