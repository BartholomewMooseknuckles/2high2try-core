package com.twohigh.core.law;

import com.twohigh.api.law.LicenseApi;
import com.twohigh.core.data.CoreStorage;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LicenseManager implements LicenseApi {

    private final Set<UUID> licenses = ConcurrentHashMap.newKeySet();
    private final CoreStorage storage;

    public LicenseManager(CoreStorage storage) {
        this.storage = storage;
    }

    public void loadFromDatabase() {
        storage.loadAllLicenses().thenAccept(set -> {
            licenses.clear();
            licenses.addAll(set);
        });
    }

    @Override
    public boolean grant(UUID officer, UUID target) {
        if (licenses.contains(target)) return false;
        licenses.add(target);
        storage.saveLicense(target);
        return true;
    }

    @Override
    public boolean revoke(UUID officer, UUID target) {
        if (!licenses.remove(target)) return false;
        storage.removeLicense(target);
        return true;
    }

    @Override
    public boolean hasLicense(UUID target) {
        return licenses.contains(target);
    }
}
