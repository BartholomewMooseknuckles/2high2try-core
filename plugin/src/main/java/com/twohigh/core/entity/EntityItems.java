package com.twohigh.core.entity;

import com.twohigh.api.entity.EntityDefinition;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;

/** Builds and identifies the placeable item form of a registered entity. */
public final class EntityItems {

    public static final NamespacedKey ENTITY_KEY = new NamespacedKey("twohigh", "entity_id");

    private EntityItems() {}

    public static ItemStack create(EntityDefinition def) {
        ItemStack item = new ItemStack(def.blockMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(def.displayName(), NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Place to set up", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text(def.maxPerPlayer() > 0
                                ? "Max per player: " + def.maxPerPlayer()
                                : "No placement limit", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(ENTITY_KEY, PersistentDataType.STRING, def.id());
        item.setItemMeta(meta);
        return item;
    }

    /** Returns the entity id stored on the item, or null if it's not an entity item. */
    public static String entityId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(ENTITY_KEY, PersistentDataType.STRING);
    }
}
