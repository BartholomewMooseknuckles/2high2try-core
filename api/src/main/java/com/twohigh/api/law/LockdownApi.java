package com.twohigh.api.law;

import java.util.UUID;

public interface LockdownApi {

    boolean start(UUID issuer);

    boolean end(UUID issuer);

    boolean isActive();
}
