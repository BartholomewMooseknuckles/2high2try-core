package com.twohigh.testbot;

public record TestDefinition(
        int num,
        String section,
        String badge,
        String action,
        String expected,
        String known   // null unless this is a pre-flagged known bug
) {
    public boolean isKnown() {
        return known != null && !known.isBlank();
    }
}
