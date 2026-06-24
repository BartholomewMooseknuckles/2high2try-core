package com.twohigh.api.claim;

import java.util.UUID;

public record ClaimInfo(
        UUID owner,
        String regionId,
        boolean raidActive,
        boolean decaying,
        int blockCount
) {}
