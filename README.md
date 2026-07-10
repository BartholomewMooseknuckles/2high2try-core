# 2high2try-core

DarkRP game mode framework for Paper 1.21.4. Provides the core infrastructure for a cops-and-criminals economy server — dual-balance economy, jobs with teams/slots/prerequisites, law enforcement (arrest, jail, warrants, lockdown), raids with legitimacy tracking and destruction-scaled cooldowns, mugging with PvP windows, PvP rules, claims, drug dog detection, money printers with MySQL persistence, party system with shared bank, group chat, voting, demoting, scoreboard HUD, cheques, and a full addon API so third-party plugins can register their own jobs, entities, and hook into every system.

## Features

### Dual-Balance Economy
- **Cash on hand** -- custom MySQL-backed balance. Lootable on death (drops as a physical emerald item), muggable, earned from selling goods and money printers.
- **Bank balance** -- Vault-backed, sacred, never lost. Deposit at Bank NPCs, withdraw at Bank NPCs or ATMs.
- Commands: `/balance` (aliases: `/bal`, `/money`), `/pay <player> <amount>`

### Cheque System
- `/cheque <amount>` -- withdraws cash and creates a physical paper item with the amount stored in PDC.
- Right-click a cheque to redeem it and add the cash to your balance.
- Cheques are physical items -- they can be traded, dropped, stolen during raids, or mugged.

### Job System
- **7 built-in default jobs**: Citizen, Mayor, Police Officer, Police Chief, Gangster, Thief, Hitman.
- **Teams**: civilian, government, police, criminal -- used for group chat routing, demote groups, and item kits.
- **Slot limits**: Jobs can cap the number of players (e.g., 1 Mayor, 4 Police Officers).
- **Vote-required jobs**: Mayor requires a server-wide election via `/vote`.
- **Prerequisite jobs**: Police Chief requires being a Police Officer first.
- **Demote groups**: Getting demoted from one job bans you from all jobs sharing the same demoteGroup for the session.
- **Salary**: Configurable per-job salary paid automatically on a timer.
- Commands: `/job list`, `/job join <id>`, `/job quit`
- Addon plugins register custom jobs via the API with full control over all fields.

### Law Enforcement
- **Wanted system**: `/wanted <player> <reason>` marks a player as wanted. Persists across restarts (MySQL). `/wanted` lists all wanted players. `/wanted remove <player>` clears wanted status.
- **Warrants**: `/warrant <player> <reason>` issues a search warrant (in-memory, auto-expires after configurable time, default 5min). Enables door ram usage on the target's containers.
- **Arrest**: `/arrest <player>` arrests a wanted player, teleports them to a random jail position, starts a jail timer (default 120s). `/unarrest <player>` releases early.
- **Jail**: `/setjail <name>` sets jail positions (admin). Jailed players can't move more than 10 blocks from jail. Respawn in jail on death. Sidebar shows remaining time.
- **Lockdown**: `/lockdown` toggles city lockdown (Mayor/Police Chief/admin only). Applies Slowness to all non-police/government players.
- **Gun licenses**: `/license` checks your license status. `/license grant <player>` and `/license revoke <player>` (police only). Persists in MySQL.
- All law commands are job-gated at runtime (checked via team field, not permissions).

### Law Enforcement Items
- **Arrest Stick** (Blaze Rod) -- right-click a wanted player to arrest and teleport them to jail.
- **Unarrest Stick** (Breeze Rod) -- right-click a jailed player to release them early.
- **Stun Stick** (Blaze Rod) -- melee hit applies Slowness V + Weakness for 3 seconds.
- **Door Ram** (Iron Axe) -- right-click a container in a claim while a warrant is active to force it open.
- **Weapon Checker** (Compass) -- right-click a player to scan their inventory for contraband.
- **Lockpick** (Tripwire Hook) -- criminal team only. Timed interaction on containers with action bar progress bar (Thief: 10s, Gangster: 15s). Cancels on movement.
- **Kit system**: Changing jobs automatically strips old law items and gives the appropriate kit based on team.

