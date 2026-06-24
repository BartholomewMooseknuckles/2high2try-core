package com.twohigh.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class CoreConfig {

    private final JavaPlugin plugin;

    private boolean overworldPvP;
    private boolean netherPvP;
    private boolean endPvP;
    private int combatTagSeconds;

    private int mugCap;
    private int mugCooldownMinutes;

    private int raidAdvertCost;
    private int raidCoordPublishDelay;
    private int raidBubbleRadius;
    private int raidPerBaseCooldownMinutes;
    private int raidSameRaiderCooldownHours;

    private int printerYieldPerHour;

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

        raidAdvertCost = c.getInt("raids.advert_cost", 5000);
        raidCoordPublishDelay = c.getInt("raids.coord_publish_delay_seconds", 60);
        raidBubbleRadius = c.getInt("raids.bubble_radius", 32);
        raidPerBaseCooldownMinutes = c.getInt("raids.per_base_cooldown_minutes", 30);
        raidSameRaiderCooldownHours = c.getInt("raids.same_raider_same_base_cooldown_hours", 24);

        printerYieldPerHour = c.getInt("economy.printer_yield_per_hour", 500);

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
    }

    public boolean overworldPvP() { return overworldPvP; }
    public boolean netherPvP() { return netherPvP; }
    public boolean endPvP() { return endPvP; }
    public int combatTagSeconds() { return combatTagSeconds; }
    public int mugCap() { return mugCap; }
    public int mugCooldownMinutes() { return mugCooldownMinutes; }
    public int raidAdvertCost() { return raidAdvertCost; }
    public int raidCoordPublishDelay() { return raidCoordPublishDelay; }
    public int raidBubbleRadius() { return raidBubbleRadius; }
    public int raidPerBaseCooldownMinutes() { return raidPerBaseCooldownMinutes; }
    public int raidSameRaiderCooldownHours() { return raidSameRaiderCooldownHours; }
    public int printerYieldPerHour() { return printerYieldPerHour; }
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
}
