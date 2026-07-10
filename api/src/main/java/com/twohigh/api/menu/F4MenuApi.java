package com.twohigh.api.menu;

import org.bukkit.entity.Player;

/**
 * The F4 menu — the game mode's central hub GUI. The core renders it as a
 * chest GUI (works on every client); a native dialog version may replace the
 * rendering later without changing this API.
 */
public interface F4MenuApi {

    /** Opens the main menu for a player. Same as running /f4. */
    void open(Player player);

    /** Registers an addon tab on the main menu. Ignored if the id is taken. */
    void registerTab(F4Tab tab);

    /** Removes a previously registered addon tab. */
    void unregisterTab(String tabId);
}
