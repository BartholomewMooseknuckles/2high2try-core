package com.twohigh.testbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TestBotListener extends ListenerAdapter {

    private static final Color GREY = new Color(110, 120, 110);
    private static final Color GREEN = new Color(46, 133, 64);
    private static final Color RED = new Color(194, 65, 56);
    private static final Color AMBER = new Color(200, 138, 45);

    private static final String EMOJI_PASS = "✅"; // ✅
    private static final String EMOJI_FAIL = "❌"; // ❌
    private static final String EMOJI_NOTE = "📝"; // 📝

    private final BotConfig config;
    private final TestStore store;
    private final Map<String, List<TestDefinition>> sections;
    private final Map<Integer, TestDefinition> byNum = new HashMap<>();

    // messageId -> test, testNum -> threadId; rebuilt from store on boot
    private final Map<Long, TestDefinition> byMessageId = new HashMap<>();
    private final Map<Integer, Long> threadByTest = new HashMap<>();

    private volatile boolean posting = false;

    public TestBotListener(BotConfig config, TestStore store,
                           Map<String, List<TestDefinition>> sections) throws Exception {
        this.config = config;
        this.store = store;
        this.sections = sections;
        sections.values().forEach(list -> list.forEach(t -> byNum.put(t.num(), t)));
        for (Map.Entry<Integer, TestStore.MessageRef> e : store.loadMessages().entrySet()) {
            TestDefinition def = byNum.get(e.getKey());
            if (def != null) {
                byMessageId.put(e.getValue().messageId(), def);
                threadByTest.put(e.getKey(), e.getValue().threadId());
            }
        }
    }

    // --- Slash commands ---

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("test")) return;
        String sub = event.getSubcommandName();
        if (sub == null) return;

        switch (sub) {
            case "start" -> handleStart(event);
            case "status" -> handleStatus(event);
            case "report" -> handleReport(event);
        }
    }

    private void handleStart(SlashCommandInteractionEvent event) {
        if (event.getMember() == null
                || !event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You need the **Manage Server** permission to start a test campaign.")
                    .setEphemeral(true).queue();
            return;
        }
        if (posting) {
            event.reply("Already posting a campaign — give it a minute.").setEphemeral(true).queue();
            return;
        }
        boolean fresh = event.getOption("fresh") != null && event.getOption("fresh").getAsBoolean();
        if (!byMessageId.isEmpty() && !fresh) {
            event.reply("A campaign is already posted. Use `/test start fresh:True` to wipe the "
                            + "saved state and post a new one (old messages stay — archive or delete "
                            + "the old threads yourself).")
                    .setEphemeral(true).queue();
            return;
        }

        TextChannel channel = event.getJDA().getTextChannelById(config.channelId());
        if (channel == null) {
            event.reply("Configured channel_id doesn't resolve to a text channel I can see.")
                    .setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();
        posting = true;
        new Thread(() -> {
            try {
                if (fresh) {
                    store.clearAll();
                    byMessageId.clear();
                    threadByTest.clear();
                }
                int posted = 0;
                for (Map.Entry<String, List<TestDefinition>> sec : sections.entrySet()) {
                    String badge = sec.getValue().get(0).badge();
                    Message header = channel
                            .sendMessage("**" + sec.getKey() + "**  ·  " + badge)
                            .complete();
                    ThreadChannel thread = channel
                            .createThreadChannel(trim(sec.getKey(), 100), header.getIdLong())
                            .complete();
                    for (TestDefinition t : sec.getValue()) {
                        MessageCreateAction action = thread.sendMessageEmbeds(buildEmbed(t));
                        if (!t.isKnown()) {
                            action = action.setComponents(ActionRow.of(
                                    Button.secondary("retest:" + t.num(),
                                            "🔁 Needs more testing")));
                        }
                        Message msg = action.complete();
                        if (!t.isKnown()) {
                            msg.addReaction(Emoji.fromUnicode(EMOJI_PASS)).complete();
                            msg.addReaction(Emoji.fromUnicode(EMOJI_FAIL)).complete();
                        }
                        store.saveMessage(t.num(), msg.getIdLong(), thread.getIdLong());
                        synchronized (this) {
                            byMessageId.put(msg.getIdLong(), t);
                            threadByTest.put(t.num(), thread.getIdLong());
                        }
                        posted++;
                    }
                }
                event.getHook().editOriginal("Posted **" + posted + " tests** across **"
                        + sections.size() + " sections** in " + channel.getAsMention()
                        + ". React ✅ or ❌ on each test, reply to a test to leave a note, "
                        + "hit the 🔁 button if it needs another look.").queue();
            } catch (Exception e) {
                event.getHook().editOriginal("Posting failed: " + e.getMessage()).queue();
                e.printStackTrace();
            } finally {
                posting = false;
            }
        }, "test-poster").start();
    }

    private void handleStatus(SlashCommandInteractionEvent event) {
        try {
            EmbedBuilder eb = new EmbedBuilder().setTitle("Test campaign status");
            int pass = 0, fail = 0, retest = 0, untested = 0, known = 0;
            StringBuilder lines = new StringBuilder();
            for (Map.Entry<String, List<TestDefinition>> sec : sections.entrySet()) {
                int sDone = 0, sTotal = 0;
                for (TestDefinition t : sec.getValue()) {
                    if (t.isKnown()) { known++; continue; }
                    sTotal++;
                    String st = stateOf(t);
                    switch (st) {
                        case "pass" -> pass++;
                        case "fail" -> fail++;
                        case "retest" -> retest++;
                        default -> untested++;
                    }
                    if (!st.equals("none")) sDone++;
                }
                if (sTotal > 0) {
                    lines.append("`").append(sDone).append("/").append(sTotal).append("` ")
                            .append(sec.getKey()).append("\n");
                }
            }
            eb.setDescription("✅ **" + pass + "** passed · ❌ **" + fail
                    + "** failed · 🔁 **" + retest + "** need retest · **"
                    + untested + "** untested · ⚠️ " + known + " known bugs\n\n" + lines);
            eb.setColor(fail > 0 ? RED : (untested > 0 ? GREY : GREEN));
            event.replyEmbeds(eb.build()).queue();
        } catch (Exception e) {
            event.reply("Status failed: " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    private void handleReport(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        try {
            String report = ReportBuilder.build(sections, store, this::stateOf);
            event.getHook().sendFiles(FileUpload.fromData(
                    report.getBytes(StandardCharsets.UTF_8),
                    "2high2try-test-report.txt")).queue();
        } catch (Exception e) {
            event.getHook().editOriginal("Report failed: " + e.getMessage()).queue();
        }
    }

    // --- Reactions ---

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUserIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        TestDefinition t = byMessageId.get(event.getMessageIdLong());
        if (t == null || t.isKnown()) return;

        String emoji = event.getEmoji().getName();
        try {
            if (EMOJI_PASS.equals(emoji)) {
                store.setVote(t.num(), event.getUserIdLong(), "pass");
                store.setRetest(t.num(), false);
            } else if (EMOJI_FAIL.equals(emoji)) {
                store.setVote(t.num(), event.getUserIdLong(), "fail");
                store.setRetest(t.num(), false);
            } else {
                return;
            }
            updateEmbed(event.getJDA(), t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (event.getUserIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        TestDefinition t = byMessageId.get(event.getMessageIdLong());
        if (t == null || t.isKnown()) return;

        String emoji = event.getEmoji().getName();
        if (!EMOJI_PASS.equals(emoji) && !EMOJI_FAIL.equals(emoji)) return;
        try {
            // Removing either reaction clears that user's vote entirely; if they still have
            // the other reaction up, the next add event already recorded it.
            store.setVote(t.num(), event.getUserIdLong(), null);
            updateEmbed(event.getJDA(), t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Retest button ---

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith("retest:")) return;
        int num = Integer.parseInt(id.substring("retest:".length()));
        TestDefinition t = byNum.get(num);
        if (t == null) return;
        try {
            store.setRetest(num, true);
            updateEmbed(event.getJDA(), t);
            event.reply("Flagged **#" + num + "** as needs-more-testing. Any new ✅/❌ "
                    + "reaction clears the flag.").setEphemeral(true).queue();
        } catch (Exception e) {
            event.reply("Failed: " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    // --- Replies become notes ---

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot()) return;
        if (event.getMessage().getMessageReference() == null) return;

        long refId = event.getMessage().getMessageReference().getMessageIdLong();
        TestDefinition t = byMessageId.get(refId);
        if (t == null) return;

        String content = event.getMessage().getContentDisplay();
        if (content.isBlank()) {
            content = event.getMessage().getAttachments().isEmpty()
                    ? "(empty message)" : "(attachment)";
        }
        String author = event.getMember() != null
                ? event.getMember().getEffectiveName()
                : event.getAuthor().getName();
        try {
            store.addNote(t.num(), author, content);
            event.getMessage().addReaction(Emoji.fromUnicode(EMOJI_NOTE)).queue();
            updateEmbed(event.getJDA(), t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Embed state ---

    /** "pass" | "fail" | "retest" | "none" (known tests are never asked). */
    private String stateOf(TestDefinition t) {
        try {
            Map<String, Integer> votes = store.voteCounts(t.num());
            int fail = votes.getOrDefault("fail", 0);
            int pass = votes.getOrDefault("pass", 0);
            if (fail > 0) return "fail";
            if (store.isRetest(t.num())) return "retest";
            if (pass > 0) return "pass";
            return "none";
        } catch (Exception e) {
            return "none";
        }
    }

    private MessageEmbed buildEmbed(TestDefinition t) throws Exception {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("#" + String.format("%02d", t.num()) + " · " + t.action());
        eb.setFooter(t.section() + "  ·  " + t.badge());

        if (t.isKnown()) {
            eb.setColor(AMBER);
            eb.setDescription("**Expected:** " + t.expected()
                    + "\n\n⚠️ **KNOWN BUG — skip.** " + t.known());
            return eb.build();
        }

        Map<String, Integer> votes = store.voteCounts(t.num());
        int pass = votes.getOrDefault("pass", 0);
        int fail = votes.getOrDefault("fail", 0);
        int noteCount = store.notes(t.num()).size();
        String state = stateOf(t);

        String statusWord = switch (state) {
            case "pass" -> "**PASSED**";
            case "fail" -> "**FAILED**";
            case "retest" -> "**NEEDS MORE TESTING**";
            default -> "untested";
        };
        eb.setColor(switch (state) {
            case "pass" -> GREEN;
            case "fail" -> RED;
            case "retest" -> AMBER;
            default -> GREY;
        });
        eb.setDescription("**Expected:** " + t.expected()
                + "\n\nStatus: " + statusWord
                + "  ·  ✅ " + pass + "  ·  ❌ " + fail
                + (noteCount > 0 ? "  ·  📝 " + noteCount : ""));
        return eb.build();
    }

    private void updateEmbed(net.dv8tion.jda.api.JDA jda, TestDefinition t) throws Exception {
        Long threadId = threadByTest.get(t.num());
        if (threadId == null) return;
        ThreadChannel thread = jda.getThreadChannelById(threadId);
        if (thread == null) return;
        Long msgId = null;
        for (Map.Entry<Long, TestDefinition> e : byMessageId.entrySet()) {
            if (e.getValue().num() == t.num()) { msgId = e.getKey(); break; }
        }
        if (msgId == null) return;
        MessageEmbed embed = buildEmbed(t);
        thread.retrieveMessageById(msgId)
                .queue(m -> m.editMessageEmbeds(embed).queue(), err -> {});
    }

    private static String trim(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1);
    }
}
