package com.twohigh.testbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public final class TestBotMain {

    public static void main(String[] args) throws Exception {
        BotConfig config = BotConfig.load();
        TestStore store = new TestStore(config.dataDir());
        Map<String, List<TestDefinition>> sections = TestLoader.load();

        int total = sections.values().stream().mapToInt(List::size).sum();
        System.out.println("[testbot] Loaded " + total + " tests in " + sections.size() + " sections.");

        TestBotListener listener = new TestBotListener(config, store, sections);

        JDA jda = JDABuilder.createLight(config.token(), EnumSet.of(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.MESSAGE_CONTENT))
                .addEventListeners(listener)
                .build()
                .awaitReady();

        Guild guild = jda.getGuildById(config.guildId());
        if (guild == null) {
            System.err.println("[testbot] Bot is not in the configured guild (guild_id="
                    + config.guildId() + "). Invite it first, then restart.");
            jda.shutdown();
            return;
        }

        guild.updateCommands().addCommands(
                Commands.slash("test", "2high2try test campaign")
                        .addSubcommands(
                                new SubcommandData("start", "Post the full checklist into the testing channel")
                                        .addOption(OptionType.BOOLEAN, "fresh",
                                                "Wipe saved votes/notes and post a fresh campaign", false),
                                new SubcommandData("status", "Show campaign progress by section"),
                                new SubcommandData("report", "Export the full campaign results as a text file"))
        ).queue(ok -> System.out.println("[testbot] Slash commands registered."));

        System.out.println("[testbot] Ready as " + jda.getSelfUser().getName()
                + " — run /test start in your server.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { store.close(); } catch (Exception ignored) {}
            jda.shutdown();
        }));
    }
}
