package com.twohigh.api.law;

public interface LawEnforcementApi {

    ArrestApi arrests();

    WarrantApi warrants();

    WantedApi wanted();

    LockdownApi lockdown();

    LicenseApi licenses();
}
