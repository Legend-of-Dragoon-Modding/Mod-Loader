package org.legendofdragoon.modloader.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.listeners.AfterEventListener;
import org.legendofdragoon.modloader.events.listeners.BaseListener;
import org.legendofdragoon.modloader.events.listeners.BeforeEventListener;
import org.legendofdragoon.modloader.events.listeners.BeforeResult;
import org.legendofdragoon.modloader.events.listeners.EventListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {

  private static final Logger LOGGER = LogManager.getFormatterLogger(EventManager.class);

  private final Map<String, EventListeners<?>> listeners = new ConcurrentHashMap<>();

  public EventManager() {//final Consumer<Access> access) {
    //access.accept(new Access());
  }

//  public class Access {
//    private Access() { }
//
//    public void initialize(final ModManager mods) {
//      LOGGER.info("Scanning for event consumers...");
//
//      final ConfigurationBuilder config = new ConfigurationBuilder()
//        .addClassLoaders(this.getClass().getClassLoader())
//        .addUrls(ClasspathHelper.forPackage("legend"));
//      final Reflections reflections = new Reflections(mods.addModsToReflectionsConfig(config));
//      final Set<Class<?>> listeners = reflections.getTypesAnnotatedWith(EventListener.class);
//
//      for(final Class<?> listener : listeners) {
//        EventManager.this.registerListener(listener);
//      }
//    }
//
//    public void reset() {
//      final var listeners = EventManager.this.listeners;
//      for (final var l : listeners.values()) {
//        l.reset();
//      }
//      listeners.clear();
//    }
//  }

  public void registerListener(final Object listener) {
    try {
      final var a = listener.getClass().getAnnotation(EventListener.class);
      final var name = a.event().getTypeName();
      synchronized (EventManager.this.listeners) {
        // Locking here as the register step could be missed
        final var l = EventManager.this.listeners.getOrDefault(name, new EventListeners<>());
        l.register(listener);
        EventManager.this.listeners.putIfAbsent(name, l);
      }
    } catch (NullPointerException e) {
      LOGGER.error("Failed to register listener %s", listener.getClass().getTypeName(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Event> void postEvent(final T event, final AfterEventListener<T> defaultLogic) {
    final var listeners = (EventListeners<T>) EventManager.this.listeners.get(event.getClass().getTypeName());
    final var result = listeners.before(event);
    if (result != BeforeResult.Return) {
      if (result == BeforeResult.Continue) {
        defaultLogic.After(event);
      }
      listeners.after(event);
    }
  }

  public void clearStaleRefs() {
    for(var v : EventManager.this.listeners.values()) {
      v.clearStaleRefs();
    }
  }

  <T extends Event> EventListeners<?> getListeners(Class<T> c) {
    return EventManager.this.listeners.get(c.getTypeName());
  }
}
