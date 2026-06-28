package com.twohigh.api.social;

import java.util.UUID;

public interface DemoteApi {

    boolean startDemoteVote(UUID initiator, UUID target);

    boolean castDemoteVote(UUID voter, boolean approve);

    boolean isDemoteVoteActive();

    boolean isDemoteBanned(UUID player, String demoteGroup);
}
