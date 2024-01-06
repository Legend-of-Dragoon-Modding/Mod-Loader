package org.legendofdragoon.modloader.events.listeners;

/**
 * Whether the Event Listener will be called Before or After the game's logic.
 */
public enum Kind {
    /**
     * The Event Listener will be called before the game's logic.
     */
    BEFORE,
    /**
     * The Event Listener will be called after the game's logic.
     */
    AFTER
}
