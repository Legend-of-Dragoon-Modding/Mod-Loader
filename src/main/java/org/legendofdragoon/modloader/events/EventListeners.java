package org.legendofdragoon.modloader.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.listeners.*;
import org.legendofdragoon.modloader.events.listeners.Result;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class EventListeners<T extends Event> {
  private static final Logger LOGGER = LogManager.getFormatterLogger(EventManager.class);

  /***
   * The prioritized set of 'before' Event Listeners.
   */
  private final List<Invokable> before = new CopyOnWriteArrayList<>();

  /***
   * The prioritized set of 'after' Event Listeners.
   */
  private final List<Invokable> after = new CopyOnWriteArrayList<>();

  /***
   * A record to store the invokable method for an Event Listener.
   * @param parent Class containing the method
   * @param method Method to invoke
   * @param priority Priority of listener
   */
  private record Invokable(Object parent, Method method, Priority priority) {
    public Object invoke(Object event) throws InvocationTargetException, IllegalAccessException {
      return this.method.invoke(this.parent, event);
    }
  }

  /***
   * A record to store the Event Listener method, parent, kind, and priority of a listener.
   * @param parent Class containing the method
   * @param method Method to invoke
   * @param kind Kind of listener
   * @param priority Priority of listener
   */
  record MethodListener(Object parent, Method method, Kind kind, Priority priority)  {
    @Override
    public int hashCode() {
      // Priority is ignored
      return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof MethodListener m) {
        // Priority is ignored
        return this.parent.getClass().getName().equals(m.parent.getClass().getName()) &&
                this.method.getName().equals(m.method.getName()) &&
                this.kind.name().equals(m.kind.name());
      }
      return false;
    }

    @Override
    public String toString() {
      // Priority is ignored
      return "%s.%s.%s".formatted(this.parent.getClass().getName(), this.method.getName(), this.kind.name());
    }
  }

  /***
   * Registers a listener to the event manager.
   * @param event Event the Event Listener is listening for
   * @param listener Event Listener to register
   * @throws Exception If the listener is not valid
   */
  public void register(final Class<? extends Event> event, final Object listener) throws Exception {
    if (listener instanceof MethodListener m) {
      if (m.kind == Kind.BEFORE) {
        this.registerBeforeListener(event, m);
      } else if (m.kind == Kind.AFTER) {
        this.registerAfterListener(event, m);
      }
    }
  }

  /***
   * Registers a 'before' Event Listener to the event manager.
   * @param event Event the Event Listener is listening for
   * @param listener Event Listener to register
   * @throws Exception If the listener is not valid
   */
  private void registerBeforeListener(Class<? extends Event> event, MethodListener listener) throws Exception {
    final var i = this.createInvokable(listener, event);
    this.before.add(i);
    this.before.sort(Comparator.comparing(o -> o.priority));
  }

  /***
   * Registers an 'after' Event Listener to the event manager.
   * @param event Event the Event Listener is listening for
   * @param listener Event Listener to register
   * @throws Exception If the listener is not valid
   */
  private void registerAfterListener(Class<? extends Event> event, MethodListener listener) throws Exception {
    final var callable = this.createInvokable(listener, event);
    this.after.add(callable);
    this.after.sort(Comparator.comparing(o -> o.priority));
  }

  /***
   * Creates an Invokable for the Event Listener.
   * @param m Event Listener to create Invokable for
   * @param event Event the Event Listener is listening for
   * @return Invokable for the Event Listener
   * @throws Exception If the listener is not valid
   */
  private Invokable createInvokable(MethodListener m, Class<? extends Event> event) throws Exception {
    // Make sure it's assignable
    if (!m.method.getParameters()[0].getType().isAssignableFrom(event)) {
      throw new Exception("Listener %s.%s does not have a parameter of type %s".formatted(m.getClass().getTypeName(), m.parent.getClass().getName(), m.method.getName(), event.getTypeName()));
    }
    return new Invokable(m.parent, m.method, m.priority);
  }

  /***
   * Invokes all 'before' Event Listeners for the event.
   * @param event The posted Event
   * @return Whether to continue the processing of the event.
   */
  public Result before(final T event) {
    for (final var i = this.before.iterator(); i.hasNext(); ) {
      final var invokable =  i.next();
      try {
        final var result = invokable.invoke(event);
        if (result == Result.HANDLED || result == Result.CANCEL) {
          return (Result) result;
        }
      } catch (Exception e) {
        LOGGER.error("Failed to invoke 'before' listener for %s".formatted(invokable.toString()), e);
        i.remove();
      }
    }
    return Result.CONTINUE;
  }

  /***
   * Invokes all 'after' Event Listeners for the event.
   * @param event The posted Event
   */
  public void after(final T event) {
    for (final var i = this.after.iterator(); i.hasNext(); ) {
      final var invokable =  i.next();
      try {
        invokable.invoke(event);
      } catch (Exception e) {
        i.remove();
        LOGGER.error("Failed to invoke 'after' listener for %s".formatted(invokable.toString()), e);
      }
    }
  }

  /***
   * Returns the 'before' Event Listeners for the event.
   * @return The 'before' Event Listeners for the event.
   */
  Collection<?> getBeforeListeners() {
    return this.before;
  }

  /***
   * Returns the 'after' Event Listeners for the event.
   * @return The 'after' Event Listeners for the event.
   */
  Collection<?> getAfterListeners() {
    return this.after;
  }

  public void reset() {
    this.before.clear();
    this.after.clear();
  }
}
