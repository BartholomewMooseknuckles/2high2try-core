package com.twohigh.api.social.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class VoteResolvedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID nominee;
    private final String jobId;
    private final boolean passed;
    private final int yesVotes;
    private final int noVotes;

    public VoteResolvedEvent(UUID nominee, String jobId, boolean passed, int yesVotes, int noVotes) {
        this.nominee = nominee;
        this.jobId = jobId;
        this.passed = passed;
        this.yesVotes = yesVotes;
        this.noVotes = noVotes;
    }

    public UUID getNominee() { return nominee; }
    public String getJobId() { return jobId; }
    public boolean hasPassed() { return passed; }
    public int getYesVotes() { return yesVotes; }
    public int getNoVotes() { return noVotes; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
