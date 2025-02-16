package org.legendofdragoon.modloader.i18n;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.ModContainer;
import org.legendofdragoon.modloader.ModManager;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

public class LangManager {
  private static final Logger LOGGER = LogManager.getFormatterLogger(LangManager.class);

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
          final URL langUrl = mod.getLangResource(locale);

          if(langUrl != null) {
            this.loadLang(langUrl);
          }
        } catch(final IOException e) {
          LOGGER.warn("Failed to load %s for mod %d", locale, mod.modId);
        }
      }
    }

    public void loadLang(final URL langUrl) throws IOException {
      final Map<String, String> lang = new HashMap<>();
      final Properties properties = new Properties();
      properties.load(langUrl.openStream());

      for(final Object key : properties.keySet()) {
        lang.put((String)key, properties.getProperty((String)key));
      }

      this.loadLang(lang);
    }

    public void loadLang(final Path path) throws IOException {
      final Map<String, String> lang = new HashMap<>();
      final Properties properties = new Properties();
      properties.load(Files.newInputStream(path));

      for(final Object key : properties.keySet()) {
        lang.put((String)key, properties.getProperty((String)key));
      }

      this.loadLang(lang);
    }

    public void loadLang(final Map<String, String> lang) {
      LangManager.this.translations.putAll(lang);
    }
    /**
     * Searches for a lang file in the following order:
     * <ol>
     *   <li>Full locale code (see {@link Locale#toString()}</li>
     *   <li>lang_COUNTRY (e.g. en_US)</li>
     *   <li>lang (e.g. en)</li>
     *   <li>en (fallback)</li>
     *   <li>null (if en isn't found)</li>
     * </ol>
     */
    public Path getLangPath(final Path directory, final Locale locale) {
      final Path specific = directory.resolve("%s.lang".formatted(locale));

      if(Files.exists(specific)) {
        return specific;
      }

      final Path langCountry = directory.resolve("%s_%s.lang".formatted(locale.getLanguage(), locale.getCountry()));

      if(Files.exists(langCountry)) {
        return langCountry;
      }

      final Path lang = directory.resolve("%s.lang".formatted(locale.getLanguage()));

      if(Files.exists(lang)) {
        return lang;
      }

      final Path fallback = directory.resolve("en.lang");

      if(Files.exists(fallback)) {
        return fallback;
      }

      return null;
    }

    public void reset() {
      LangManager.this.translations.clear();
    }
  }
}
