package com.bot.nwdiscobot.listeners;

import com.bot.nwdiscobot.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class PlayerAddRoleListener implements ReactionAddListener {
    private Constants constants;
    String rolesMsg = "PLEASE SELECT FROM THE FOLLOWING CLASSES AS YOUR PLAYER TYPE";

    public PlayerAddRoleListener(Constants constants) {
        this.constants = constants;
    }

    @Override
    public void onReactionAdd(ReactionAddEvent reactionAddEvent) {
        AtomicReference<String> thisUserId = new AtomicReference<>("");
        AtomicBoolean bIsMatchingRolesMsg = new AtomicBoolean(false);
        AtomicReference<String> roleToAdd = new AtomicReference<>("");
        AtomicBoolean bIsBot = new AtomicBoolean(false);

        log.info(String.valueOf(reactionAddEvent.getChannel()));
        // GET EMOJI CLICKED FROM MESSAGE - MAP TO ROLE NAME
        log.info("REACTION EMOJI => {}", reactionAddEvent.getEmoji().asUnicodeEmoji().get());
        roleToAdd.set(constants.getRolesMap().get(reactionAddEvent.getEmoji().asUnicodeEmoji().get()));

        // GET CONTENT OF MESSAGE - MATCH TO ROLES MESSAGE
        reactionAddEvent.requestMessage()
            .thenAccept(msg -> {
                if (msg.getContent().equalsIgnoreCase(this.rolesMsg)){
                    bIsMatchingRolesMsg.set(true);
                    log.info("MESSAGES MATCH FOR PLAYER ROLES MESSAGE!!!");
                    log.info("MESSAGE ID => {}", msg.getIdAsString());
                }
            }).exceptionally(ExceptionLogger.get()).join();

        // GET THE USER THAT CLICKED ON REACTION
        reactionAddEvent.requestUser()
            .thenAccept(user -> {
                if(!user.isBot()) {
                    thisUserId.set(user.getIdAsString());
                    log.info(user.getName());
                }
                else bIsBot.set(true);
            }).exceptionally(ExceptionLogger.get()).join();

        // IF WE MATCH THE RIGHT MESSAGE - PROCEED TO GRANT ROLE TO USER
        if (bIsMatchingRolesMsg.get() && !bIsBot.get()){
            DiscordApi api = this.constants.getServer().getApi();
            List<Role> roles = (List<Role>) api.getRolesByName(roleToAdd.get());
            if (roles.size() > 0 && roles.get(0).getName().equalsIgnoreCase(roleToAdd.get())){
                api.getUserById(thisUserId.get()).thenAccept(user -> {
                    roles.get(0).addUser(user).thenAccept(success -> {
                        log.info("ADDED ROLE- {}    TO USER => {}", roles.get(0).getName(), user.getName());
                    });
                });
            }
        }
    }
}
