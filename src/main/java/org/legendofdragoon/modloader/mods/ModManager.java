package org.legendofdragoon.modloader.mods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventManager;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ModManager {
  private static final Logger LOGGER = LogManager.getFormatterLogger(ModManager.class);

  private final Map<String, ModContainer> allMods = new HashMap<>();

  private final Map<String, ModContainer> loadedMods = new HashMap<>();

  public ModManager(final Consumer<Access> access) {
    access.accept(new Access());
  }

  public Collection<ModContainer> getLoadedMods() {
    return this.loadedMods.values();
  }

  public class Access {
    private Access() {
    }

    public void findMods() throws IOException {
      final Path modsDir = Path.of("./mods");
      Files.createDirectories(modsDir);

      LOGGER.info("Scanning for mods...");

      final Collection<URL> urlList = new ArrayList<>();
      try (final DirectoryStream<Path> jars = Files.newDirectoryStream(modsDir, "*.jar")) {
        for (final Path jar : jars) {
          urlList.add(jar.toUri().toURL());
        }
      }

      ModManager.this.allMods.clear();
      for (final var url : urlList) {
        final ClassLoader classLoader = new URLClassLoader(urlList.toArray(URL[]::new), ModManager.class.getClassLoader());
        final Reflections reflections = new Reflections(
                new ConfigurationBuilder().addClassLoaders(classLoader).addUrls(url)
        );
        final Set<Class<?>> modClasses = reflections.getTypesAnnotatedWith(Mod.class);
        if (modClasses == null || modClasses.isEmpty()) {
          LOGGER.error("No mods found in %s", url);
        } else if (modClasses.size() > 1) {
          LOGGER.error("Multiple mods found in %s", url);
        } else {
          final var modClass = modClasses.iterator().next();
          final var modID = modClasses.iterator().next().getDeclaredAnnotation(Mod.class).id();
          if (ModManager.this.loadedMods.containsKey(modID)) {
            LOGGER.error("Duplicate mod ID %s! Skipping.", modID);
          } else {
            LOGGER.info("Found mod: %s", modID);
            ModManager.this.allMods.put(
                    modID,
                    new ModContainer(modID, modClass, reflections, classLoader, modClass.getProtectionDomain().getCodeSource().getLocation())
            );
          }
        }
      }
    }

    /**
     * Load all mods in the list and return a list of missing mods
     */
    public Set<String> loadMods(final EventManager eventManager, final Set<String> modIds) {
      ModManager.this.loadedMods.clear();

      final Set<String> missingModIds = new HashSet<>();
      for (final String modId : modIds) {
        if (!this.loadMod(eventManager, modId)) {
          missingModIds.add(modId);
        }
      }
      return missingModIds;
    }

    public boolean loadMod(final EventManager eventManager, final String modId) {
      final var mod = ModManager.this.allMods.get(modId);
      if(mod == null) {
        LOGGER.warn("Could not find mod: %s", modId);
        return false;
      }
      LOGGER.info("Loaded mod: %s", modId);

      try {
        eventManager.registerListeners(mod.modId, mod.mod, mod.Instance());
      } catch (Exception e) {
        LOGGER.error("Failed to load mod: %s", modId, e);
        return false;
      }
      ModManager.this.loadedMods.put(modId, mod);
      return true;
    }

    public void removeMod(final EventManager eventManager, final String modId) {
      final var mod = ModManager.this.allMods.get(modId);
      if(mod == null) {
        LOGGER.warn("Could not find mod: %s", modId);
        return;
      }
      LOGGER.info("Removing mod: %s", modId);

      try {
        eventManager.unregister(modId);
      } catch (Exception e) {
        LOGGER.error("Failed to remove mod: %s", modId, e);
        return;
      }
      ModManager.this.loadedMods.remove(modId);

      this.loadMods(eventManager, ModManager.this.loadedMods.keySet());
    }

    public void loadingComplete() {
      for(final ModContainer container : ModManager.this.loadedMods.values()) {
        container.state = ModState.READY;
      }
    }

    public void reset() {
      ModManager.this.loadedMods.clear();
    }
  }
}
