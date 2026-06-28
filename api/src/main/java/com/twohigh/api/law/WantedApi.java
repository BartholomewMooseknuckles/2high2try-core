package com.twohigh.api.law;

import java.util.Collection;
import java.util.UUID;

public interface WantedApi {

    boolean setWanted(UUID officer, UUID target, String reason);

    boolean removeWanted(UUID target);

    boolean isWanted(UUID target);

    Collection<UUID> allWanted();
}
