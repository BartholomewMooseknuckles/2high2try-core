package com.twohigh.api.social;

import java.util.UUID;

public interface GroupChatApi {

    void sendTeamMessage(UUID sender, String message);

    boolean isGroupChatEnabled(UUID player);

    void toggleGroupChat(UUID player);
}
