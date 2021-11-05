package com.bot.nwdiscobot.config;

import lombok.Data;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
public class Constants {
    /***********    SERVER INFO    **********/
    private DiscordApi api;
    private Server server;


    /***********    CHANNELS INFO    **********/
    private List<String> textChannels;
    private boolean bHasPlayerRolesChannel = false;
    private boolean bHasEventsChannel = false;


    /***********    ROLES INFO    **********/
    private String pRolesMsgId;
    Map<String, String> rolesMap = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put("⚔️", "nwbot_Melee");
            put("🛡️", "nwbot_Tank");
            put("🏹", "nwbot_Ranged");
            put("💖", "nwbot_Heals");
            put("💥", "nwbot_Mage");
        }
    };

}
