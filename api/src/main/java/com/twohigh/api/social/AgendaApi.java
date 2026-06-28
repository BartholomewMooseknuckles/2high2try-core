package com.twohigh.api.social;

import java.util.Optional;
import java.util.UUID;

public interface AgendaApi {

    boolean setAgenda(UUID leader, String team, String agenda);

    Optional<String> getAgenda(String team);

    void clearAgenda(String team);
}
