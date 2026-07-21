package com.twohigh.testbot;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ReportBuilder {

    private ReportBuilder() {}

    public static String build(Map<String, List<TestDefinition>> sections,
                               TestStore store,
                               Function<TestDefinition, String> stateOf) throws Exception {
        List<TestDefinition> all = new ArrayList<>();
        sections.values().forEach(all::addAll);

        List<TestDefinition> testable = all.stream().filter(t -> !t.isKnown()).toList();
        List<TestDefinition> failed = testable.stream().filter(t -> stateOf.apply(t).equals("fail")).toList();
        List<TestDefinition> retest = testable.stream().filter(t -> stateOf.apply(t).equals("retest")).toList();
        List<TestDefinition> passed = testable.stream().filter(t -> stateOf.apply(t).equals("pass")).toList();
        List<TestDefinition> untested = testable.stream().filter(t -> stateOf.apply(t).equals("none")).toList();

        StringBuilder sb = new StringBuilder();
        sb.append("2high2try-core v1 — Test Campaign Report\n");
        sb.append("Generated: ").append(ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"))).append("\n");
        sb.append("Result: ").append(passed.size()).append(" passed / ")
                .append(failed.size()).append(" failed / ")
                .append(retest.size()).append(" need retest / ")
                .append(untested.size()).append(" untested (of ")
                .append(testable.size()).append(")\n\n");

        sb.append("=== FAILURES ===\n");
        if (failed.isEmpty()) sb.append("(none)\n");
        for (TestDefinition t : failed) appendFull(sb, t, store);
        sb.append("\n");

        if (!retest.isEmpty()) {
            sb.append("=== NEEDS MORE TESTING ===\n");
            for (TestDefinition t : retest) appendFull(sb, t, store);
            sb.append("\n");
        }

        StringBuilder other = new StringBuilder();
        for (TestDefinition t : all) {
            String st = t.isKnown() ? "known" : stateOf.apply(t);
            if (st.equals("fail") || st.equals("retest")) continue;
            List<TestStore.Note> notes = store.notes(t.num());
            if (notes.isEmpty()) continue;
            other.append("#").append(String.format("%02d", t.num()))
                    .append(" [").append(st.toUpperCase()).append("] ").append(t.action()).append("\n");
            for (TestStore.Note n : notes) {
                other.append("    ").append(n.author()).append(": ").append(n.content()).append("\n");
            }
        }
        if (other.length() > 0) {
            sb.append("=== NOTES ON OTHER TESTS ===\n").append(other).append("\n");
        }

        if (!untested.isEmpty()) {
            sb.append("=== UNTESTED ===\n");
            sb.append(String.join(", ", untested.stream().map(t -> "#" + t.num()).toList()));
            sb.append("\n\n");
        }

        sb.append("=== KNOWN ISSUES (pre-logged, excluded from this campaign) ===\n");
        for (TestDefinition t : all) {
            if (t.isKnown()) {
                sb.append("#").append(String.format("%02d", t.num())).append(" ")
                        .append(t.action()).append(" — ").append(t.known()).append("\n");
            }
        }
        return sb.toString();
    }

    private static void appendFull(StringBuilder sb, TestDefinition t, TestStore store) throws Exception {
        sb.append("#").append(String.format("%02d", t.num()))
                .append(" [").append(t.section()).append("] ").append(t.action()).append("\n");
        sb.append("    expected: ").append(t.expected()).append("\n");
        Map<String, Integer> votes = store.voteCounts(t.num());
        sb.append("    votes: ✅ ").append(votes.getOrDefault("pass", 0))
                .append(" / ❌ ").append(votes.getOrDefault("fail", 0)).append("\n");
        List<TestStore.Note> notes = store.notes(t.num());
        if (notes.isEmpty()) {
            sb.append("    notes: (none)\n");
        } else {
            for (TestStore.Note n : notes) {
                sb.append("    ").append(n.author()).append(": ").append(n.content()).append("\n");
            }
        }
        sb.append("\n");
    }
}
