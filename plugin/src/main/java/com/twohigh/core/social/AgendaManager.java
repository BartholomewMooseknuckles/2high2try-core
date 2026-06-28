package com.twohigh.core.social;

import com.twohigh.api.social.AgendaApi;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AgendaManager implements AgendaApi {

    private final ConcurrentHashMap<String, String> agendas = new ConcurrentHashMap<>();

    @Override
    public boolean setAgenda(UUID leader, String team, String agenda) {
        agendas.put(team, agenda);
        return true;
    }

    @Override
    public Optional<String> getAgenda(String team) {
        return Optional.ofNullable(agendas.get(team));
    }

    @Override
    public void clearAgenda(String team) {
        agendas.remove(team);
    }
}
