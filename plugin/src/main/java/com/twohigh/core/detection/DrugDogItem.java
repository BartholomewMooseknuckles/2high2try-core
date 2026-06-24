package com.twohigh.core.detection;

import com.twohigh.api.detection.SignalReading;
import com.twohigh.api.detection.SignalTier;
import com.twohigh.core.TwoHigh2TryCore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Optional;

public final class DrugDogItem {

    private static final NamespacedKey DOG_KEY = new NamespacedKey("twohigh", "drug_dog_level");

    private DrugDogItem() {}

    public static ItemStack create(int level) {
        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Drug Dog Whistle (Lv." + level + ")", NamedTextColor.GOLD));
        meta.lore(List.of(
                Component.text("Right-click to sniff for signals", NamedTextColor.GRAY),
                Component.text("Range: " + (level * 50) + " blocks", NamedTextColor.DARK_GRAY)
        ));
        meta.getPersistentDataContainer().set(DOG_KEY, PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isDrugDog(ItemStack item) {
        if (item == null || item.getType() != Material.BONE) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(DOG_KEY, PersistentDataType.INTEGER);
    }

    public static int getLevel(ItemStack item) {
        if (!isDrugDog(item)) return 0;
        Integer level = item.getItemMeta().getPersistentDataContainer()
                .get(DOG_KEY, PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }

    public static void sniff(Player player, TwoHigh2TryCore plugin) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!isDrugDog(held)) return;

        int level = getLevel(held);
        double range = level * 50.0;

        DetectionManagerImpl detection = plugin.detectionManager();
        Optional<SignalReading> strongest = detection.getStrongestSignal(player.getLocation(), range);

        if (strongest.isEmpty()) {
            player.sendMessage("§7The dog sniffs around but finds nothing...");
            return;
        }

        SignalReading reading = strongest.get();
        Location dir = reading.direction();
        Location pLoc = player.getLocation();
        double dx = dir.getX() - pLoc.getX();
        double dz = dir.getZ() - pLoc.getZ();
        String compass = compassDirection(dx, dz);

        String tierColor = switch (reading.tier()) {
            case FAINT -> "§7";
            case INTERESTED -> "§e";
            case ALERT -> "§6";
            case FERAL -> "§c";
        };

        player.sendMessage(tierColor + "§l[DOG] " + tierDisplay(reading.tier())
                + " §7— pointing §f" + compass
                + " §7(strength: " + String.format("%.1f", reading.effectiveStrength()) + ")");
    }

    private static String tierDisplay(SignalTier tier) {
        return switch (tier) {
            case FAINT -> "§7Faint scent...";
            case INTERESTED -> "§eThe dog perks up!";
            case ALERT -> "§6The dog is ALERT!";
            case FERAL -> "§c§lThe dog goes FERAL!";
        };
    }

    private static String compassDirection(double dx, double dz) {
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        if (angle < 0) angle += 360;

        if (angle < 22.5 || angle >= 337.5) return "S";
        if (angle < 67.5) return "SW";
        if (angle < 112.5) return "W";
        if (angle < 157.5) return "NW";
        if (angle < 202.5) return "N";
        if (angle < 247.5) return "NE";
        if (angle < 292.5) return "E";
        return "SE";
    }
}
