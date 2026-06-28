package com.twohigh.core.law.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class LawItems {

    public static final String TAG_ARREST = "arrest_stick";
    public static final String TAG_UNARREST = "unarrest_stick";
    public static final String TAG_STUN = "stun_stick";
    public static final String TAG_DOOR_RAM = "door_ram";
    public static final String TAG_WEAPON_CHECKER = "weapon_checker";
    public static final String TAG_LOCKPICK = "lockpick";

    private static NamespacedKey itemKey;

    private LawItems() {}

    public static void init(JavaPlugin plugin) {
        itemKey = new NamespacedKey(plugin, "law_item");
    }

    public static NamespacedKey key() {
        return itemKey;
    }

    public static String getTag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(itemKey, PersistentDataType.STRING);
    }

    public static ItemStack arrestStick() {
        return build(Material.BLAZE_ROD, TAG_ARREST,
                "§c§lArrest Stick",
                List.of("§7Right-click a §cwanted§7 player to arrest them."));
    }

    public static ItemStack unarrestStick() {
        return build(Material.BREEZE_ROD, TAG_UNARREST,
                "§a§lUnarrest Stick",
                List.of("§7Right-click a §cjailed§7 player to release them."));
    }

    public static ItemStack stunStick() {
        return build(Material.BLAZE_ROD, TAG_STUN,
                "§e§lStun Stick",
                List.of("§7Hit a player to stun them.",
                        "§7Applies §cSlowness V§7 + §cWeakness§7 for 3s."));
    }

    public static ItemStack doorRam() {
        return build(Material.IRON_AXE, TAG_DOOR_RAM,
                "§9§lDoor Ram",
                List.of("§7Right-click a container to force it open.",
                        "§7Requires an active §ewarrant§7."));
    }

    public static ItemStack weaponChecker() {
        return build(Material.COMPASS, TAG_WEAPON_CHECKER,
                "§b§lWeapon Checker",
                List.of("§7Right-click a player to scan for contraband."));
    }

    public static ItemStack lockpick() {
        return build(Material.TRIPWIRE_HOOK, TAG_LOCKPICK,
                "§8§lLockpick",
                List.of("§7Right-click a container to pick the lock.",
                        "§7Takes time — don't move!"));
    }

    private static ItemStack build(Material material, String tag, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, tag);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public static void givePoliceKit(org.bukkit.entity.Player player) {
        player.getInventory().addItem(
                arrestStick(), unarrestStick(), stunStick(), doorRam(), weaponChecker());
    }

    public static void giveCriminalKit(org.bukkit.entity.Player player) {
        player.getInventory().addItem(lockpick());
    }

    public static void stripLawItems(org.bukkit.entity.Player player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack slot = inv.getItem(i);
            if (slot != null && getTag(slot) != null) {
                inv.setItem(i, null);
            }
        }
    }
}
