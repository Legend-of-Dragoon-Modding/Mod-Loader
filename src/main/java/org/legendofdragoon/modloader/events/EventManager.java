package org.legendofdragoon.modloader.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.ModManager;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventManager {
  private static final Logger LOGGER = LogManager.getFormatterLogger(EventManager.class);

  private final Map<Consumer<Event>, Class<?>> listeners = new ConcurrentHashMap<>();
  private final Set<Consumer<Event>> staleListeners = Collections.synchronizedSet(new HashSet<>());

  public EventManager(final Consumer<Access> access) {
    access.accept(new Access());
  }

  public class Access {
    private Access() { }

    public void initialize(final ModManager mods) {
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

        if(!Event.class.isAssignableFrom(method.getParameters()[0].getType())) {
          LOGGER.warn("Event listener %s must have event parameter", listener);
          continue;
        }

        if(instance == null) {
          this.listeners.put(event -> {
            try {
              method.invoke(null, event);
            } catch(final IllegalAccessException | InvocationTargetException e) {
              LOGGER.error("Failed to deliver event", e);
            }
          }, method.getParameters()[0].getType());
        } else {
          final WeakReference<Object> ref = new WeakReference<>(instance);
          this.listeners.put(new Consumer<>() {
            @Override
            public void accept(final Event event) {
              final Object inst = ref.get();

              if(inst == null) {
                EventManager.this.staleListeners.add(this);
              } else {
                try {
                  method.invoke(inst, event);
                } catch(final IllegalAccessException | InvocationTargetException e) {
                  LOGGER.error("Failed to deliver event", e);
                }
              }
            }
          }, method.getParameters()[0].getType());
        }
      }
    }
  }

  public void register(final Object listener) {
    this.register(listener.getClass(), listener);
  }

  public <T extends Event> T postEvent(final T event) {
    for(final var entry : this.listeners.entrySet()) {
      if(entry.getValue().isInstance(event)) {
        entry.getKey().accept(event);
      }
    }

    return event;
  }

  public void clearStaleRefs() {
    this.listeners.keySet().removeAll(this.staleListeners);
    this.staleListeners.clear();
  }
}
