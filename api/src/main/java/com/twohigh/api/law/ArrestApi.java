package com.twohigh.api.law;

import java.util.UUID;

public interface ArrestApi {

    boolean arrest(UUID officer, UUID target);

    boolean unarrest(UUID officer, UUID target);

    boolean isJailed(UUID player);

    long getRemainingJailTimeMs(UUID player);
}
