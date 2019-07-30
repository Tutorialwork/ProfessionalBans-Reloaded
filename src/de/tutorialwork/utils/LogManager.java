package de.tutorialwork.utils;

import de.tutorialwork.main.Main;

public class LogManager {

    //DATABASE STRUCTURE
    //ID int(11) AUTO_INCREMENT UNIQUE, UUID varchar(255), BYUUID varchar(255), ACTION varchar(255), NOTE varchar(255), DATE varchar(255)

    //ACTION Codes
    //BAN, MUTE, ADD_WORD_BLACKLIST, DEL_WORD_BLACKLIST, CREATE_CHATLOG, IPBAN_IP, IPBAN_PLAYER, KICK, REPORT, REPORT_OFFLINE, REPORT_ACCEPT, UNBAN_IP, UNBAN_BAN, UNBAN_MUTE,
    // ADD_WEBACCOUNT, DEL_WEBACCOUNT, AUTOMUTE_ADBLACKLIST, AUTOMUTE_BLACKLIST
    //
    //UUID/BY_UUID = UUID des Spielers, null = keine Spieler verfügbar, "KONSOLE" = Befehl über Konsole ausgeführt

    public static void createEntry(String UUID, String ByUUID, String Action, String Note){
        Main.mysql.update("INSERT INTO log(UUID, BYUUID, ACTION, NOTE, DATE) " +
                "VALUES ('" + UUID + "', '" + ByUUID + "', '" + Action + "', '" + Note + "', '" + System.currentTimeMillis() + "')");
    }

}
