package org.legendofdragoon.modloader.events;

import org.legendofdragoon.modloader.events.listeners.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

class EventListeners<T extends Event> {
  private final List<BeforeEventListener<T>> before = new CopyOnWriteArrayList<>();
  private final List<AfterEventListener<T>> after = new CopyOnWriteArrayList<>();

  @SuppressWarnings("unchecked")
  public void register(final Object listener) {
    synchronized(this) {
      if (listener instanceof BeforeEventListener<?>) {
        this.before.add((BeforeEventListener<T>) listener);
        this.before.sort((o1, o2) -> {
          var p1 = listenerPriority(o1, true);
          var p2 = listenerPriority(o2, true);
          return p1.compareTo(p2);
        });
      }
      if (listener instanceof AfterEventListener<?>) {
        this.after.add((AfterEventListener<T>) listener);
        this.after.sort((o1, o2) -> {
            var p1 = listenerPriority(o1, false);
            var p2 = listenerPriority(o2, false);
            return p1.compareTo(p2);
        });
      }
    }
  }

  private Priority listenerPriority(final Object listener, final boolean isBefore) {
      final var c = listener.getClass();
      final var a = c.getAnnotation(EventListener.class);
      if (a != null) {
        return (isBefore) ? a.beforePriority() : a.afterPriority();
      }
      return Priority.Normal;
  }

  public BeforeResult before(final T event) {
    for(final var b : this.before) {
      if (b != null) {
        final var result = b.Before(event);
        if (result != BeforeResult.Continue) {
          return result;
        }
      }
    }
    return BeforeResult.Continue;
  }

  public void after(final T event) {
    for (final var o : this.after) {
      if (o != null) {
        o.After(event);
      }
    }
  }

  public Collection<BeforeEventListener<T>> getBeforeListeners() {
    return this.before;
  }

  public Collection<AfterEventListener<T>> getAfterListeners() {
    return this.after;
  }

  public void clearStaleRefs() {
    synchronized(this) {
      this.before.removeIf(Objects::isNull);
      this.after.removeIf(Objects::isNull);
    }
  }

  public void reset() {
    this.before.clear();
    this.after.clear();
  }
}
