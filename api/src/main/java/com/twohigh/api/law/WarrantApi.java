package com.twohigh.api.law;

import java.util.UUID;

public interface WarrantApi {

    boolean issue(UUID officer, UUID target, String reason);

    boolean revoke(UUID target);

    boolean hasWarrant(UUID target);
}
