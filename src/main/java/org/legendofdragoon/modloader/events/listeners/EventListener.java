package org.legendofdragoon.modloader.events.listeners;

import org.legendofdragoon.modloader.events.Event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener {
    Priority beforePriority() default Priority.Normal;
    Priority afterPriority() default Priority.Normal;
    Class<? extends Event> event();
}