### Entity/Block Registry
- Addon plugins register block-based entities (like DarkRP's `createEntity`) with price, max-per-player, allowed jobs, and detection signal strength.
- `EntityRegistryApi` -- place, remove, query entities at locations or by owner.
- Fires `EntityPlaceEvent` and `EntityBreakEvent` for addons to hook into.
- Owner check on break -- only the owner or admins can remove placed entities.

### Social Systems
- **Group Chat**: `/g <message>` sends a message to all online players on your job team. `/g` with no args toggles group chat mode (all messages auto-route to team). Color-coded by team (police=blue, government=gold, criminal=red, civilian=gray).
- **Agenda**: `/agenda` shows your team's agenda. `/agenda set <message>` sets it. `/agenda clear` clears it. Session-scoped.
- **Voting**: `/vote start <job_id>` starts a 60-second server-wide election for vote-required jobs. `/vote yes` or `/vote no` to cast. Majority wins.
- **Demoting**: `/demote <player>` starts a 30-second team vote to demote a player. `/demote yes` or `/demote no` to cast. Demoted player falls back to Citizen and gets banned from all jobs in their demote group for the session.

### Scoreboard / HUD / Stats
- **Sidebar**: Shows job name, cash, K/D ratio, and status (JAILED timer or LOCKDOWN ACTIVE). Auto-refreshes every 5 seconds. Auto-enabled on join.
- **Toggle**: `/sidebar` to show, `/sidebar off` to hide. Alias: `/hud`.
- **Stats**: `/stats [player]` shows kill/death stats and K/D ratio. In-memory tracking.

### PvP & Combat Tagging
- Dimension-based PvP rules -- configurable per overworld/nether/end.
- Combat tag applied on PvP hit. Combat-logging (disconnecting while tagged) kills the player.
- Tag duration configurable (default 60s).

### Claims (WorldGuard)
- WorldGuard-backed claim regions for DarkRP mechanics (coexists with Towny for general server use).
- Claims track block count, decay state, and raid status.
- Abstracted behind `ClaimApi` so a future custom claim plugin can replace WorldGuard without touching addons.

### Raids
- `/advert raid` near an enemy claim -- costs configurable cash, starts the raid state machine.
- **ADVERTISED** -> coordinates hidden, countdown timer broadcasts to server.
- **ACTIVE** -> coordinates revealed, PvP bubble enabled, boss bar shows destruction % and remaining time.
- **ENDED** -> destruction-scaled cooldowns: `floor + destruction% * minutesPerPercent`, clamped to max.
- **Legitimacy**: Raids must involve looting containers or PvP damage to count as legit. Illegitimate raids get no cooldown.
- **Walk-in detection**: Non-involved players in the bubble get prompted to join as attacker or defender.
- **Abandon timer**: If all attackers leave the bubble, a grace period (30s default) starts before auto-ending.
- **Party auto-join**: Party members entering a raid bubble auto-join the same side as their teammate.
- `/advert defend` to join as a defender, `/advert counter` to join as a counter-raider.

### Mugging
- `/advert mug <player> <amount>` -- mug a nearby player. Victim can comply (pay) or fight back.
- **PvP window**: A targeted PvP exception opens between mugger and victim for the duration (30s default).
- **Distance check**: Must be within 6 blocks (configurable) of the target.
- Amount capped (default $10,000), cooldown between mugs (default 20min).
- Paying a mug demand via `/pay` auto-resolves the mug session.

### Party System
- `/party create` -- create a new party.
- `/party invite <player>` -- invite a player (leader/officer only). Invites expire after 60s.
- `/party accept` -- accept a pending invite.
- `/party leave` -- leave the party. Leadership auto-transfers if the leader leaves.
- `/party kick <player>` -- kick a member (leader/officer only).
- `/party disband` -- disband the party (leader only). Bank balance is split evenly among all members.
- `/party list` -- show members with roles, online status, bank balance, and FF status.
- `/party ff` -- toggle friendly fire (leader only, when server allows `party_choice` mode).
- `/party role <player> <leader|officer|member>` -- set a member's role (leader only).
- `/party deposit <amount>` -- deposit cash into the shared party bank.
- `/party withdraw <amount>` -- withdraw from the party bank (leader/officer only).
- `/party bank` -- check party bank balance.
- `/p <message>` -- party chat.
- **Friendly fire modes**: `always_on`, `always_off`, or `party_choice` (leader toggles).
- **Raid auto-join**: Party members entering a raid bubble auto-join the same side as their party member.
- **PvP protection**: Party members with FF off can't damage each other, even in PvP zones.

### F4 Menu
- `/f4` (alias `/menu`) opens the game mode's central hub as a chest GUI — works on every client, including Bedrock via Geyser later.
- **Main menu**: your wallet (cash + job), Jobs, Shop, Leaderboard, Party tabs, plus any addon-registered tabs.
- **Jobs tab**: browse every registered job with team, salary, slots, and requirements in the tooltip — click to join (same checks as `/job join`), barrier item to quit.
- **Shop tab**: buy money printers (price configurable via `economy.printer_price`) and any addon-registered entities. Job restrictions and cash checks enforced at purchase and at placement.
- **Leaderboard tab**: top 10 cash holders.
- **Party tab**: create/leave your party, see members and bank, leader can toggle friendly fire.
- Addons register their own tabs via `F4MenuApi` (see addon guide below).
- The native Paper Dialog version (ESC-menu integration) is planned for the 1.21.11 server upgrade — this chest GUI stays as the fallback renderer.

### Cash Token Anti-Stash
- Cash drop items (emeralds with PDC tag) cannot be placed into containers, hoppers, or villager trades.
- Prevents players from hiding cash in chests to avoid losing it on death/mug.

### Detection & Drug Dog
- Addon plugins register signal sources (e.g., weed plants) at locations with a strength value.
- Nearby signals merge within a configurable radius.
- **Drug Dog item** (bone with PDC) -- right-click to sniff. Uses inverse-square-law to point toward the strongest signal with compass direction and tier display (FAINT -> INTERESTED -> ALERT -> FERAL).
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
- **Vault** + an economy provider -- enables bank balance. Without it, only cash-on-hand works.
- **WorldGuard** -- enables claims and raid bubbles. Without it, claim features are disabled.
- **Citizens** -- for Bank NPC and dealer NPC traits (future).
- **DiscordSRV** -- for raid/event announcements to Discord (future).
- **CoreProtect** -- for audit log integration (future).
- **FancyHolograms** -- for hologram leaderboards (future).

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
  printer_price: 5000

raids:
  duration_seconds: 1800
  abandon_grace_seconds: 30
  cooldown_floor_minutes: 5
  cooldown_minutes_per_percent: 1
  cooldown_max_minutes: 120

mugging:
  window_seconds: 30
  max_distance: 6

law:
  jail_time_seconds: 120
  warrant_expire_seconds: 300
  lockdown_slowness_level: 1

party:
  max_size_default: 6
  friendly_fire_mode: party_choice  # always_on, always_off, party_choice
  persistence: false

defaults:
  salary_interval_minutes: 30
  disabled_jobs: []
```

## All Commands

| Command | Description | Who can use |
|---------|-------------|-------------|
| `/balance` | Show cash + bank balance | Everyone |
| `/pay <player> <amount>` | Pay another player | Everyone |
| `/cheque <amount>` | Write a cheque from cash | Everyone |
| `/job list` | List available jobs with slots | Everyone |
| `/job join <id>` | Join a job | Everyone |
| `/job quit` | Quit job (return to Citizen) | Everyone |
| `/advert raid` | Start a raid on a nearby claim | Everyone |
| `/advert defend` | Join as a defender | Everyone |
| `/g [message]` | Team group chat (or toggle mode) | Everyone |
| `/agenda [set\|clear]` | View/set/clear team agenda | Everyone |
| `/vote start <job>` | Start election for vote-required job | Everyone |
| `/vote yes\|no` | Cast vote in active election | Everyone |
| `/demote <player>` | Start demote vote against teammate | Everyone |
| `/demote yes\|no` | Cast vote in active demote | Everyone |
| `/advert counter` | Join active raid as counter-raider | Everyone |
| `/advert mug <player> <amount>` | Mug a nearby player | Everyone |
| `/stats [player]` | View kill/death stats | Everyone |
| `/sidebar [off]` | Toggle scoreboard sidebar | Everyone |
| `/party <sub>` | Party management (create/invite/kick/etc.) | Everyone |
| `/p <message>` | Party chat | Everyone |
| `/f4` | Open the game mode menu (alias: `/menu`) | Everyone |
| `/wanted [player] [reason]` | Mark wanted / list wanted | Police team |
| `/wanted remove <player>` | Remove wanted status | Police team |
| `/warrant <player> [reason]` | Issue a search warrant | Police team |
| `/warrant revoke <player>` | Revoke a warrant | Police team |
| `/arrest <player>` | Arrest a wanted player | Police team |
| `/unarrest <player>` | Release a jailed player early | Police team |
| `/lockdown` | Toggle city lockdown | Mayor / Chief / Admin |
| `/license` | Check your gun license | Everyone |
| `/license grant\|revoke <player>` | Grant/revoke gun license | Police team |
| `/setjail <name>` | Set a jail position | Admin (`twohigh.admin`) |

## Default Jobs

| Job | ID | Team | Slots | Salary | Vote? | Prerequisite |
|-----|----|------|-------|--------|-------|-------------|
| Citizen | `citizen` | civilian | unlimited | $50/30min | No | -- |
| Mayor | `mayor` | government | 1 | $300/30min | Yes | -- |
| Police Officer | `police` | police | 4 | $200/30min | No | -- |
| Police Chief | `chief` | police | 1 | $350/30min | No | `police` |
| Gangster | `gangster` | criminal | 3 | $0 | No | -- |
| Thief | `thief` | criminal | 2 | $0 | No | -- |
| Hitman | `hitman` | criminal | 1 | $0 | No | -- |

All default jobs can be disabled individually in config via `defaults.disabled_jobs`.

## Writing Addon Plugins

The core provides a thin API jar (`2high2try-api`) that addon plugins compile against. Your addon registers jobs, entities, and hooks into the economy, detection, law enforcement, social, and claim systems without depending on core internals.

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
        "myjob.meth_cook",     // permission (or null)
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

### Register a Block Entity

```java
import com.twohigh.api.DarkRPApi;
import com.twohigh.api.entity.EntityDefinition;
import org.bukkit.Material;
import java.util.Set;

DarkRPApi.get().entities().registerEntity(new EntityDefinition(
    "drug_lab",             // unique id
    "Drug Lab",             // display name
    Material.SMOKER,        // block material
    5000.0,                 // purchase price
    2,                      // max per player
    Set.of("meth_cook"),    // allowed jobs
    this,                   // owning plugin
    3.0                     // detection signal strength
));
```

### Law Enforcement API

```java
import com.twohigh.api.DarkRPApi;

// Check if player is wanted
boolean wanted = DarkRPApi.get().law().wanted().isWanted(playerUUID);

// Check if player is jailed
boolean jailed = DarkRPApi.get().law().arrests().isJailed(playerUUID);

// Check lockdown
boolean lockdown = DarkRPApi.get().law().lockdown().isActive();

// Check gun license
boolean licensed = DarkRPApi.get().law().licenses().hasLicense(playerUUID);
```

### Cheque API

```java
import com.twohigh.api.DarkRPApi;

// Create a cheque item
ItemStack cheque = DarkRPApi.get().cheques().createCheque(writerUUID, 5000.0);

// Check if an item is a cheque
boolean isCheque = DarkRPApi.get().cheques().isCheque(itemStack);

// Redeem
DarkRPApi.get().cheques().redeem(player, itemStack);
```

### Register an F4 Menu Tab

```java
import com.twohigh.api.DarkRPApi;
import com.twohigh.api.menu.F4Tab;
import org.bukkit.Material;
import java.util.List;

DarkRPApi.get().menu().registerTab(new F4Tab(
    "weed_shop",                       // unique id
    "Weed Shop",                       // display name on the main menu
    Material.SHORT_GRASS,              // icon
    List.of("§7Buy seeds and gear."),  // tooltip lore
    player -> openMyShopGui(player),   // click handler
    this                               // owning plugin
));

// in onDisable:
DarkRPApi.get().menu().unregisterTab("weed_shop");
```

### Party API

```java
import com.twohigh.api.DarkRPApi;

// Check if two players are in the same party
boolean sameParty = DarkRPApi.get().party().areInSameParty(playerA, playerB);

// Check friendly fire status
DarkRPApi.get().party().getPartyId(player).ifPresent(partyId -> {
    boolean ff = DarkRPApi.get().party().isFriendlyFireEnabled(partyId);
    double bank = DarkRPApi.get().party().getPartyBankBalance(partyId);
});
```

### Check Raid Status

```java
// Suppress protections during active raids
if (DarkRPApi.get().claims().isRaidActive(location)) {
    // raid is happening here -- allow PvP, skip harvest protection, etc.
}
```

## MySQL Tables

The plugin auto-creates and migrates its tables:

| Table | Purpose |
|-------|---------|
| `player_cash` | Cash-on-hand balances |
| `player_jobs` | Player job assignments |
| `wanted_players` | Currently wanted players (name, reason, officer) |
| `jail_positions` | Admin-set jail locations |
| `active_arrests` | Currently jailed players with release time |
| `gun_licenses` | Players with gun licenses |
| `money_printers` | Placed printer locations, owner, accumulated cash |
| `party_banks` | Party shared bank balances |

## Project Structure

```
2high2try/
  api/          -> 2high2try-api (thin jar for addon developers)
    com.twohigh.api.
      DarkRPApi, economy/, job/, detection/, claim/, pvp/, event/,
      law/, entity/, social/, scoreboard/, cheque/, party/, menu/
  plugin/       -> 2high2try-core (main plugin, shadow jar)
    com.twohigh.core.
      TwoHigh2TryCore (main class, implements DarkRPApi)
      economy/      CashManager, DualEconomyService
      job/          JobRegistryImpl, SalaryTask
      defaults/     DefaultJobs
      pvp/          PvPManager, CombatTagManager
      claim/        ClaimManagerImpl
      raid/         RaidManager, ActiveRaid, RaidState
      mug/          MugManager, MugSession
      detection/    DetectionManagerImpl, DrugDogItem
      printer/      MoneyPrinterManager
      leaderboard/  LeaderboardManager
      audit/        AuditService
      law/          ArrestManager, WarrantManager, WantedManager,
                    JailManager, LockdownManager, LicenseManager,
                    LawEnforcementService
      law/item/     LawItems, LawItemListener, LockpickTask
      entity/       EntityRegistryImpl, EntityListener
      social/       GroupChatManager, AgendaManager, VoteManager,
                    DemoteManager, SocialService
      scoreboard/   SidebarManager, PlayerStatsTracker, StatsCommand,
                    ScoreboardCommand
      cheque/       ChequeManager, ChequeListener, ChequeCommand
      party/        PartyManager, Party, PartyRole, PartyCommand,
                    PartyChatCommand
      menu/         F4MenuManager, F4MenuHolder, F4MenuListener, F4Command
      integration/  VaultHook, WorldGuardHook (+ Noop + Impl)
      command/      BalanceCommand, PayCommand, JobCommand, AdvertCommand,
                    WantedCommand, WarrantCommand, ArrestCommand,
                    SetJailCommand, LockdownCommand, GunLicenseCommand
      listener/     DeathListener, JoinQuitListener, PvPListener,
                    DrugDogListener, PrinterListener, LawListener,
                    CashTokenListener, ClaimBlockListener
      raid/         (also) RaidAccessListener, RaidLootListener
      data/         CoreStorage, MysqlStorage, MysqlMigrations
      config/       CoreConfig
```

## License

Private repository. Contact BartholomewMooseknuckles for access.
