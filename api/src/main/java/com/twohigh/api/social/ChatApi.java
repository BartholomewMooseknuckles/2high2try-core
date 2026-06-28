package com.twohigh.api.social;

import java.util.UUID;

public interface ChatApi {

    int localRadius();

    int whisperRadius();

    int yellRadius();

    void sendLocalMessage(UUID sender, String message);

    void sendWhisperMessage(UUID sender, String message);

    void sendYellMessage(UUID sender, String message);

    void sendOocMessage(UUID sender, String message);
}
