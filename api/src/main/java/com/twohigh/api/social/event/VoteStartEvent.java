package com.twohigh.api.social.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class VoteStartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID nominee;
    private final String jobId;

    public VoteStartEvent(UUID nominee, String jobId) {
        this.nominee = nominee;
        this.jobId = jobId;
    }

    public UUID getNominee() { return nominee; }
    public String getJobId() { return jobId; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
