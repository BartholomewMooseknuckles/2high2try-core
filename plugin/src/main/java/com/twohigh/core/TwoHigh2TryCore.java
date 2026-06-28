package com.twohigh.core;

import com.twohigh.api.DarkRPApi;
import com.twohigh.api.cheque.ChequeApi;
import com.twohigh.api.claim.ClaimApi;
import com.twohigh.api.detection.DetectionApi;
import com.twohigh.api.economy.EconomyApi;
import com.twohigh.api.entity.EntityRegistryApi;
import com.twohigh.api.job.JobRegistry;
import com.twohigh.api.law.LawEnforcementApi;
import com.twohigh.api.party.PartyApi;
import com.twohigh.api.pvp.CombatTagApi;
import com.twohigh.api.pvp.PvPApi;
import com.twohigh.api.scoreboard.ScoreboardApi;
import com.twohigh.api.social.SocialApi;
import com.twohigh.core.audit.AuditService;
import com.twohigh.core.cheque.ChequeCommand;
import com.twohigh.core.cheque.ChequeListener;
import com.twohigh.core.cheque.ChequeManager;
import com.twohigh.core.claim.ClaimManagerImpl;
import com.twohigh.core.command.AdvertCommand;
import com.twohigh.core.command.ArrestCommand;
import com.twohigh.core.command.BalanceCommand;
import com.twohigh.core.command.GunLicenseCommand;
import com.twohigh.core.command.JobCommand;
import com.twohigh.core.command.LockdownCommand;
import com.twohigh.core.command.PayCommand;
import com.twohigh.core.command.SetJailCommand;
import com.twohigh.core.command.WantedCommand;
import com.twohigh.core.command.WarrantCommand;
import com.twohigh.core.config.CoreConfig;
import com.twohigh.core.defaults.DefaultJobs;
import com.twohigh.core.data.CoreStorage;
import com.twohigh.core.data.mysql.MysqlStorage;
import com.twohigh.core.detection.DetectionManagerImpl;
import com.twohigh.core.economy.CashManager;
import com.twohigh.core.economy.DualEconomyService;
import com.twohigh.core.entity.EntityListener;
import com.twohigh.core.entity.EntityRegistryImpl;
import com.twohigh.core.integration.NoopVaultHook;
import com.twohigh.core.integration.NoopWorldGuardHook;
import com.twohigh.core.integration.VaultHook;
import com.twohigh.core.integration.VaultHookImpl;
import com.twohigh.core.integration.WorldGuardHook;
import com.twohigh.core.integration.WorldGuardHookImpl;
import com.twohigh.core.job.JobRegistryImpl;
import com.twohigh.core.job.SalaryTask;
import com.twohigh.core.law.ArrestManager;
import com.twohigh.core.law.JailManager;
import com.twohigh.core.law.LawEnforcementService;
import com.twohigh.core.law.LicenseManager;
import com.twohigh.core.law.LockdownManager;
import com.twohigh.core.law.WantedManager;
import com.twohigh.core.law.WarrantManager;
import com.twohigh.core.law.item.LawItemListener;
import com.twohigh.core.law.item.LawItems;
import com.twohigh.core.leaderboard.LeaderboardManager;
import com.twohigh.core.listener.CashTokenListener;
import com.twohigh.core.listener.ClaimBlockListener;
import com.twohigh.core.listener.DeathListener;
import com.twohigh.core.listener.DrugDogListener;
import com.twohigh.core.listener.ChatListener;
import com.twohigh.core.listener.JoinQuitListener;
import com.twohigh.core.listener.LawListener;
import com.twohigh.core.listener.PrinterListener;
import com.twohigh.core.listener.PvPListener;
import com.twohigh.core.mug.MugManager;
import com.twohigh.core.party.PartyChatCommand;
import com.twohigh.core.party.PartyCommand;
import com.twohigh.core.party.PartyManager;
import com.twohigh.core.printer.MoneyPrinterManager;
import com.twohigh.core.pvp.CombatTagManager;
import com.twohigh.core.pvp.PvPManager;
import com.twohigh.core.raid.RaidAccessListener;
import com.twohigh.core.raid.RaidLootListener;
import com.twohigh.core.raid.RaidManager;
import com.twohigh.core.scoreboard.PlayerStatsTracker;
import com.twohigh.core.scoreboard.ScoreboardCommand;
import com.twohigh.core.scoreboard.SidebarManager;
import com.twohigh.core.scoreboard.StatsCommand;
import com.twohigh.core.social.AgendaManager;
import com.twohigh.core.social.DemoteManager;
import com.twohigh.core.social.ChatManager;
import com.twohigh.core.social.GroupChatManager;
import com.twohigh.core.social.SocialService;
import com.twohigh.core.social.VoteManager;
import com.twohigh.core.social.command.AgendaCommand;
import com.twohigh.core.social.command.DemoteCommand;
import com.twohigh.core.social.command.GroupChatCommand;
import com.twohigh.core.social.command.OocCommand;
import com.twohigh.core.social.command.VoteCommand;

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
    private LawEnforcementService lawEnforcement;
    private EntityRegistryImpl entityRegistry;
    private SocialService socialService;
    private SidebarManager sidebarManager;
    private PlayerStatsTracker statsTracker;
    private ChequeManager chequeManager;
    private PartyManager partyManager;
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
            DefaultJobs.register(jobRegistry, this, config);
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

            this.lawEnforcement = makeLawEnforcement();
            LawItems.init(this);

            this.entityRegistry = new EntityRegistryImpl(getLogger());
            this.socialService = makeSocialService();
            this.statsTracker = new PlayerStatsTracker();
            this.sidebarManager = new SidebarManager(this);
            this.chequeManager = new ChequeManager(this, cashManager);
            this.partyManager = new PartyManager(this);

            pvpManager.setRaidManager(raidManager);
            pvpManager.setPartyManager(partyManager);

            DarkRPApi.setInstance(this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Startup failed — disabling 2high2try-core.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Listeners
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);
        getServer().getPluginManager().registerEvents(new DrugDogListener(this), this);
        getServer().getPluginManager().registerEvents(new PrinterListener(this), this);
        getServer().getPluginManager().registerEvents(new LawListener(this), this);
        getServer().getPluginManager().registerEvents(new LawItemListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        getServer().getPluginManager().registerEvents(new ChequeListener(this), this);
        getServer().getPluginManager().registerEvents(new RaidAccessListener(this), this);
        getServer().getPluginManager().registerEvents(new RaidLootListener(this), this);
        getServer().getPluginManager().registerEvents(new ClaimBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new CashTokenListener(this), this);

        // Commands — economy
        bindCommand("balance", new BalanceCommand(this));
        bindTabCommand("pay", new PayCommand(this));
        bindCommand("cheque", new ChequeCommand(this));

        // Commands — jobs
        bindTabCommand("job", new JobCommand(this));
        bindTabCommand("advert", new AdvertCommand(this));
        bindTabCommand("vote", new VoteCommand(this));
        bindTabCommand("demote", new DemoteCommand(this));

        // Commands — law enforcement
        bindTabCommand("wanted", new WantedCommand(this));
        bindTabCommand("warrant", new WarrantCommand(this));
        bindCommand("arrest", new ArrestCommand(this, false));
        bindCommand("unarrest", new ArrestCommand(this, true));
        bindCommand("setjail", new SetJailCommand(this));
        bindCommand("lockdown", new LockdownCommand(this));
        bindTabCommand("license", new GunLicenseCommand(this));

        // Commands — social
        bindCommand("g", new GroupChatCommand(this));
        bindCommand("ooc", new OocCommand(this));
        bindCommand("agenda", new AgendaCommand(this));

        // Commands — scoreboard/stats
        bindTabCommand("stats", new StatsCommand(this));
        bindCommand("sidebar", new ScoreboardCommand(this));

        // Commands — party
        bindTabCommand("party", new PartyCommand(this));
        bindCommand("p", new PartyChatCommand(this));

        // Scheduled tasks
        SalaryTask salary = new SalaryTask(jobRegistry, cashManager);
        this.salaryTask = salary.runTaskTimer(this, 20L * 60, 20L * 60);
        lawEnforcement.arrestManager().startReleaseChecker();
        sidebarManager.startUpdating();

        getLogger().info("2high2try-core enabled (Phases 1-15 + party system).");
    }

    @Override
    public void onDisable() {
        org.bukkit.event.HandlerList.unregisterAll(this);

        if (salaryTask != null) {
            salaryTask.cancel();
            salaryTask = null;
        }
        if (sidebarManager != null) {
            sidebarManager.shutdown();
            sidebarManager = null;
        }
        if (printerManager != null) {
            printerManager.shutdown();
            printerManager = null;
        }
        if (raidManager != null) {
            raidManager.shutdown();
            raidManager = null;
        }
        if (lawEnforcement != null) {
            lawEnforcement.lockdownManager().shutdown();
            lawEnforcement = null;
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
        entityRegistry = null;
        socialService = null;
        statsTracker = null;
        chequeManager = null;
        partyManager = null;
        config = null;
        getLogger().info("2high2try-core disabled.");
    }

    private LawEnforcementService makeLawEnforcement() {
        WantedManager wantedManager = new WantedManager(storage);
        WarrantManager warrantManager = new WarrantManager(config);
        JailManager jailManager = new JailManager(storage, getLogger());
        ArrestManager arrestManager = new ArrestManager(
                storage, config, jailManager, wantedManager, jobRegistry, this);
        LockdownManager lockdownManager = new LockdownManager(config, jobRegistry, this);
        LicenseManager licenseManager = new LicenseManager(storage);

        wantedManager.loadFromDatabase();
        arrestManager.loadFromDatabase();
        licenseManager.loadFromDatabase();

        return new LawEnforcementService(
                arrestManager, warrantManager, wantedManager, lockdownManager, licenseManager);
    }

    private SocialService makeSocialService() {
        GroupChatManager groupChat = new GroupChatManager(jobRegistry);
        ChatManager chat = new ChatManager(jobRegistry, config);
        AgendaManager agenda = new AgendaManager();
        VoteManager vote = new VoteManager(jobRegistry, this);
        DemoteManager demote = new DemoteManager(jobRegistry, this);
        return new SocialService(groupChat, chat, agenda, vote, demote);
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

    private <T extends org.bukkit.command.CommandExecutor & org.bukkit.command.TabCompleter>
    void bindTabCommand(String name, T handler) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
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
    @Override public LawEnforcementApi law() { return lawEnforcement; }
    @Override public EntityRegistryApi entities() { return entityRegistry; }
    @Override public SocialApi social() { return socialService; }
    @Override public ScoreboardApi scoreboard() { return sidebarManager; }
    @Override public ChequeApi cheques() { return chequeManager; }
    @Override public PartyApi party() { return partyManager; }

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
    public LawEnforcementService lawEnforcement() { return lawEnforcement; }
    public EntityRegistryImpl entityRegistry() { return entityRegistry; }
    public SocialService socialService() { return socialService; }
    public SidebarManager sidebarManager() { return sidebarManager; }
    public PlayerStatsTracker statsTracker() { return statsTracker; }
    public ChequeManager chequeManager() { return chequeManager; }
    public PartyManager partyManager() { return partyManager; }
}
