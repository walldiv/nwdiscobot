package com.bot.nwdiscobot.config;

import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelCategoryBuilder;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.util.logging.ExceptionLogger;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ServerSetup {
    private List<String> rolesToSetup = new ArrayList<>(Arrays.asList("nwbot_Tank", "nwbot_Melee", "nwbot_Ranged", "nwbot_Heals", "nwbot_Mage"));

    private DiscordApi api;
    private Constants constants;

    @Autowired
    public ServerSetup(DiscordApi api, Constants constants) {
        this.api = api;
        this.constants = constants;

        constants.setApi(api);
        List<Server> servers = (List<Server>) api.getServers();
        if (servers.size() > 0) {
            constants.setServer(servers.get(0));
            log.info("SET SERVER PROPERLY");

            //SETUP CATEGORY, PLAYER ROLES & EVENTS CHANNEL
            PermissionsBuilder permBuilder = new PermissionsBuilder()
                    .setAllDenied()
                    .setAllowed(PermissionType.ADD_REACTIONS, PermissionType.READ_MESSAGE_HISTORY, PermissionType.READ_MESSAGES);

            List<ServerTextChannel> channels = constants.getServer().getTextChannels();
            List<ChannelCategory> categories = new ArrayList(api.getChannelCategoriesByNameIgnoreCase("nw bot"));
            ChannelCategory cat;
            for (ServerTextChannel channel : channels) {
                log.info("SERVER TEXT CHANNEL => {}", channel.getName());
                if (channel.getName().equalsIgnoreCase("PLAYER-ROLES")) {
                    constants.setBHasPlayerRolesChannel(true);
                }
                if(channel.getName().equalsIgnoreCase("SCHEDULED-EVENTS")){
                    constants.setBHasEventsChannel(true);
                }
            }
            if(categories.isEmpty()){
                cat = new ChannelCategoryBuilder(constants.getServer())
                        .setName("NW BOT").create().join();
//                 categories.add(new ChannelCategoryBuilder(constants.getServer())
//                        .setName("NW BOT").create().join());
                log.info("SERVER CREATED CATEGORY => {}", cat);
            }
            else cat = categories.get(0);
            if (!constants.isBHasPlayerRolesChannel()) {
                ServerTextChannel rolesChannel = new ServerTextChannelBuilder(constants.getServer())
                        .setName("PLAYER ROLES")
                        .setCategory(cat)
                        .setTopic("PLAYER ROLE USED FOR WARS/PVP")
                        .addPermissionOverwrite(constants.getServer().getEveryoneRole(), permBuilder.build())
                        .create().join();
                log.info("CREATING PLAYER-ROLES CHANNEL");
                rolesChannel.sendMessage("PLEASE SELECT FROM THE FOLLOWING CLASSES AS YOUR PLAYER TYPE")
                        .thenAccept(msg -> {
                            msg.addReactions("⚔️", "\uD83D\uDEE1️", "\uD83C\uDFF9", "\uD83D\uDC96", "\uD83D\uDCA5\t");
                            constants.setPRolesMsgId(msg.getIdAsString());
                        }).exceptionally(ExceptionLogger.get());
            }

            if (!constants.isBHasEventsChannel()) {
                ServerTextChannel eventsChannel = new ServerTextChannelBuilder(constants.getServer())
                        .setName("SCHEDULED EVENTS")
                        .setCategory(cat)
                        .setTopic("SCHEDULED EVENTS CALENDAR")
                        .addPermissionOverwrite(constants.getServer().getEveryoneRole(), permBuilder.build())
                        .create().join();
                log.info("CREATING EVENTS CHANNEL");
            }

            // SETUP ROLES
            List<Role> roles = constants.getServer().getRoles();
            List<String> rolesByName = new ArrayList<>();
            for (Role role : roles) {
                log.info("ROLE FOUND => {}", role.getName());
                rolesByName.add(role.getName());
            }
            if (!rolesByName.contains("nwbot_Tank")) {
                constants.getServer().createRoleBuilder().setName("nwbot_Tank").setColor(new Color(39, 77, 126)).create().exceptionally(ExceptionLogger.get()).join();
                log.info("ROLE CREATED - nwbot_Tank");
            }
            if (!rolesByName.contains("nwbot_Melee")) {
                constants.getServer().createRoleBuilder().setName("nwbot_Melee").setColor(new Color(58, 152, 163)).create().exceptionally(ExceptionLogger.get()).join();
                log.info("ROLE CREATED - nwbot_Melee");
            }
            if (!rolesByName.contains("nwbot_Ranged")) {
                constants.getServer().createRoleBuilder().setName("nwbot_Ranged").setColor(new Color(224, 85, 127)).create().exceptionally(ExceptionLogger.get()).join();
                log.info("ROLE CREATED - nwbot_Ranged");
            }
            if (!rolesByName.contains("nwbot_Heals")) {
                constants.getServer().createRoleBuilder().setName("nwbot_Heals").setColor(new Color(243, 103, 51)).create().exceptionally(ExceptionLogger.get()).join();
                log.info("ROLE CREATED - nwbot_Heals");
            }
            if (!rolesByName.contains("nwbot_Mage")) {
                constants.getServer().createRoleBuilder().setName("nwbot_Mage").setColor(new Color(220, 220, 52)).create().exceptionally(ExceptionLogger.get()).join();
                log.info("ROLE CREATED - nwbot_Mage");
            }
        }
    }
}
