package com.twohigh.core;

import com.twohigh.api.DarkRPApi;
import com.twohigh.api.claim.ClaimApi;
import com.twohigh.api.detection.DetectionApi;
import com.twohigh.api.economy.EconomyApi;
import com.twohigh.api.job.JobRegistry;
import com.twohigh.api.pvp.CombatTagApi;
import com.twohigh.api.pvp.PvPApi;
import com.twohigh.core.command.BalanceCommand;
import com.twohigh.core.command.PayCommand;
import com.twohigh.core.config.CoreConfig;
import com.twohigh.core.data.CoreStorage;
import com.twohigh.core.data.mysql.MysqlStorage;
import com.twohigh.core.economy.CashManager;
import com.twohigh.core.economy.DualEconomyService;
import com.twohigh.core.integration.NoopVaultHook;
import com.twohigh.core.integration.VaultHook;
import com.twohigh.core.integration.VaultHookImpl;
import com.twohigh.core.listener.DeathListener;
import com.twohigh.core.listener.JoinQuitListener;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.logging.Level;

public final class TwoHigh2TryCore extends JavaPlugin implements DarkRPApi {

    private CoreConfig config;
    private CoreStorage storage;
    private CashManager cashManager;
    private VaultHook vaultHook;
    private DualEconomyService economyService;

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

            DarkRPApi.setInstance(this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Startup failed — disabling 2high2try-core.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);

        bindCommand("balance", new BalanceCommand(this));
        PayCommand payCmd = new PayCommand(this);
        PluginCommand pay = getCommand("pay");
        if (pay != null) {
            pay.setExecutor(payCmd);
            pay.setTabCompleter(payCmd);
        }

        getLogger().info("2high2try-core enabled (Phase 1+2: economy foundation).");
    }

    @Override
    public void onDisable() {
        org.bukkit.event.HandlerList.unregisterAll(this);
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

    private void bindCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
        } else {
            getLogger().warning("Command '" + name + "' missing from plugin.yml.");
        }
    }

    // --- DarkRPApi implementation (stubs for future phases) ---

    @Override public EconomyApi economy() { return economyService; }
    @Override public JobRegistry jobs() { throw new UnsupportedOperationException("Phase 3"); }
    @Override public DetectionApi detection() { throw new UnsupportedOperationException("Phase 7"); }
    @Override public ClaimApi claims() { throw new UnsupportedOperationException("Phase 4"); }
    @Override public PvPApi pvp() { throw new UnsupportedOperationException("Phase 3"); }
    @Override public CombatTagApi combatTag() { throw new UnsupportedOperationException("Phase 3"); }

    // --- Internal accessors ---

    public CoreConfig coreConfig() { return config; }
    public CashManager cashManager() { return cashManager; }
    public DualEconomyService economyService() { return economyService; }
    public VaultHook vaultHook() { return vaultHook; }
}
