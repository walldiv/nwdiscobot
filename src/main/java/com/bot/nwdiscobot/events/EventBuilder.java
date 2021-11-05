package com.bot.nwdiscobot.events;

import com.bot.nwdiscobot.config.Constants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Data
@Slf4j
public class EventBuilder {
    private User userAuthor;
    private Constants constants;
    private String title = "blah";
    private String description = "blah description";
    private LocalDateTime scheduledTime = LocalDateTime.now();
    ListenerManager<MessageCreateListener> listenMgr = null;

    /***  TIMER FOR REPLIES ***/
    private Timer timer;

    /*** QUESTION STAGE ***/
    private int stage = 0;


    public EventBuilder(User userAuthor, Constants constants) {
        this.userAuthor = userAuthor;
        this.constants = constants;
        listenMgr = this.userAuthor.addMessageCreateListener(messageCreateEvent -> {
            replyFromBot(messageCreateEvent.getMessageContent());
        });
        this.sendBotMsg("PLEASE ENTER A TITLE FOR EVENT", "Enter text for title \n To cancel - type 'cancel'").thenAccept(msg -> this.stage = 1);
    }

    private CompletableFuture<Message> sendBotMsg(String message, String descript){
        startWaitTimer();
        return new MessageBuilder()
            .setEmbed(new EmbedBuilder()
                    .setTitle(message)
                    .setDescription(descript))
            .send(userAuthor).exceptionally(ExceptionLogger.get());
    }

    private void replyFromBot(String message){
        if (message.equalsIgnoreCase("cancel")){
            this.destroy(true);
            return;
        }
        switch (this.stage) {
            case 1:{
                log.info("ENTERING STAGE 1");
                this.title = message;
                this.sendBotMsg("PLEASE ENTER A DESCRIPTION FOR EVENT", "Enter text for description - can be blank. \n To cancel - type 'cancel'").thenAccept(msg -> this.stage = 2);
                break;
            }
            case 2:{
                log.info("ENTERING STAGE 2");
                this.description = message;
                this.sendBotMsg("PLEASE ENTER A DATE & TIME FOR EVENT", "FORMAT - 01-02-2021 09:30 AM \n To cancel - type 'cancel'").thenAccept(msg -> this.stage = 3);
                break;
            }
            case 3: {
                log.info("ENTERING STAGE 3");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-d-yyyy hh:mm a");
                try{
                    this.scheduledTime = LocalDateTime.parse(message, formatter);
                } catch (DateTimeParseException e) {
                    this.sendBotMsg("INVALID ENTRY - PLEASE FOLLOW FORMAT:", "FORMAT - 01-02-2021 09:30 AM. \n To cancel - type 'cancel'").thenAccept(msg -> this.stage = 3);
                    break;
                }
                this.create();
                break;
            }
        }
    }

    private void startWaitTimer(){
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                log.info("CANCELLING TIMER");
                cancelTimer();
            }
        };
        log.info("STARTING TIMER");
        this.timer = new Timer("replyTimer");
        timer.schedule(task, 15000l);
    }

    private void cancelTimer(){
        log.info("TIMER HAS BEEN CANCELLED");
        this.timer.cancel();
    }

    public void create(){
        ServerTextChannel channel = constants.getServer().getTextChannelsByName("scheduled-events").get(0);
        log.info("CHANNEL FOUND => {}", channel.getName());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE MMM dd uuuu hh:mm a");
        LocalDateTime calendarTime = this.scheduledTime.minusHours(20).plusDays(1);        //WTF IS THIS GARBAGE MATH?!
        String calendarUrl = this.title.replace(" ", "+") + "&details=&location=&dates=" + calendarTime.toString().replace("-", "").replace(":", "") + "00Z/" + calendarTime.plusHours(1).toString().replace("-", "").replace(":", "") + "00Z";
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(this.title)
                .setDescription(this.description)
                .setAuthor(this.userAuthor)
                .addField("TIME:", this.scheduledTime.format(formatter))
                .setColor(Color.GREEN)
                .addField("LINKS:", "[Add to Google calendar](http://www.google.com/calendar/event?action=TEMPLATE&text="+ calendarUrl + ")")
                .addInlineField("✅ ACCEPTED", "-")
                .addInlineField("❔ TENTATIVE", "-")
                .setFooter("Created by - " + this.userAuthor.getDisplayName(this.constants.getServer()), "https://cdn.discordapp.com/embed/avatars/1.png");

        try {
            String msgId = new MessageBuilder()
                    .setEmbed(embed)
                    .addComponents(
                            ActionRow.of(Button.success("JOIN", "JOIN EVENT"),
                                    Button.primary("TENTATIVE", "TENTATIVE")))
                    .send(channel).exceptionally(ExceptionLogger.get()).get().getIdAsString();
            EmbedBuilder success = new EmbedBuilder()
                    .setTitle("EVENT HAS BEEN CREATED!")
                    .setColor(Color.GREEN)
                    .addField("LINKS:", "[Link to your event](http://discordapp.com/channels/" + this.constants.getServer().getIdAsString() + "/" + channel.getIdAsString() + "/" + msgId + ")");
            this.userAuthor.sendMessage(success);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.destroy(false);
    }

    private void destroy(boolean bCancelled){
        if(bCancelled)
            this.userAuthor.sendMessage("Welp - guess we'll make an event some other time!  Good day mate :)");
        listenMgr.remove();
        this.cancelTimer();
        this.userAuthor = null;
        this.constants = null;
        this.title = null;
        this.description = null;
        this.scheduledTime = null;
    }
}
