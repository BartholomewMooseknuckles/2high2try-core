package com.twohigh.core.law.item;

import com.twohigh.api.event.JobChangeEvent;
import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.defaults.DefaultJobs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public final class LawItemListener implements Listener {

    private final TwoHigh2TryCore plugin;

    public LawItemListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        String tag = LawItems.getTag(item);
        if (tag == null) return;

        event.setCancelled(true);

        switch (tag) {
            case LawItems.TAG_ARREST -> handleArrest(player, target);
            case LawItems.TAG_UNARREST -> handleUnarrest(player, target);
            case LawItems.TAG_WEAPON_CHECKER -> handleWeaponCheck(player, target);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        ItemStack item = attacker.getInventory().getItemInMainHand();
        String tag = LawItems.getTag(item);
        if (!LawItems.TAG_STUN.equals(tag)) return;

        String team = plugin.jobRegistry().getPlayerTeam(attacker.getUniqueId());
        if (!"police".equals(team)) {
            attacker.sendMessage("§cOnly police can use the stun stick.");
            return;
        }

        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 4, false, true, true));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, true, true));
        attacker.sendMessage("§eStunned §f" + victim.getName() + "§e for 3 seconds.");
        victim.sendMessage("§cYou have been stunned!");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        String tag = LawItems.getTag(item);
        if (tag == null) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        switch (tag) {
            case LawItems.TAG_DOOR_RAM -> {
                event.setCancelled(true);
                handleDoorRam(player, block);
            }
            case LawItems.TAG_LOCKPICK -> {
                if (block.getState() instanceof Container) {
                    event.setCancelled(true);
                    handleLockpick(player, block);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;
        if (LockpickTask.isActive(event.getPlayer().getUniqueId())) {
            LockpickTask.cancel(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage("§cYou moved! Lockpick failed.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJobChange(JobChangeEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayerUuid());
        if (player == null) return;

        LawItems.stripLawItems(player);

        String newJob = event.getNewJobId();
        if (newJob == null) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            switch (newJob) {
                case DefaultJobs.POLICE, DefaultJobs.CHIEF -> LawItems.givePoliceKit(player);
                case DefaultJobs.GANGSTER, DefaultJobs.THIEF -> LawItems.giveCriminalKit(player);
            }
        }, 1L);
    }

    private void handleArrest(Player officer, Player target) {
        String team = plugin.jobRegistry().getPlayerTeam(officer.getUniqueId());
        if (!"police".equals(team)) {
            officer.sendMessage("§cOnly police can use the arrest stick.");
            return;
        }
        UUID targetId = target.getUniqueId();
        if (!plugin.lawEnforcement().wantedManager().isWanted(targetId)) {
            officer.sendMessage("§c" + target.getName() + " is not wanted.");
            return;
        }
        if (plugin.lawEnforcement().arrestManager().arrest(officer.getUniqueId(), targetId)) {
            officer.sendMessage("§a" + target.getName() + " has been arrested!");
            Bukkit.broadcastMessage("§9§l[POLICE] §e" + target.getName()
                    + " §7arrested by §e" + officer.getName() + "§7.");
        } else {
            officer.sendMessage("§cFailed to arrest — no jail positions set.");
        }
    }

    private void handleUnarrest(Player officer, Player target) {
        String team = plugin.jobRegistry().getPlayerTeam(officer.getUniqueId());
        if (!"police".equals(team)) {
            officer.sendMessage("§cOnly police can use the unarrest stick.");
            return;
        }
        if (plugin.lawEnforcement().arrestManager().unarrest(officer.getUniqueId(), target.getUniqueId())) {
            officer.sendMessage("§a" + target.getName() + " released from jail.");
        } else {
            officer.sendMessage("§c" + target.getName() + " is not jailed.");
        }
    }

    private void handleWeaponCheck(Player officer, Player target) {
        String team = plugin.jobRegistry().getPlayerTeam(officer.getUniqueId());
        if (!"police".equals(team)) {
            officer.sendMessage("§cOnly police can use the weapon checker.");
            return;
        }

        officer.sendMessage("§b§lScanning " + target.getName() + "...");
        boolean foundContraband = false;

        for (ItemStack slot : target.getInventory().getContents()) {
            if (slot == null) continue;
            String itemTag = LawItems.getTag(slot);
            if (LawItems.TAG_LOCKPICK.equals(itemTag)) {
                officer.sendMessage(" §c[!] Lockpick detected");
                foundContraband = true;
            }
            if (slot.getType() == Material.GUNPOWDER || slot.getType() == Material.TNT) {
                officer.sendMessage(" §c[!] " + slot.getType().name() + " x" + slot.getAmount());
                foundContraband = true;
            }
        }

        if (!foundContraband) {
            officer.sendMessage("§a  Clean — no contraband found.");
        }
        target.sendMessage("§7You have been searched by §e" + officer.getName() + "§7.");
    }

    private void handleDoorRam(Player officer, Block block) {
        String team = plugin.jobRegistry().getPlayerTeam(officer.getUniqueId());
        if (!"police".equals(team)) {
            officer.sendMessage("§cOnly police can use the door ram.");
            return;
        }

        if (!(block.getState() instanceof Container container)) {
            officer.sendMessage("§cThat's not a container.");
            return;
        }

        if (!plugin.lawEnforcement().warrantManager().hasWarrant(
                getContainerOwner(block))) {
            officer.sendMessage("§cYou need an active warrant to ram this container.");
            return;
        }

        officer.sendMessage("§9Door rammed! Opening container...");
        officer.openInventory(container.getInventory());
    }

    private void handleLockpick(Player player, Block block) {
        String team = plugin.jobRegistry().getPlayerTeam(player.getUniqueId());
        if (!"criminal".equals(team)) {
            player.sendMessage("§cOnly criminals can use lockpicks.");
            return;
        }

        if (!(block.getState() instanceof Container)) {
            player.sendMessage("§cThat's not a container.");
            return;
        }

        LockpickTask.start(player, block, plugin, plugin.jobRegistry());
    }

    private UUID getContainerOwner(Block block) {
        return null;
    }
}
