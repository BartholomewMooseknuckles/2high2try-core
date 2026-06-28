package com.twohigh.core.law;

import com.twohigh.api.law.ArrestApi;
import com.twohigh.api.law.LawEnforcementApi;
import com.twohigh.api.law.LicenseApi;
import com.twohigh.api.law.LockdownApi;
import com.twohigh.api.law.WantedApi;
import com.twohigh.api.law.WarrantApi;

public final class LawEnforcementService implements LawEnforcementApi {

    private final ArrestManager arrestManager;
    private final WarrantManager warrantManager;
    private final WantedManager wantedManager;
    private final LockdownManager lockdownManager;
    private final LicenseManager licenseManager;

    public LawEnforcementService(ArrestManager arrestManager, WarrantManager warrantManager,
                                  WantedManager wantedManager, LockdownManager lockdownManager,
                                  LicenseManager licenseManager) {
        this.arrestManager = arrestManager;
        this.warrantManager = warrantManager;
        this.wantedManager = wantedManager;
        this.lockdownManager = lockdownManager;
        this.licenseManager = licenseManager;
    }

    @Override public ArrestApi arrests() { return arrestManager; }
    @Override public WarrantApi warrants() { return warrantManager; }
    @Override public WantedApi wanted() { return wantedManager; }
    @Override public LockdownApi lockdown() { return lockdownManager; }
    @Override public LicenseApi licenses() { return licenseManager; }

    public ArrestManager arrestManager() { return arrestManager; }
    public WarrantManager warrantManager() { return warrantManager; }
    public WantedManager wantedManager() { return wantedManager; }
    public LockdownManager lockdownManager() { return lockdownManager; }
    public LicenseManager licenseManager() { return licenseManager; }
}
