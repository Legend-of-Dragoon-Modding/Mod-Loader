package org.legendofdragoon.modloader.events.listeners;

import org.legendofdragoon.modloader.events.Event;

public interface BeforeEventListener<T extends Event> extends BaseListener {
    BeforeResult Before(T event);
}
