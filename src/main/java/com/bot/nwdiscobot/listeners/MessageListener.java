package com.bot.nwdiscobot.listeners;

import com.bot.nwdiscobot.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MessageListener implements MessageCreateListener {
    private DiscordApi api;
    private Constants constants;

    @Autowired
    public MessageListener(DiscordApi api, Constants constants) {
        this.api = api;
        this.constants = constants;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        Pattern pattern = Pattern.compile("![online]?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(messageCreateEvent.getMessageContent());

        if (messageCreateEvent.getMessageContent().equalsIgnoreCase("!ping")) {
            messageCreateEvent.getChannel().sendMessage("PONG");
        }
        if (messageCreateEvent.getMessageContent().equalsIgnoreCase("!?")) {
            List<SlashCommand> commands = api.getGlobalSlashCommands().join();
            messageCreateEvent.getChannel().sendMessage("AVAILABLE COMMANDS ARE: \n" + commands.toString());
        }
        if (messageCreateEvent.getMessageContent().equalsIgnoreCase("!channels")) {
            Collection<ServerTextChannel> channels = api.getServerTextChannels();
            String chanString = "";
            for (ServerTextChannel channel : channels) {
                chanString += " " + channel.getName();
            }
            messageCreateEvent.getChannel().sendMessage("CHANNELS IN SERVER => \n" + chanString);
        }
        if (messageCreateEvent.getMessageContent().equalsIgnoreCase("!whosonline")) {
//            log.info("CACHED USERS LENGTH => {}", api.getCachedUsers().size());
//            api.getCachedUsers().forEach(t-> log.info(t.getName()));
            String usersString = this.getAllOnlineUsers();
            messageCreateEvent.getChannel().sendMessage("PLAYERS ONLINE ARE: \n" + usersString);
        }
        else if (matcher.find()){
            String[] splitStr = messageCreateEvent.getMessageContent().split("online", 2);
            log.info("REGEX MATCH FOUND => {}", messageCreateEvent.getMessageContent());
            for(String s : splitStr){
                log.info("SPLIT STRING => {}", s);
            }
            if (splitStr[0].toLowerCase(Locale.ROOT).contains("tank")) {
                String playersStr = getOnlineUsersByRole("nwbot_Tank");
                messageCreateEvent.getChannel().sendMessage("TANKS ONLINE ARE: \n" + playersStr);
            }
        }
    }

    private String getAllOnlineUsers(){
        String usersOnline = "";
        for(User user : api.getCachedUsers()){
            if(!user.isBot()) {
                usersOnline = usersOnline.concat(user.getName() + "- ");
                for (Role role : user.getRoles(constants.getServer())) {
                    if (role.getName().contains("nwbot_")) {
                        log.info("USER ROLE => {}", role.getName());
                        usersOnline = usersOnline.concat(role.getName().replace("nwbot_", "") + ", ");
                    }
                };
                usersOnline = usersOnline.concat("\n");
                log.info("usersOnline STR => {}", usersOnline);
            }
        };
        return usersOnline;
    }

    private String getOnlineUsersByRole(String role){
        List<String> usersOnline = new ArrayList<>();
        log.info("CHECKING ROLE => {}", role);
        Collection<User> usersList = constants.getServer().getRolesByName(role).get(0).getUsers();
        log.info("ROLES FROM SERVER => {}", constants.getServer().getRolesByName(role).get(0).getName());
        for (User user : usersList){
            log.info("USER IN LIST => {}", user.getName());
            if (user.getStatus().getStatusString().equalsIgnoreCase("ONLINE")){
                usersOnline.add(user.getName() + " - " + role);
            }
        }
        AtomicReference<String> usersString = new AtomicReference<>("");
        usersOnline.forEach( t -> usersString.set( usersString.get().concat(t + "\n")));
        return usersString.get();
    }

}
