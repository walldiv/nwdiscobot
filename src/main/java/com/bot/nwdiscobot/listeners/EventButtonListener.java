package com.bot.nwdiscobot.listeners;

import com.bot.nwdiscobot.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.MessageComponentCreateEvent;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;
import org.javacord.api.util.logging.ExceptionLogger;

@Slf4j
public class EventButtonListener implements MessageComponentCreateListener {
    Constants constants;
    DiscordApi api;
    Server server;

    public EventButtonListener(Constants constants) {
        this.constants = constants;
        this.api = constants.getApi();
        this.server = constants.getServer();
    }

    @Override
    public void onComponentCreate(MessageComponentCreateEvent messageComponentCreateEvent) {
        MessageComponentInteraction interaction = messageComponentCreateEvent.getMessageComponentInteraction();
        String status = interaction.getCustomId();
        String msgId = String.valueOf(interaction.getMessageId());
        String userName = interaction.getUser().getDisplayName(server);
//        interaction.createImmediateResponder()
//                .setContent("YOU SELECTED A BUTTON => " + customId);
        log.info("{} SELECTED A BUTTON => {}     FROM MESSAGE => {}", userName, status, msgId);
        this.addToEventList(userName, status, msgId);
    }

    private void addToEventList(String userName, String status, String msgId){
        api.getMessageById(msgId, server.getTextChannelsByName("scheduled-events").get(0)).thenAccept(msg -> {
            Embed msgEmbed = msg.getEmbeds().get(0);
            String joinList = msgEmbed.getFields().get(2).getValue();
            String tentList = msgEmbed.getFields().get(3).getValue();
            this.removeFromEventList(userName, status, msg);
            switch(status){
                case "JOIN":{
                    if(!joinList.contains(userName)) {
                        log.info("JOIN LIST => {}", joinList);
                        joinList = joinList.concat(userName + "\n");
                        log.info("JOIN LIST AFTER JOIN => {}", joinList);
                        String finalJoinList = joinList;
                        msg.edit(
                                msg.getEmbeds().get(0).toBuilder().updateAllFields(embedField -> {
                                    if (embedField.getName().equalsIgnoreCase("✅ ACCEPTED")) {
                                        embedField.setValue(finalJoinList);
                                    }
                                })
                        ).exceptionally(ExceptionLogger.get());
                    }
                    break;
                }
                case "TENTATIVE":{
                    if(!tentList.contains(userName)) {
                        log.info("TENTATIVE LIST => {}", tentList);
                        tentList = tentList.concat(userName + "\n");
                        log.info("TENTATIVE LIST AFTER JOIN => {}", tentList);
                        String finalTentList = tentList;
                        msg.edit(
                                msg.getEmbeds().get(0).toBuilder().updateAllFields(embedField -> {
                                    if (embedField.getName().equalsIgnoreCase("❔ TENTATIVE")) {
                                        embedField.setValue(finalTentList);
                                    }
                                })
                        ).exceptionally(ExceptionLogger.get());
                    }
                    break;
                }
            }
        });
    }

    private void removeFromEventList(String userName, String status, Message msg){
        Embed msgEmbed = msg.getEmbeds().get(0);
        String joinList = msgEmbed.getFields().get(2).getValue();
        String tentList = msgEmbed.getFields().get(3).getValue();
        switch(status){
            case "JOIN":{
                if(tentList.contains(userName)) {
                    log.info("TENTATIVE LIST => {}", tentList);
                    tentList = tentList.replace(userName, "");
                    log.info("TENTATIVE LIST AFTER REMOVE => {}", tentList);
                    String finalTentList = tentList;
                    msg.edit(
                            msg.getEmbeds().get(0).toBuilder().updateAllFields(embedField -> {
                                if (embedField.getName().equalsIgnoreCase("❔ TENTATIVE")) {
                                    embedField.setValue(finalTentList);
                                }
                            })
                    ).exceptionally(ExceptionLogger.get()).join();
                    break;
                }
            }
            case "TENTATIVE":{
                if(joinList.contains(userName)) {
                    log.info("JOIN LIST => {}", joinList);
                    joinList = joinList.replace(userName, "");
                    log.info("JOIN LIST AFTER REMOVE => {}", tentList);
                    String finalJoinList = joinList;
                    msg.edit(
                            msg.getEmbeds().get(0).toBuilder().updateAllFields(embedField -> {
                                if (embedField.getName().equalsIgnoreCase("✅ ACCEPTED")) {
                                    embedField.setValue(finalJoinList);
                                }
                            })
                    ).exceptionally(ExceptionLogger.get()).join();
                }
                break;
            }
        }
    }
}
