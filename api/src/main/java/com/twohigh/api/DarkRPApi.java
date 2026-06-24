package com.twohigh.api;

import com.twohigh.api.claim.ClaimApi;
import com.twohigh.api.detection.DetectionApi;
import com.twohigh.api.economy.EconomyApi;
import com.twohigh.api.job.JobRegistry;
import com.twohigh.api.pvp.CombatTagApi;
import com.twohigh.api.pvp.PvPApi;

public interface DarkRPApi {

    EconomyApi economy();

    JobRegistry jobs();

    DetectionApi detection();

    ClaimApi claims();

    PvPApi pvp();

    CombatTagApi combatTag();

    static DarkRPApi get() {
        DarkRPApi api = Holder.INSTANCE;
        if (api == null) {
            throw new IllegalStateException("2high2try-core is not enabled");
        }
        return api;
    }

    static void setInstance(DarkRPApi api) {
        Holder.INSTANCE = api;
    }

    static boolean isAvailable() {
        return Holder.INSTANCE != null;
    }
}

final class Holder {
    static volatile DarkRPApi INSTANCE;
}
