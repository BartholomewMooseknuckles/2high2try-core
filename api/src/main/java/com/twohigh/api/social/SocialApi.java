package com.twohigh.api.social;

public interface SocialApi {

    GroupChatApi groupChat();

    ChatApi chat();

    AgendaApi agenda();

    VoteApi votes();

    DemoteApi demote();
}
