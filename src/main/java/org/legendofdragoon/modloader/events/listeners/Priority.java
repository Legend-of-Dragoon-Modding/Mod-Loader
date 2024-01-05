package org.legendofdragoon.modloader.events.listeners;

/**
 * The priority of a listener.
 * If more than one Event Listener has the same priority, they will be called in the order they were registered.
 */
public enum Priority {
    /**
     * The listener will be called first.
     */
    First,
    /**
     * The listener will be called after First priority Event Listeners.
     */
    High,
    /**
     * The listener will be called after High priority Event Listeners.
     */
    Normal,
    /**
     * The listener will be called after Normal priority Event Listeners.
     */
    Low,
    /**
     * The listener will be called last.
     */
    Last
}
