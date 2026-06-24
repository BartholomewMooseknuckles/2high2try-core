package com.twohigh.core;

import com.twohigh.api.DarkRPApi;
import com.twohigh.api.claim.ClaimApi;
import com.twohigh.api.detection.DetectionApi;
import com.twohigh.api.economy.EconomyApi;
import com.twohigh.api.job.JobRegistry;
import com.twohigh.api.pvp.CombatTagApi;
import com.twohigh.api.pvp.PvPApi;
import com.twohigh.core.audit.AuditService;
import com.twohigh.core.claim.ClaimManagerImpl;
import com.twohigh.core.command.AdvertCommand;
import com.twohigh.core.command.BalanceCommand;
import com.twohigh.core.command.JobCommand;
import com.twohigh.core.command.PayCommand;
import com.twohigh.core.config.CoreConfig;
import com.twohigh.core.data.CoreStorage;
import com.twohigh.core.data.mysql.MysqlStorage;
import com.twohigh.core.detection.DetectionManagerImpl;
import com.twohigh.core.economy.CashManager;
import com.twohigh.core.economy.DualEconomyService;
import com.twohigh.core.integration.NoopVaultHook;
import com.twohigh.core.integration.NoopWorldGuardHook;
import com.twohigh.core.integration.VaultHook;
import com.twohigh.core.integration.VaultHookImpl;
import com.twohigh.core.integration.WorldGuardHook;
import com.twohigh.core.integration.WorldGuardHookImpl;
import com.twohigh.core.job.JobRegistryImpl;
import com.twohigh.core.job.SalaryTask;
import com.twohigh.core.leaderboard.LeaderboardManager;
import com.twohigh.core.listener.DeathListener;
import com.twohigh.core.listener.DrugDogListener;
import com.twohigh.core.listener.JoinQuitListener;
import com.twohigh.core.listener.PrinterListener;
import com.twohigh.core.listener.PvPListener;
import com.twohigh.core.mug.MugManager;
import com.twohigh.core.printer.MoneyPrinterManager;
import com.twohigh.core.pvp.CombatTagManager;
import com.twohigh.core.pvp.PvPManager;
import com.twohigh.core.raid.RaidManager;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.logging.Level;

public final class TwoHigh2TryCore extends JavaPlugin implements DarkRPApi {

    private CoreConfig config;
    private CoreStorage storage;
    private CashManager cashManager;
    private VaultHook vaultHook;
    private DualEconomyService economyService;
    private JobRegistryImpl jobRegistry;
    private PvPManager pvpManager;
    private CombatTagManager combatTagManager;
    private WorldGuardHook worldGuardHook;
    private ClaimManagerImpl claimManager;
    private RaidManager raidManager;
    private MugManager mugManager;
    private DetectionManagerImpl detectionManager;
    private MoneyPrinterManager printerManager;
    private LeaderboardManager leaderboardManager;
    private AuditService auditService;
    private BukkitTask salaryTask;

