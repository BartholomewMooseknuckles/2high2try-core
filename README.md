# 2high2try-core

DarkRP game mode framework for Paper 1.21.4. Provides the core infrastructure for a cops-and-criminals economy server — dual-balance economy, raids, mugging, PvP rules, claims, drug dog detection, money printers, and a job addon API so third-party plugins can register their own jobs (weed dealer, meth cook, gun seller, etc.).

## Features

### Dual-Balance Economy
- **Cash on hand** — custom MySQL-backed balance. Lootable on death (drops as a physical emerald item), muggable, earned from selling goods and money printers.
- **Bank balance** — Vault-backed, sacred, never lost. Deposit at Bank NPCs, withdraw at Bank NPCs or ATMs.
- Commands: `/balance` (aliases: `/bal`, `/money`), `/pay <player> <amount>`

### Job System
- Addon plugins register jobs via the API with salary, legal/illegal status, and permissions.
- Players join jobs with `/job join <id>`, view available jobs with `/job list`, quit with `/job quit`.
- Salary is paid automatically on a configurable interval to online players.

### PvP & Combat Tagging
- Dimension-based PvP rules — configurable per overworld/nether/end.
- Combat tag applied on PvP hit. Combat-logging (disconnecting while tagged) kills the player.
- Tag duration configurable (default 60s).

### Claims (WorldGuard)
- WorldGuard-backed claim regions for DarkRP mechanics (coexists with Towny for general server use).
- Claims track block count, decay state, and raid status.
- Abstracted behind `ClaimApi` so a future custom claim plugin can replace WorldGuard without touching addons.

### Raids
- `/advert raid` near an enemy claim — costs configurable cash, starts the raid state machine.
- **ADVERTISED** → coordinates hidden, countdown timer broadcasts to server.
- **ACTIVE** → coordinates revealed, PvP bubble enabled, boss bar shown to players in range.
- **ENDED** → cooldowns applied (per-base 30min default, per-raider-per-base 24hr default).
- `/advert defend` to join as a defender.

### Mugging
- Right-click or command-based mugging prompt. Victim can comply (pay) or fight back.
- Amount capped (default $10,000), cooldown between mugs (default 20min).
- PvP must be enabled in the area to mug.

### Detection & Drug Dog
- Addon plugins register signal sources (e.g., weed plants) at locations with a strength value.
- Nearby signals merge within a configurable radius.
- **Drug Dog item** (bone with PDC) — right-click to sniff. Uses inverse-square-law to point toward the strongest signal with compass direction and tier display (FAINT → INTERESTED → ALERT → FERAL).
- Treat cooldown prevents spam (default 60s).

### Money Printers
- Place an emerald block (printer item) to start generating cash passively.
- Right-click to collect accumulated earnings.
- Yield configurable (default $500/hour).

### Leaderboard & Audit
- Cash leaderboard rankings.
- Audit log tracks economy transactions, raids, and mugging events.

## Building

Requires **Java 21** and an internet connection (Gradle downloads dependencies automatically).

```bash
# Windows
.\gradlew.bat shadowJar

# Linux/Mac
./gradlew shadowJar
```

Output: `plugin/build/libs/2high2try-core-0.1.0-SNAPSHOT.jar`

Drop the jar into your Paper 1.21.4 server's `plugins/` folder.

## Dependencies

**Required:**
- Paper 1.21.4+
- MySQL database

**Soft-dependencies (optional, features degrade gracefully):**
- **Vault** + an economy provider — enables bank balance. Without it, only cash-on-hand works.
- **WorldGuard** — enables claims and raid bubbles. Without it, claim features are disabled.
- **Citizens** — for Bank NPC and dealer NPC traits (future).
- **DiscordSRV** — for raid/event announcements to Discord (future).
- **CoreProtect** — for audit log integration (future).
- **FancyHolograms** — for hologram leaderboards (future).

## Configuration

`plugins/2high2try-core/config.yml` is generated on first run. Key sections:

