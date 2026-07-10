package com.twohigh.core.menu;

import com.twohigh.api.entity.EntityDefinition;
import com.twohigh.api.job.JobDefinition;
import com.twohigh.api.menu.F4MenuApi;
import com.twohigh.api.menu.F4Tab;
import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.entity.EntityItems;
import com.twohigh.core.leaderboard.LeaderboardManager;
import com.twohigh.core.party.Party;
import com.twohigh.core.party.PartyRole;
import com.twohigh.core.printer.MoneyPrinterManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * The F4 menu, rendered as a chest GUI. Works on 1.21.4 and doubles as the
 * fallback renderer once the native Dialog version arrives (1.21.7+ servers).
 */
public final class F4MenuManager implements F4MenuApi {

    private static final int[] ADDON_SLOTS = {19, 20, 21, 22, 23, 24, 25};

    private final TwoHigh2TryCore plugin;
    private final Map<String, F4Tab> addonTabs = new LinkedHashMap<>();

    public F4MenuManager(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    // --- F4MenuApi ---

    @Override
    public void open(Player player) {
        openMain(player);
    }

    @Override
    public void registerTab(F4Tab tab) {
        if (tab == null || tab.id() == null) return;
        if (addonTabs.putIfAbsent(tab.id(), tab) != null) {
            plugin.getLogger().warning("F4 tab '" + tab.id() + "' already registered — ignoring.");
            return;
        }
        plugin.getLogger().info("Registered F4 tab: " + tab.id()
                + " from " + tab.owningPlugin().getName());
    }

    @Override
    public void unregisterTab(String tabId) {
        addonTabs.remove(tabId);
    }

    // --- Main menu ---

    private void openMain(Player player) {
        F4MenuHolder holder = new F4MenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, text("§8☰ 2high2try Menu"));
        holder.setInventory(inv);

        UUID uuid = player.getUniqueId();
        String jobName = plugin.jobRegistry().getPlayerJob(uuid)
                .flatMap(id -> plugin.jobRegistry().getJob(id))
                .map(JobDefinition::displayName)
                .orElse("Citizen");
        inv.setItem(4, icon(Material.GOLD_INGOT, "§6Your Wallet", List.of(
                "§7Cash: §a$" + String.format("%.2f", plugin.cashManager().getCash(uuid)),
                "§7Job: §f" + jobName)));

        set(inv, holder, 10, icon(Material.NAME_TAG, "§eJobs", List.of(
                "§7Browse and join jobs.")), p -> openLater(p, this::openJobs));
        set(inv, holder, 12, icon(Material.CHEST, "§eShop", List.of(
                "§7Buy printers and gear.")), p -> openLater(p, this::openShop));
        set(inv, holder, 14, icon(Material.GOLD_BLOCK, "§eLeaderboard", List.of(
                "§7Top cash holders.")), p -> openLater(p, this::openLeaderboard));
        set(inv, holder, 16, icon(Material.WHITE_BANNER, "§eParty", List.of(
                "§7Manage your party.")), p -> openLater(p, this::openParty));

        int slotIdx = 0;
        for (F4Tab tab : addonTabs.values()) {
            if (slotIdx >= ADDON_SLOTS.length) break;
            List<String> lore = tab.lore() != null ? tab.lore() : List.of();
            set(inv, holder, ADDON_SLOTS[slotIdx++],
                    icon(tab.icon(), "§d" + tab.displayName(), lore),
                    p -> {
                        p.closeInventory();
                        tab.onClick().accept(p);
                    });
        }

        set(inv, holder, 26, icon(Material.BARRIER, "§cClose", List.of()),
                Player::closeInventory);

        player.openInventory(inv);
    }

    // --- Jobs ---

    private void openJobs(Player player) {
        F4MenuHolder holder = new F4MenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, text("§8Jobs"));
        holder.setInventory(inv);

        String currentJob = plugin.jobRegistry()
                .getPlayerJob(player.getUniqueId()).orElse(null);

        List<JobDefinition> jobs = new ArrayList<>(plugin.jobRegistry().allJobs());
        jobs.sort(Comparator.comparing(JobDefinition::team)
                .thenComparing(JobDefinition::displayName));

