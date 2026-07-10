package com.twohigh.api.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Consumer;

/**
 * A custom tab shown on the main F4 menu. Addons register these to add
 * their own entries (e.g. a weed shop, a gun catalogue). Clicking the
 * tab's icon runs {@code onClick} — typically opening the addon's own GUI.
 */
public record F4Tab(
        String id,
        String displayName,
        Material icon,
        List<String> lore,
        Consumer<Player> onClick,
        Plugin owningPlugin
) {}
