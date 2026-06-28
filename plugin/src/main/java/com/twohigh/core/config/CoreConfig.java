package com.twohigh.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class CoreConfig {

    private final JavaPlugin plugin;

    private boolean overworldPvP;
    private boolean netherPvP;
    private boolean endPvP;
    private int combatTagSeconds;

    private int mugCap;
    private int mugCooldownMinutes;
    private int mugWindowSeconds;
    private int mugMaxDistance;

    private int raidAdvertCost;
    private int raidCoordPublishDelay;
    private int raidBubbleRadius;
    private int raidPerBaseCooldownMinutes;
    private int raidSameRaiderCooldownHours;
    private int raidDurationSeconds;
    private int raidAbandonGraceSeconds;
    private int raidCooldownFloorMinutes;
    private int raidCooldownMinutesPerPercent;
    private int raidCooldownMaxMinutes;

    private int printerYieldPerHour;
    private boolean printerReturnsItemOnBreak;

    private double pdSalaryPerInterval;
    private int pdSalaryIntervalMinutes;
    private double pdSeizeBonusPct;

    private int dogSitRadius;
    private int dogTreatIntervalSeconds;
    private double dogDistanceFalloffExponent;
    private int dogGrowSiteMergeRadius;

    private String mysqlHost;
    private int mysqlPort;
    private String mysqlDatabase;
    private String mysqlUsername;
    private String mysqlPassword;

    private long defaultSalaryIntervalMs;
    private final Set<String> disabledDefaultJobs = new HashSet<>();

    private int jailTimeSeconds;
    private int warrantExpireSeconds;
    private int lockdownSlownessLevel;

    private int partyMaxSize;
    private String partyFriendlyFireMode;
    private boolean partyPersistence;

    private int chatLocalRadius;
    private int chatWhisperRadius;
    private int chatYellRadius;
    private boolean chatShowJobTitle;

    public CoreConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();

        overworldPvP = c.getBoolean("pvp.overworld_enabled", false);
        netherPvP = c.getBoolean("pvp.nether_enabled", true);
        endPvP = c.getBoolean("pvp.end_enabled", true);
        combatTagSeconds = c.getInt("pvp.combat_tag_seconds", 60);

        mugCap = c.getInt("mugging.cap", 10000);
        mugCooldownMinutes = c.getInt("mugging.cooldown_minutes", 20);
        mugWindowSeconds = c.getInt("mugging.window_seconds", 30);
        mugMaxDistance = c.getInt("mugging.max_distance", 6);

        raidAdvertCost = c.getInt("raids.advert_cost", 5000);
        raidCoordPublishDelay = c.getInt("raids.coord_publish_delay_seconds", 60);
        raidBubbleRadius = c.getInt("raids.bubble_radius", 32);
        raidPerBaseCooldownMinutes = c.getInt("raids.per_base_cooldown_minutes", 30);
        raidSameRaiderCooldownHours = c.getInt("raids.same_raider_same_base_cooldown_hours", 24);
        raidDurationSeconds = c.getInt("raids.duration_seconds", 1800);
        raidAbandonGraceSeconds = c.getInt("raids.abandon_grace_seconds", 30);
        raidCooldownFloorMinutes = c.getInt("raids.cooldown_floor_minutes", 5);
        raidCooldownMinutesPerPercent = c.getInt("raids.cooldown_minutes_per_percent", 1);
        raidCooldownMaxMinutes = c.getInt("raids.cooldown_max_minutes", 120);

        printerYieldPerHour = c.getInt("economy.printer_yield_per_hour", 500);
        printerReturnsItemOnBreak = c.getBoolean("economy.printer_returns_item_on_break", false);

        pdSalaryPerInterval = c.getDouble("pd.salary_per_interval", 500);
        pdSalaryIntervalMinutes = c.getInt("pd.salary_interval_minutes", 30);
        pdSeizeBonusPct = c.getDouble("pd.seize_bonus_pct", 25);

        dogSitRadius = c.getInt("dog.sit_radius", 50);
        dogTreatIntervalSeconds = c.getInt("dog.treat_interval_seconds", 60);
        dogDistanceFalloffExponent = c.getDouble("dog.distance_falloff_exponent", 2.0);
        dogGrowSiteMergeRadius = c.getInt("dog.grow_site_merge_radius", 50);

        mysqlHost = c.getString("mysql.host", "localhost");
        mysqlPort = c.getInt("mysql.port", 3306);
        mysqlDatabase = c.getString("mysql.database", "twohigh2try");
        mysqlUsername = c.getString("mysql.username", "root");
        mysqlPassword = c.getString("mysql.password", "");

        jailTimeSeconds = c.getInt("law.jail_time_seconds", 120);
        warrantExpireSeconds = c.getInt("law.warrant_expire_seconds", 300);
        lockdownSlownessLevel = c.getInt("law.lockdown_slowness_level", 1);

        defaultSalaryIntervalMs = c.getLong("defaults.salary_interval_minutes", 30) * 60_000L;
        disabledDefaultJobs.clear();
        for (String job : c.getStringList("defaults.disabled_jobs")) {
            disabledDefaultJobs.add(job.toLowerCase());
        }

        partyMaxSize = c.getInt("party.max_size_default", 6);
        partyFriendlyFireMode = c.getString("party.friendly_fire_mode", "party_choice");
        partyPersistence = c.getBoolean("party.persistence", false);

        chatLocalRadius = c.getInt("chat.local_radius", 50);
        chatWhisperRadius = c.getInt("chat.whisper_radius", 10);
        chatYellRadius = c.getInt("chat.yell_radius", 150);
        chatShowJobTitle = c.getBoolean("chat.show_job_title", true);
    }

    public boolean overworldPvP() { return overworldPvP; }
    public boolean netherPvP() { return netherPvP; }
    public boolean endPvP() { return endPvP; }
    public int combatTagSeconds() { return combatTagSeconds; }
    public int mugCap() { return mugCap; }
    public int mugCooldownMinutes() { return mugCooldownMinutes; }
    public int mugWindowSeconds() { return mugWindowSeconds; }
    public int mugMaxDistance() { return mugMaxDistance; }
    public int raidAdvertCost() { return raidAdvertCost; }
    public int raidCoordPublishDelay() { return raidCoordPublishDelay; }
    public int raidBubbleRadius() { return raidBubbleRadius; }
    public int raidPerBaseCooldownMinutes() { return raidPerBaseCooldownMinutes; }
    public int raidSameRaiderCooldownHours() { return raidSameRaiderCooldownHours; }
    public int raidDurationSeconds() { return raidDurationSeconds; }
    public int raidAbandonGraceSeconds() { return raidAbandonGraceSeconds; }
    public int raidCooldownFloorMinutes() { return raidCooldownFloorMinutes; }
    public int raidCooldownMinutesPerPercent() { return raidCooldownMinutesPerPercent; }
    public int raidCooldownMaxMinutes() { return raidCooldownMaxMinutes; }
    public int printerYieldPerHour() { return printerYieldPerHour; }
    public boolean printerReturnsItemOnBreak() { return printerReturnsItemOnBreak; }
    public double pdSalaryPerInterval() { return pdSalaryPerInterval; }
    public int pdSalaryIntervalMinutes() { return pdSalaryIntervalMinutes; }
    public double pdSeizeBonusPct() { return pdSeizeBonusPct; }
    public int dogSitRadius() { return dogSitRadius; }
    public int dogTreatIntervalSeconds() { return dogTreatIntervalSeconds; }
    public double dogDistanceFalloffExponent() { return dogDistanceFalloffExponent; }
    public int dogGrowSiteMergeRadius() { return dogGrowSiteMergeRadius; }
    public String mysqlHost() { return mysqlHost; }
    public int mysqlPort() { return mysqlPort; }
    public String mysqlDatabase() { return mysqlDatabase; }
    public String mysqlUsername() { return mysqlUsername; }
    public String mysqlPassword() { return mysqlPassword; }
    public int jailTimeSeconds() { return jailTimeSeconds; }
    public long jailTimeMs() { return jailTimeSeconds * 1000L; }
    public int warrantExpireSeconds() { return warrantExpireSeconds; }
    public long warrantExpireMs() { return warrantExpireSeconds * 1000L; }
    public int lockdownSlownessLevel() { return lockdownSlownessLevel; }
    public long defaultSalaryIntervalMs() { return defaultSalaryIntervalMs; }
    public int partyMaxSize() { return partyMaxSize; }
    public String partyFriendlyFireMode() { return partyFriendlyFireMode; }
    public boolean partyPersistence() { return partyPersistence; }

    public int chatLocalRadius() { return chatLocalRadius; }
    public int chatWhisperRadius() { return chatWhisperRadius; }
    public int chatYellRadius() { return chatYellRadius; }
    public boolean chatShowJobTitle() { return chatShowJobTitle; }

    public boolean isDefaultJobEnabled(String jobId) {
        return !disabledDefaultJobs.contains(jobId.toLowerCase());
    }
}