        int slot = 9;
        for (JobDefinition job : jobs) {
            if (slot >= 45) break;

            List<String> lore = new ArrayList<>();
            lore.add("§7Team: §f" + job.team());
            lore.add("§7Type: " + (job.legal() ? "§aLegal" : "§cIllegal"));
            if (job.salary() > 0) {
                lore.add("§7Salary: §a$" + String.format("%.0f", job.salary())
                        + " §7/ " + (job.salaryIntervalMs() / 60_000) + "m");
            }
            if (job.hasSlotLimit()) {
                int taken = plugin.jobRegistry().getPlayersInJob(job.id());
                lore.add("§7Slots: §f" + taken + "/" + job.maxSlots());
            }
            if (job.voteRequired()) {
                lore.add("§cRequires election (/vote start " + job.id() + ")");
            }
            if (job.prerequisiteJobId() != null) {
                String prereqName = plugin.jobRegistry().getJob(job.prerequisiteJobId())
                        .map(JobDefinition::displayName).orElse(job.prerequisiteJobId());
                lore.add("§cRequires: " + prereqName);
            }
            lore.add("");
            boolean isCurrent = job.id().equals(currentJob);
            lore.add(isCurrent ? "§a★ Your current job" : "§eClick to join");

            String jobId = job.id();
            set(inv, holder, slot++, icon(teamIcon(job.team()),
                            colorOf(job) + job.displayName(), lore),
                    p -> {
                        p.closeInventory();
                        p.performCommand("job join " + jobId);
                    });
        }

        set(inv, holder, 45, icon(Material.ARROW, "§7Back", List.of()),
                p -> openLater(p, this::openMain));
        set(inv, holder, 49, icon(Material.BARRIER, "§cQuit Job", List.of(
                        "§7Return to Citizen.")),
                p -> {
                    p.closeInventory();
                    p.performCommand("job quit");
                });

