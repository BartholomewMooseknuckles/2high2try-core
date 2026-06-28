package com.twohigh.core.social;

import com.twohigh.api.social.AgendaApi;
import com.twohigh.api.social.ChatApi;
import com.twohigh.api.social.DemoteApi;
import com.twohigh.api.social.GroupChatApi;
import com.twohigh.api.social.SocialApi;
import com.twohigh.api.social.VoteApi;

public final class SocialService implements SocialApi {

    private final GroupChatManager groupChatManager;
    private final ChatManager chatManager;
    private final AgendaManager agendaManager;
    private final VoteManager voteManager;
    private final DemoteManager demoteManager;

    public SocialService(GroupChatManager groupChat, ChatManager chat,
                         AgendaManager agenda, VoteManager vote, DemoteManager demote) {
        this.groupChatManager = groupChat;
        this.chatManager = chat;
        this.agendaManager = agenda;
        this.voteManager = vote;
        this.demoteManager = demote;
    }

    @Override public GroupChatApi groupChat() { return groupChatManager; }
    @Override public ChatApi chat() { return chatManager; }
    @Override public AgendaApi agenda() { return agendaManager; }
    @Override public VoteApi votes() { return voteManager; }
    @Override public DemoteApi demote() { return demoteManager; }

    public GroupChatManager groupChatManager() { return groupChatManager; }
    public ChatManager chatManager() { return chatManager; }
    public AgendaManager agendaManager() { return agendaManager; }
    public VoteManager voteManager() { return voteManager; }
    public DemoteManager demoteManager() { return demoteManager; }
}
