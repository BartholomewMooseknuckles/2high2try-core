package com.twohigh.api.social;

import java.util.UUID;

public interface VoteApi {

    boolean startVote(UUID nominee, String jobId);

    boolean castVote(UUID voter, boolean approve);

    boolean isVoteActive();

    String getActiveJobId();

    UUID getNominee();
}
