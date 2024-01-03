package org.legendofdragoon.modloader.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.ModManager;
import org.legendofdragoon.modloader.events.listeners.EventListener;
import org.legendofdragoon.modloader.events.listeners.Result;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

/***
 * The Event Manager is responsible for registering event listeners and managing sending Events.
 */
public class EventManager {

  private static final Logger LOGGER = LogManager.getFormatterLogger(EventManager.class);

  /***
   * The map of event listeners.
   * Key: Event Type Name
   * Value: Event Listeners
   */
  private final Map<String, EventListeners<?>> listeners = Collections.synchronizedMap(new HashMap<>());

  /***
   * Creates a new Event Manager.
   */
  public EventManager() {}

  /***
   * Registers all Event Listeners in the given object.
   * @param modID Mod ID of the object
   * @param listener Class to register Event Listeners in
   * @param instance of the class
   */
  public void registerListeners(final String modID, final Class<?> listener, @Nullable Object instance) {
    try {
      for (final var l : this.findListeners(modID, listener, instance)) {
        try {
          final var event = this.getEvent(l);
          final var name = event.getTypeName();
          synchronized (EventManager.this.listeners) {
            // Locking here as the `register` step could be missed
            var i = EventManager.this.listeners.get(name);
            if (i == null) {
              i = new EventListeners<>();
              EventManager.this.listeners.put(name, i);
            }
            i.register(event, l);
          }
        } catch (Exception e) {
          LOGGER.error("Failed to register listener %s", l.getClass().getTypeName(), e);
        }
      }
    } catch (Exception e) {
      LOGGER.error("Failed to find listeners for %s", listener.getTypeName(), e);
    }
  }

  /***
   * Finds all Event Listeners in the given object.
   * @param modID Mod ID of the object
   * @param listener Class to register Event Listeners in
   * @param instance of the class
   * @return Set of Event Listeners found
   */
  private Set<EventListeners.MethodListener> findListeners(final String modID, final Class<?> listener, @Nullable Object instance) {
    final Set<EventListeners.MethodListener> result = new HashSet<>();
    // Check to see any of the listener's methods implement either before or after listener
    final var methods = listener.getDeclaredMethods();
    for (final var m : methods) {
      if (m.isAnnotationPresent(EventListener.class)) {
        final var a = m.getAnnotation(EventListener.class);
        final var parent = instance == null ? null : new WeakReference<>(instance);
        result.add(new EventListeners.MethodListener(modID, parent, m, a.kind(), a.priority()));
      }
    }
    if (result.isEmpty()) {
      LOGGER.info("No listeners found in %s".formatted(listener.getTypeName()));
    }
    return result;
  }

  /***
   * Gets the event type for the given listener.
   * @param listener Listener to get event type for
   * @return Event type for the given listener
   * @param <T> Event type
   * @throws Exception If the listener is not valid
   */
  @SuppressWarnings("unchecked")
  private <T extends Event>  Class<T> getEvent(Object listener) throws Exception {
    if (listener instanceof EventListeners.MethodListener m) {
      final var method = m.method();
      if (method.isAnnotationPresent(EventListener.class)) {
        return (Class<T>) method.getAnnotation(EventListener.class).event();
      }
    }
    throw new Exception("Listener %s does not have an event annotation".formatted(listener.getClass().getTypeName()));
  }

  /***
   * Posts an Event to all registered listeners.
   * @param event Event to post
   * @param defaultLogic Default logic to run if no 'before' listeners stop execution
   * @param <T> Event type
   */
  @SuppressWarnings("unchecked")
  public <T extends Event> void postEvent(final T event, final Listener<T> defaultLogic) {
    final var listeners = (EventListeners<T>) EventManager.this.listeners.get(event.getClass().getTypeName());
    final var result = listeners.before(event);
    if (result != Result.CANCEL) {
      if (result == Result.CONTINUE) {
        defaultLogic.action(event);
      }
      listeners.after(event);
    }
  }

  /***
   * Gets the Event Listeners for the given event.
   * @param event Event to get Event Listeners for
   * @return Event Listeners for the given event
   * @param <T> Event type
   */
  <T extends Event> EventListeners<?> getListeners(Class<T> event) {
    return EventManager.this.listeners.get(event.getTypeName());
  }

  /***
   * Listener for the default logic to run if no 'before' listeners stop execution.
   * @param <T> Event type
   */
  public interface Listener<T extends Event> {
    void action(T event);
  }
}
