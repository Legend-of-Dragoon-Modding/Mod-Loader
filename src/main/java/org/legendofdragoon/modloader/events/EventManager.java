package org.legendofdragoon.modloader.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.ModManager;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class EventManager {
  private static final Logger LOGGER = LogManager.getFormatterLogger(EventManager.class);

  private final Map<Class<?>, Set<EventBinding>> listeners = new HashMap<>();
  private final Set<EventBinding> staleListeners = new HashSet<>();

  private ModManager modManager;

  public EventManager(final Consumer<Access> access) {
    access.accept(new Access());
  }

  public class Access {
    private Access() { }

    public void initialize(final ModManager mods) {
      EventManager.this.modManager = mods;

      LOGGER.info("Scanning for event consumers...");

      final ConfigurationBuilder config = new ConfigurationBuilder()
        .addClassLoaders(this.getClass().getClassLoader())
        .addUrls(ClasspathHelper.forPackage("legend"));
      final Reflections reflections = new Reflections(mods.addModsToReflectionsConfig(config));
      final Set<Class<?>> listeners = reflections.getTypesAnnotatedWith(EventListener.class);

      for(final Class<?> listener : listeners) {
        EventManager.this.register(listener, null);
      }
    }

    public void reset() {
      EventManager.this.listeners.clear();
      EventManager.this.staleListeners.clear();
    }
  }

  private void register(final Class<?> listener, @Nullable final Object instance) {
    for(final Method method : listener.getDeclaredMethods()) {
      if(method.isAnnotationPresent(EventListener.class)) {
        if(!method.canAccess(instance)) {
          LOGGER.warn("Event listener %s must be static", listener);
          continue;
        }

        if(method.getParameterCount() != 1) {
          LOGGER.warn("Event listener %s must have one parameter", listener);
          continue;
        }

        final Class<?> eventType = method.getParameters()[0].getType();

        if(!Event.class.isAssignableFrom(eventType)) {
          LOGGER.warn("Event listener %s must have event parameter", listener);
          continue;
        }

        synchronized(this.listeners) {
          this.listeners.computeIfAbsent(eventType, k -> new HashSet<>()).add(new EventBinding(eventType, listener, instance, method));
        }
      }
    }
  }

  public void register(final Object listener) {
    this.register(listener.getClass(), listener);
  }

  public <T extends Event> T postEvent(final T event) {
    synchronized(this.listeners) {
      final Set<EventBinding> bindings = this.listeners.get(event.getClass());

      if(bindings != null) {
        for(final EventBinding binding : bindings) {
          try {
            this.modManager.setActiveModByClassloader(binding.listenerClass.getClassLoader());
            binding.execute(event);
          } catch(final IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Failed to deliver event", e);
          }

          if(binding.isInvalid()) {
            this.staleListeners.add(binding);
          }
        }
      }

      this.modManager.setActiveModByClassloader(null);
    }

    return event;
  }

  public void clearStaleRefs() {
    synchronized(this.listeners) {
      for(final EventBinding binding : this.staleListeners) {
        this.listeners.get(binding.eventClass).remove(binding);
      }

      this.staleListeners.clear();
    }
  }
}