```yaml
mysql:
  host: localhost
  port: 3306
  database: twohigh2try
  username: root
  password: ""

pvp:
  overworld_enabled: false
  nether_enabled: true
  end_enabled: true
  combat_tag_seconds: 60

raids:
  advert_cost: 5000
  coord_publish_delay_seconds: 60
  bubble_radius: 32

mugging:
  cap: 10000
  cooldown_minutes: 20

economy:
  printer_yield_per_hour: 500
```

## Writing Addon Plugins

The core provides a thin API jar (`2high2try-api`) that addon plugins compile against. Your addon registers jobs and hooks into the economy, detection, and claim systems without depending on core internals.

### Setup (Gradle)

```kotlin
repositories {
    // point to wherever you host the api jar, or use a local maven repo
}

dependencies {
    compileOnly("com.twohigh:2high2try-api:0.1.0-SNAPSHOT")
}
```

Add to your `plugin.yml`:
```yaml
depend: [2high2try-core]
```

### Register a Job

```java
import com.twohigh.api.DarkRPApi;
import com.twohigh.api.job.JobDefinition;

@Override
public void onEnable() {
    DarkRPApi.get().jobs().registerJob(new JobDefinition(
        "meth_cook",           // unique id
        "Meth Cook",           // display name
        false,                 // legal? (false = illegal)
        250.0,                 // salary per interval
        30 * 60 * 1000L,       // salary interval (30 min)
        "myjob.meth_cook",    // permission (or null)
        this                   // owning plugin
    ));
}

@Override
public void onDisable() {
    DarkRPApi.get().jobs().unregisterJob("meth_cook");
}
```

### Use the Economy

```java
import com.twohigh.api.DarkRPApi;

// Pay cash to a player (lootable on death)
DarkRPApi.get().economy().depositCash(player, 500.0);

// Check cash balance
double cash = DarkRPApi.get().economy().getCash(player);

// Charge for a purchase
if (DarkRPApi.get().economy().hasCash(player, 100.0)) {
    DarkRPApi.get().economy().withdrawCash(player, 100.0);
}
```

### Register Detection Signals

```java
import com.twohigh.api.DarkRPApi;
import com.twohigh.api.detection.SignalHandle;

// Register a signal source (e.g., a grow operation)
SignalHandle handle = DarkRPApi.get().detection()
    .registerSignalSource(location, 5.0, "meth_lab");

// Update strength as the operation grows
DarkRPApi.get().detection().updateSignalStrength(handle, 10.0);

// Remove when destroyed
DarkRPApi.get().detection().removeSignalSource(handle);
```

### Check Raid Status

```java
// Suppress protections during active raids
if (DarkRPApi.get().claims().isRaidActive(location)) {
    // raid is happening here — allow PvP, skip harvest protection, etc.
}
```

## Project Structure

```
2high2try/
  api/          → 2high2try-api (thin jar for addon developers)
    com.twohigh.api.
      DarkRPApi, economy/, job/, detection/, claim/, pvp/, event/
  plugin/       → 2high2try-core (main plugin, shadow jar)
    com.twohigh.core.
      TwoHigh2TryCore (main class, implements DarkRPApi)
      economy/     CashManager, DualEconomyService
      job/         JobRegistryImpl, SalaryTask
      pvp/         PvPManager, CombatTagManager
      claim/       ClaimManagerImpl
      raid/        RaidManager, ActiveRaid, RaidState
      mug/         MugManager, MugSession
      detection/   DetectionManagerImpl, DrugDogItem
      printer/     MoneyPrinterManager
      leaderboard/ LeaderboardManager
      audit/       AuditService
      integration/ VaultHook, WorldGuardHook (+ Noop + Impl)
      command/     BalanceCommand, PayCommand, JobCommand, AdvertCommand
      listener/    DeathListener, JoinQuitListener, PvPListener, DrugDogListener, PrinterListener
      data/        CoreStorage, MysqlStorage, MysqlMigrations
      config/      CoreConfig
```

## License

Private repository. Contact BartholomewMooseknuckles for access.
