package com.bot.nwdiscobot.listeners;

import com.bot.nwdiscobot.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class PlayerRemoveRoleListener implements ReactionRemoveListener {
    private Constants constants;
    String rolesMsg = "PLEASE SELECT FROM THE FOLLOWING CLASSES AS YOUR PLAYER TYPE";

    public PlayerRemoveRoleListener(Constants constants) {
        this.constants = constants;
    }


    @Override
    public void onReactionRemove(ReactionRemoveEvent reactionRemoveEvent) {
        AtomicReference<String> thisUserId = new AtomicReference<>("");
        AtomicBoolean bIsMatchingRolesMsg = new AtomicBoolean(false);
        AtomicReference<String> roleToAdd = new AtomicReference<>("");

        log.info(String.valueOf(reactionRemoveEvent.getChannel()));
        // GET EMOJI CLICKED FROM MESSAGE - MAP TO ROLE NAME
        log.info("REACTION EMOJI => {}", reactionRemoveEvent.getEmoji().asUnicodeEmoji().get());
        roleToAdd.set(constants.getRolesMap().get(reactionRemoveEvent.getEmoji().asUnicodeEmoji().get()));

        // GET CONTENT OF MESSAGE - MATCH TO ROLES MESSAGE
        reactionRemoveEvent.requestMessage()
                .thenAccept(msg -> {
                    if (msg.getContent().equalsIgnoreCase(this.rolesMsg)){
                        bIsMatchingRolesMsg.set(true);
                        log.info("MESSAGES MATCH FOR PLAYER ROLES MESSAGE!!!");
                        log.info("MESSAGE ID => {}", msg.getIdAsString());
                    }
                }).exceptionally(ExceptionLogger.get()).join();

        // GET THE USER THAT CLICKED ON REACTION
        reactionRemoveEvent.requestUser()
                .thenAccept(user -> {
                    thisUserId.set(user.getIdAsString());
                    log.info(user.getName());
                }).exceptionally(ExceptionLogger.get()).join();

        // IF WE MATCH THE RIGHT MESSAGE - PROCEED TO GRANT ROLE TO USER
        if (bIsMatchingRolesMsg.get()){
            DiscordApi api = this.constants.getServer().getApi();
            List<Role> roles = (List<Role>) api.getRolesByName(roleToAdd.get());
            if (roles.size() > 0 && roles.get(0).getName().equalsIgnoreCase(roleToAdd.get())){
                api.getUserById(thisUserId.get()).thenAccept(user -> {
                    roles.get(0).removeUser(user).thenAccept(success -> {
//                        log.info("REMOVE USER RETURNED RESULT => {}", success.toString());
                        log.info("REMOVED ROLE- {}    TO USER => {}", roles.get(0).getName(), user.getName());
                    }).exceptionally(ExceptionLogger.get());
                });
            }
        }

    }
}
