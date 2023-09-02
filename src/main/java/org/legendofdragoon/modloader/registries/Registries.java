package org.legendofdragoon.modloader.registries;

import org.legendofdragoon.modloader.events.EventManager;
import org.legendofdragoon.modloader.events.registries.RegistryEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class Registries {
  private final EventManager events;
  private final List<MutableRegistry<?>> registries = new ArrayList<>();
  private final Map<MutableRegistry<?>, Function<MutableRegistry<?>, RegistryEvent.Register<?>>> registryEvents = new HashMap<>();

  protected Registries(final EventManager events, final Consumer<Access> access) {
    this.events = events;
    access.accept(new Access());
  }

  protected <Type extends RegistryEntry> Registry<Type> addRegistry(final Registry<Type> registry, final Function<MutableRegistry<Type>, RegistryEvent.Register<Type>> registryEvent) {
    final MutableRegistry<Type> mutableRegistry = (MutableRegistry<Type>)registry;
    this.registries.add(mutableRegistry);
    //noinspection unchecked
    this.registryEvents.put(mutableRegistry, (Function<MutableRegistry<?>, RegistryEvent.Register<?>>)(Object)registryEvent);
    return registry;
  }

  public class Access {
    private Access() { }

    private final Set<Registry<?>> initialized = new HashSet<>();

    public <T extends RegistryEntry> void initialize(final Registry<?> registry) {
      if(this.initialized.contains(registry)) {
        throw new IllegalStateException("Registry " + registry + " already initialized");
      }

      final MutableRegistry<?> mutableRegistry = (MutableRegistry<?>)registry;

      if(!Registries.this.registryEvents.containsKey(mutableRegistry)) {
        throw new IllegalArgumentException("Unknown registry " + registry);
      }

      Registries.this.events.postEvent(Registries.this.registryEvents.get(mutableRegistry).apply(mutableRegistry));
      mutableRegistry.lock();
      this.initialized.add(mutableRegistry);
    }

    public void initializeRemaining() {
      for(final MutableRegistry<?> registry : Registries.this.registries) {
        if(!this.initialized.contains(registry)) {
          this.initialize(registry);
        }
      }
    }

    public void reset() {
      this.initialized.clear();

      for(final MutableRegistry<?> registry : Registries.this.registries) {
        registry.reset();
      }
    }
  }
}
