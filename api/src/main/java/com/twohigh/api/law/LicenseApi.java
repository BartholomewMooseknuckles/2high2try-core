package com.twohigh.api.law;

import java.util.UUID;

public interface LicenseApi {

    boolean grant(UUID officer, UUID target);

    boolean revoke(UUID officer, UUID target);

    boolean hasLicense(UUID target);
}
