package com.bot.nwdiscobot;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class NewWorldDiscordBotApplication {
	@Autowired
	private Environment env;

	public static void main(String[] args) {
		SpringApplication.run(NewWorldDiscordBotApplication.class, args);
	}

	@Bean
	@ConfigurationProperties(value = "discord-api")
	public DiscordApi discordApi() {
		String token = env.getProperty("TOKEN");
		DiscordApi api = new DiscordApiBuilder().setToken(token)
				.setAllNonPrivilegedIntents()
				.login()
				.join();

		// LISTENERS
		api.addMessageCreateListener(messageCreateEvent -> {
			if(messageCreateEvent.getMessageContent().equalsIgnoreCase("!ping")) {
				messageCreateEvent.getChannel().sendMessage("PONG");
			}
		});

		return api;
	}

}
