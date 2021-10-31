package com.bot.nwdiscobot.listeners;

import com.bot.nwdiscobot.config.Constants;
import com.bot.nwdiscobot.config.ServerSetup;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelCategoryBuilder;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.listener.server.ServerJoinListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class JoinListener implements ServerJoinListener {
    private Constants constants;
    private DiscordApi api;

    @Autowired
    public JoinListener(DiscordApi api, Constants constants) {
        this.api = api;
        this.constants = constants;
    }

    @Override
    public void onServerJoin(ServerJoinEvent serverJoinEvent) {
        new ServerSetup(api, constants);
    }
}
