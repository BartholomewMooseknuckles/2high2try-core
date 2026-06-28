package com.twohigh.api.party;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PartyApi {

    Optional<UUID> getPartyId(UUID player);

    boolean areInSameParty(UUID a, UUID b);

    boolean isFriendlyFireEnabled(UUID partyId);

    Set<UUID> getMembers(UUID partyId);

    Optional<String> getPartyLeaderName(UUID partyId);

    double getPartyBankBalance(UUID partyId);
}
