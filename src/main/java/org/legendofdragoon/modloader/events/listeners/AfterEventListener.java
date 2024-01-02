package org.legendofdragoon.modloader.events.listeners;

import org.legendofdragoon.modloader.events.Event;

public interface AfterEventListener<T extends Event> extends BaseListener {
    void After(T event);
}
