package org.legendofdragoon.modloader.events.listeners;

/**
 * The result of a before event.
 */
public enum Result {
    /**
     * Continue to the next listener.
     */
    CONTINUE,
    /**
     * The event was handled and should not be passed to any other listeners.
     * This will skip the core game's logic.
     * After events will still be called.
     */
    HANDLED,
    /**
     * The event was handled and should not be passed to any other listeners.
     * No After events will be called either.
     * Should only be used in rare cases.
     */
    CANCEL,
}
