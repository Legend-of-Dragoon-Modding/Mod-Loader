package org.legendofdragoon.modloader.i18n;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.ModContainer;
import org.legendofdragoon.modloader.ModManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class LangManager {
  private static final Logger LOGGER = LogManager.getFormatterLogger();

  private final Map<String, String> translations = new HashMap<>();

  public LangManager(final Consumer<Access> access) {
    access.accept(new Access());
  }

  public String getTranslation(final String key) {
    return this.translations.getOrDefault(key, key).formatted();
  }

  public String getTranslation(final String key, final Object... args) {
    return this.translations.getOrDefault(key, key).formatted(args);
  }

  public class Access {
    private Access() { }

    public void initialize(final ModManager mods, final Locale locale) {
      for(final ModContainer mod : mods.getLoadedMods()) {
        try {
          LangManager.this.translations.putAll(mod.loadLang(locale));
        } catch(final IOException e) {
          LOGGER.warn("Failed to load %s for mod %d", locale, mod.modId);
        }
      }
    }

    public void reset() {
      LangManager.this.translations.clear();
    }
  }
}
