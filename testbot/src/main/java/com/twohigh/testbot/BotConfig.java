package com.twohigh.testbot;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public record BotConfig(String token, long guildId, long channelId, File dataDir) {

    public static BotConfig load() throws Exception {
        File file = new File("bot-config.yml");
        if (!file.exists()) file = new File("testbot/bot-config.yml");
        if (!file.exists()) {
            throw new IllegalStateException(
                    "bot-config.yml not found. Copy testbot/bot-config.example.yml to "
                            + "testbot/bot-config.yml and fill in token, guild_id, channel_id.");
        }

        try (InputStream in = new FileInputStream(file)) {
            Map<String, Object> map = new Yaml().load(in);
            String token = String.valueOf(map.get("token"));
            long guildId = ((Number) map.get("guild_id")).longValue();
            long channelId = ((Number) map.get("channel_id")).longValue();
            if (token == null || token.isBlank() || token.contains("PASTE_TOKEN")) {
                throw new IllegalStateException("bot-config.yml: token is not set.");
            }
            if (guildId == 0 || channelId == 0) {
                throw new IllegalStateException("bot-config.yml: guild_id and channel_id must be set.");
            }
            return new BotConfig(token, guildId, channelId, file.getAbsoluteFile().getParentFile());
        }
    }
}
