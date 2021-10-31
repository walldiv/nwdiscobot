package com.bot.nwdiscobot;

import com.bot.nwdiscobot.config.Constants;
import com.bot.nwdiscobot.config.ServerSetup;
import com.bot.nwdiscobot.listeners.JoinListener;
import com.bot.nwdiscobot.listeners.MessageListener;
import com.bot.nwdiscobot.listeners.PlayerAddRoleListener;
import com.bot.nwdiscobot.listeners.PlayerRemoveRoleListener;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class NewWorldDiscordBotApplication {
	@Autowired
	private Environment env;

	@Autowired
	private Constants constants;

	public static void main(String[] args) {
		SpringApplication.run(NewWorldDiscordBotApplication.class, args);
	}

	@Bean
	@ConfigurationProperties(value = "discord-api")
	public DiscordApi discordApi() {
		String token = env.getProperty("TOKEN");
		DiscordApi api = new DiscordApiBuilder().setToken(token)
				.setAllIntents()
				.login()
				.join();
		//SETUP CONSTANTS - WORKS WHEN REDEPLOYED & ALREADY JOINED
		new ServerSetup(api, constants);

		//SETUP DEFAULT CHANNEL FOR EVENTS MESSAGE
		api.addListener(new JoinListener(api, constants));

		api.addReconnectListener(event -> {
			log.info("RECONNECT HAPPEND - SERVER => ");
		});

		// LISTENERS
		api.addListener(new MessageListener(api, constants));
		api.addListener(new PlayerAddRoleListener(constants));
		api.addListener(new PlayerRemoveRoleListener(constants));

		api.addSlashCommandCreateListener(event -> {
			SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
			ServerChannel channel = slashCommandInteraction.getFirstOptionChannelValue().orElse(null);
			User user = slashCommandInteraction.getSecondOptionUserValue().orElse(null);
			Integer permissionNumber = slashCommandInteraction.getThirdOptionIntValue().orElse(null);

			//update channel permissions.....
			slashCommandInteraction.createImmediateResponder()
					.setContent("THE CHANNELS PERMISSIONS HAVE BEEN UPDATED")
					.respond();
		});

		return api;
	}

}
