package com.twohigh.api.scoreboard;

import java.util.UUID;

public interface ScoreboardApi {

    void showSidebar(UUID player);

    void hideSidebar(UUID player);

    void refreshAll();
}
