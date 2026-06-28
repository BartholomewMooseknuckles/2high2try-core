package com.twohigh.core.defaults;

import com.twohigh.api.job.JobDefinition;
import com.twohigh.core.config.CoreConfig;
import com.twohigh.core.job.JobRegistryImpl;

import org.bukkit.plugin.java.JavaPlugin;

public final class DefaultJobs {

    public static final String CITIZEN = "citizen";
    public static final String MAYOR = "mayor";
    public static final String POLICE = "police";
    public static final String CHIEF = "chief";
    public static final String GANGSTER = "gangster";
    public static final String THIEF = "thief";
    public static final String HITMAN = "hitman";

    private DefaultJobs() {}

    public static void register(JobRegistryImpl registry, JavaPlugin plugin, CoreConfig config) {
        long interval = config.defaultSalaryIntervalMs();

        if (config.isDefaultJobEnabled(CITIZEN)) {
            registry.registerJob(new JobDefinition(
                    CITIZEN, "Citizen", true,
                    50.0, interval, null, plugin,
                    -1, "civilian", false, null, null, "§7"));
        }

        if (config.isDefaultJobEnabled(MAYOR)) {
            registry.registerJob(new JobDefinition(
                    MAYOR, "Mayor", true,
                    300.0, interval, null, plugin,
                    1, "government", true, null, "government", "§6"));
        }

        if (config.isDefaultJobEnabled(POLICE)) {
            registry.registerJob(new JobDefinition(
                    POLICE, "Police Officer", true,
                    200.0, interval, null, plugin,
                    4, "police", false, null, "government", "§9"));
        }

        if (config.isDefaultJobEnabled(CHIEF)) {
            registry.registerJob(new JobDefinition(
                    CHIEF, "Police Chief", true,
                    350.0, interval, null, plugin,
                    1, "police", false, POLICE, "government", "§1"));
        }

        if (config.isDefaultJobEnabled(GANGSTER)) {
            registry.registerJob(new JobDefinition(
                    GANGSTER, "Gangster", false,
                    0.0, 0, null, plugin,
                    3, "criminal", false, null, null, "§c"));
        }

        if (config.isDefaultJobEnabled(THIEF)) {
            registry.registerJob(new JobDefinition(
                    THIEF, "Thief", false,
                    0.0, 0, null, plugin,
                    2, "criminal", false, null, null, "§4"));
        }

        if (config.isDefaultJobEnabled(HITMAN)) {
            registry.registerJob(new JobDefinition(
                    HITMAN, "Hitman", false,
                    0.0, 0, null, plugin,
                    1, "criminal", false, null, null, "§8"));
        }
    }
}