        player.openInventory(inv);
    }

    // --- Shop ---

    private void openShop(Player player) {
        F4MenuHolder holder = new F4MenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, text("§8Shop"));
        holder.setInventory(inv);

        double printerPrice = plugin.coreConfig().printerPrice();
        set(inv, holder, 9, icon(Material.EMERALD_BLOCK, "§aMoney Printer", List.of(
                        "§7Prints §a$" + plugin.coreConfig().printerYieldPerHour() + "§7/hour.",
                        "§7Price: §a$" + String.format("%.0f", printerPrice),
                        "",
                        "§eClick to buy")),
                this::buyPrinter);

        List<EntityDefinition> defs = new ArrayList<>(plugin.entityRegistry().allDefinitions());
        defs.sort(Comparator.comparing(EntityDefinition::id));

        int slot = 10;
        for (EntityDefinition def : defs) {
            if (slot >= 45) break;

            List<String> lore = new ArrayList<>();
            lore.add("§7Price: §a$" + String.format("%.0f", def.price()));
            if (def.maxPerPlayer() > 0) {
                lore.add("§7Max per player: §f" + def.maxPerPlayer());
            }
            if (def.isJobRestricted()) {
                lore.add("§7Jobs: §f" + String.join(", ", def.allowedJobs()));
            }
            lore.add("");
            lore.add("§eClick to buy");

            String defId = def.id();
            set(inv, holder, slot++, icon(def.blockMaterial(),
                            "§b" + def.displayName(), lore),
                    p -> buyEntity(p, defId));
        }

        set(inv, holder, 45, icon(Material.ARROW, "§7Back", List.of()),
                p -> openLater(p, this::openMain));

        player.openInventory(inv);
    }

    private void buyPrinter(Player player) {
        UUID uuid = player.getUniqueId();
        double price = plugin.coreConfig().printerPrice();
        if (!plugin.cashManager().hasCash(uuid, price)) {
            player.sendMessage("§cYou need §a$" + String.format("%.0f", price)
                    + " §cin cash for a money printer.");
            return;
        }
        plugin.cashManager().withdraw(uuid, price);
        give(player, MoneyPrinterManager.createPrinterItem());
        player.sendMessage("§a[SHOP] Bought a money printer for §f$"
                + String.format("%.0f", price) + "§a.");
    }

    private void buyEntity(Player player, String entityId) {
        Optional<EntityDefinition> defOpt = plugin.entityRegistry().getDefinition(entityId);
        if (defOpt.isEmpty()) {
            player.sendMessage("§cThat item is no longer sold.");
            return;
        }
        EntityDefinition def = defOpt.get();
        UUID uuid = player.getUniqueId();

        if (def.isJobRestricted()) {
            String job = plugin.jobRegistry().getPlayerJob(uuid).orElse("");
            if (!def.allowedJobs().contains(job)) {
                player.sendMessage("§cYour job can't buy §e" + def.displayName() + "§c.");
                return;
            }
        }
        if (!plugin.cashManager().hasCash(uuid, def.price())) {
            player.sendMessage("§cYou need §a$" + String.format("%.0f", def.price())
                    + " §cin cash for " + def.displayName() + ".");
            return;
        }
        plugin.cashManager().withdraw(uuid, def.price());
        give(player, EntityItems.create(def));
        player.sendMessage("§a[SHOP] Bought §e" + def.displayName() + " §afor §f$"
                + String.format("%.0f", def.price()) + "§a.");
    }

    // --- Leaderboard ---

    private void openLeaderboard(Player player) {
        F4MenuHolder holder = new F4MenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, text("§8Cash Leaderboard"));
        holder.setInventory(inv);

        List<LeaderboardManager.LeaderboardEntry> top =
                plugin.leaderboardManager().getTopCash(10);

        if (top.isEmpty()) {
            inv.setItem(13, icon(Material.PAPER, "§7No data yet", List.of(
                    "§8Only online players are ranked.")));
        }
        int slot = 8;
        for (int i = 0; i < top.size() && slot < 19; i++) {
            LeaderboardManager.LeaderboardEntry entry = top.get(i);
            Material mat = switch (i) {
                case 0 -> Material.GOLD_BLOCK;
                case 1 -> Material.GOLD_INGOT;
                case 2 -> Material.GOLD_NUGGET;
                default -> Material.PAPER;
            };
            inv.setItem(++slot, icon(mat, "§e#" + (i + 1) + " §f" + entry.name(), List.of(
                    "§a$" + String.format("%.2f", entry.amount()))));
        }

        set(inv, holder, 22, icon(Material.ARROW, "§7Back", List.of()),
                p -> openLater(p, this::openMain));

        player.openInventory(inv);
    }

    // --- Party ---

    private void openParty(Player player) {
        F4MenuHolder holder = new F4MenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, text("§8Party"));
        holder.setInventory(inv);

        Optional<Party> partyOpt = plugin.partyManager().getPlayerParty(player.getUniqueId());
        if (partyOpt.isEmpty()) {
            set(inv, holder, 13, icon(Material.LIME_WOOL, "§aCreate Party", List.of(
                            "§7Start a new party and invite",
                            "§7friends with §e/party invite§7.")),
                    p -> {
                        p.performCommand("party create");
                        openLater(p, this::openParty);
                    });
        } else {
            Party party = partyOpt.get();
            boolean ff = plugin.partyManager().isFriendlyFireEnabled(party.id());

            inv.setItem(4, icon(Material.WHITE_BANNER, "§6Your Party", List.of(
                    "§7Members: §f" + party.size() + "/" + plugin.coreConfig().partyMaxSize(),
                    "§7Bank: §a$" + String.format("%.2f", party.bankBalance()),
                    "§7Friendly fire: " + (ff ? "§cON" : "§aOFF"))));

            List<String> memberLore = new ArrayList<>();
            for (UUID member : party.members()) {
                PartyRole role = party.getRole(member);
                String name = Bukkit.getOfflinePlayer(member).getName();
                memberLore.add("§7" + (role != null ? role.name().toLowerCase() : "member")
                        + ": §f" + (name != null ? name : member.toString().substring(0, 8)));
            }
            inv.setItem(11, icon(Material.BOOK, "§eMembers", memberLore));

            if (party.isLeader(player.getUniqueId())) {
                set(inv, holder, 15, icon(Material.FLINT_AND_STEEL, "§eToggle Friendly Fire",
                                List.of("§7Currently: " + (ff ? "§cON" : "§aOFF"))),
                        p -> {
                            p.performCommand("party ff");
                            openLater(p, this::openParty);
                        });
            }

            set(inv, holder, 22, icon(Material.RED_WOOL, "§cLeave Party", List.of()),
                    p -> {
                        p.closeInventory();
                        p.performCommand("party leave");
                    });
        }

        set(inv, holder, 18, icon(Material.ARROW, "§7Back", List.of()),
                p -> openLater(p, this::openMain));

        player.openInventory(inv);
    }

    // --- Helpers ---

    private void set(Inventory inv, F4MenuHolder holder, int slot,
                     ItemStack item, Consumer<Player> action) {
        inv.setItem(slot, item);
        holder.setAction(slot, action);
    }

    /** Opening a new inventory from inside a click handler must wait a tick. */
    private void openLater(Player player, Consumer<Player> opener) {
        Bukkit.getScheduler().runTask(plugin, () -> opener.accept(player));
    }

    private void give(Player player, ItemStack item) {
        player.getInventory().addItem(item).values()
                .forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
    }

    private ItemStack icon(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(text(name));
        if (!lore.isEmpty()) {
            meta.lore(lore.stream().map(this::text).toList());
        }
        item.setItemMeta(meta);
        return item;
    }

    private Component text(String legacy) {
        return LegacyComponentSerializer.legacySection().deserialize(legacy)
                .decoration(TextDecoration.ITALIC, false);
    }

    private Material teamIcon(String team) {
        return switch (team) {
            case "police" -> Material.IRON_CHESTPLATE;
            case "government" -> Material.GOLDEN_HELMET;
            case "criminal" -> Material.IRON_SWORD;
            case "civilian" -> Material.PAPER;
            default -> Material.BOOK;
        };
    }

    private String colorOf(JobDefinition job) {
        return job.chatColor() != null ? job.chatColor() : "§f";
    }
}
