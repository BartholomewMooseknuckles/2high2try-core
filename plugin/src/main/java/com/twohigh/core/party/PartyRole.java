package com.twohigh.core.party;

public enum PartyRole {
    LEADER,
    OFFICER,
    MEMBER;

    public boolean canInvite() {
        return this == LEADER || this == OFFICER;
    }

    public boolean canKick() {
        return this == LEADER || this == OFFICER;
    }

    public boolean canWithdraw() {
        return this == LEADER || this == OFFICER;
    }

    public boolean canSetRole() {
        return this == LEADER;
    }

    public boolean canDisband() {
        return this == LEADER;
    }

    public boolean canToggleFriendlyFire() {
        return this == LEADER;
    }
}