    @Override
    public void onEnable() {
        try {
            this.config = new CoreConfig(this);
            this.config.load();

            this.storage = new MysqlStorage(getLogger(),
                    config.mysqlHost(), config.mysqlPort(), config.mysqlDatabase(),
                    config.mysqlUsername(), config.mysqlPassword());
            this.storage.init();

            this.vaultHook = makeVaultHook();
            this.cashManager = new CashManager(storage, getLogger());
            this.economyService = new DualEconomyService(cashManager, vaultHook);

            this.jobRegistry = new JobRegistryImpl(storage, getLogger());
            this.combatTagManager = new CombatTagManager(config);
            this.pvpManager = new PvPManager(config, combatTagManager);

            this.worldGuardHook = makeWorldGuardHook();
            this.claimManager = new ClaimManagerImpl(worldGuardHook);
            this.raidManager = new RaidManager(this);
            this.mugManager = new MugManager(this);

            this.detectionManager = new DetectionManagerImpl(config);
            this.printerManager = new MoneyPrinterManager(this);
            this.leaderboardManager = new LeaderboardManager(this);
            this.auditService = new AuditService(getLogger());

            DarkRPApi.setInstance(this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Startup failed — disabling 2high2try-core.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);
        getServer().getPluginManager().registerEvents(new DrugDogListener(this), this);
        getServer().getPluginManager().registerEvents(new PrinterListener(this), this);

        bindCommand("balance", new BalanceCommand(this));
        PayCommand payCmd = new PayCommand(this);
        PluginCommand pay = getCommand("pay");
        if (pay != null) {
            pay.setExecutor(payCmd);
            pay.setTabCompleter(payCmd);
        }
        JobCommand jobCmd = new JobCommand(this);
        PluginCommand job = getCommand("job");
        if (job != null) {
            job.setExecutor(jobCmd);
            job.setTabCompleter(jobCmd);
        }
        AdvertCommand advertCmd = new AdvertCommand(this);
        PluginCommand advert = getCommand("advert");
        if (advert != null) {
            advert.setExecutor(advertCmd);
            advert.setTabCompleter(advertCmd);
        }

        SalaryTask salary = new SalaryTask(jobRegistry, cashManager);
        this.salaryTask = salary.runTaskTimer(this, 20L * 60, 20L * 60);

        getLogger().info("2high2try-core enabled (all phases).");
    }

    @Override
    public void onDisable() {
        org.bukkit.event.HandlerList.unregisterAll(this);

        if (salaryTask != null) {
            salaryTask.cancel();
            salaryTask = null;
        }

        if (printerManager != null) {
            printerManager.shutdown();
            printerManager = null;
        }
        if (raidManager != null) {
            raidManager.shutdown();
            raidManager = null;
        }

        DarkRPApi.setInstance(null);

        if (cashManager != null) {
            cashManager.flushAll();
            cashManager = null;
        }
        if (storage != null) {
            storage.shutdown();
            storage = null;
        }
        vaultHook = null;
        economyService = null;
        jobRegistry = null;
        pvpManager = null;
        combatTagManager = null;
        worldGuardHook = null;
        claimManager = null;
        mugManager = null;
        detectionManager = null;
        leaderboardManager = null;
        auditService = null;
        config = null;
        getLogger().info("2high2try-core disabled.");
    }

    private VaultHook makeVaultHook() {
        if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            getLogger().warning("Vault not found — bank balance will be unavailable.");
            return new NoopVaultHook();
        }
        try {
            Optional<VaultHook> hooked = VaultHookImpl.tryCreate(getServer(), getLogger());
            if (hooked.isEmpty()) {
                getLogger().warning("Vault is installed but no economy provider is registered.");
                return new NoopVaultHook();
            }
            return hooked.get();
        } catch (Throwable t) {
            getLogger().log(Level.WARNING, "Vault hook failed — bank balance disabled.", t);
            return new NoopVaultHook();
        }
    }

    private WorldGuardHook makeWorldGuardHook() {
        if (!getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            getLogger().warning("WorldGuard not found — claims will be unavailable.");
            return new NoopWorldGuardHook();
        }
        try {
            Optional<WorldGuardHook> hooked = WorldGuardHookImpl.tryCreate(getLogger());
            if (hooked.isEmpty()) {
                getLogger().warning("WorldGuard hook failed to initialize.");
                return new NoopWorldGuardHook();
            }
            return hooked.get();
        } catch (Throwable t) {
            getLogger().log(Level.WARNING, "WorldGuard hook failed — claims disabled.", t);
            return new NoopWorldGuardHook();
        }
    }

    private void bindCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
        } else {
            getLogger().warning("Command '" + name + "' missing from plugin.yml.");
        }
    }

    // --- DarkRPApi implementation ---

    @Override public EconomyApi economy() { return economyService; }
    @Override public JobRegistry jobs() { return jobRegistry; }
    @Override public DetectionApi detection() { return detectionManager; }
    @Override public ClaimApi claims() { return claimManager; }
    @Override public PvPApi pvp() { return pvpManager; }
    @Override public CombatTagApi combatTag() { return combatTagManager; }

    // --- Internal accessors ---

    public CoreConfig coreConfig() { return config; }
    public CoreStorage storage() { return storage; }
    public CashManager cashManager() { return cashManager; }
    public DualEconomyService economyService() { return economyService; }
    public VaultHook vaultHook() { return vaultHook; }
    public JobRegistryImpl jobRegistry() { return jobRegistry; }
    public PvPManager pvpManager() { return pvpManager; }
    public CombatTagManager combatTagManager() { return combatTagManager; }
    public WorldGuardHook worldGuardHook() { return worldGuardHook; }
    public ClaimManagerImpl claimManager() { return claimManager; }
    public RaidManager raidManager() { return raidManager; }
    public MugManager mugManager() { return mugManager; }
    public DetectionManagerImpl detectionManager() { return detectionManager; }
    public MoneyPrinterManager printerManager() { return printerManager; }
    public LeaderboardManager leaderboardManager() { return leaderboardManager; }
    public AuditService auditService() { return auditService; }
}
