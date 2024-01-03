package org.legendofdragoon.modloader.events.listeners;

import org.legendofdragoon.modloader.events.Event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * An annotation to mark a method as an Event Listener.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventListener {
    /***
     * Whether the Event Listener will be called Before or After the game's logic.
     * @return The kind of Event Listener.
     */
    Kind kind();

    /***
     * The Event that the Event Listener will listen for.
     * @return
     */
    Class<? extends Event> event();

    /***
     * The priority of a listener.
     * If more than one Event Listener has the same priority, they will be called in the order they were registered.
     * @return The priority of the Event Listener.
     */
    Priority priority() default Priority.Normal;
}
