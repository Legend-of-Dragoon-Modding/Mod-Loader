package org.legendofdragoon.modloader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class ModContainer {
  public final String modId;
  public final Object mod;
  public final ClassLoader classLoader;
  ModState state = ModState.INITIALIZED;

  /** Set to the mod that owns the event handler that is currently executing (null for internal mods) */
  private static ModContainer activeMod;

  public ModContainer(final String modId, final Object mod) {
    this.modId = modId;
    this.mod = mod;
    this.classLoader = mod.getClass().getClassLoader();
  }

  public ClassLoader getClassLoader() {
    return this.classLoader;
  }

  public URL getResource(final String path) {
    return this.classLoader.getResource(this.modId + '/' + path);
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
  private URL getLangResource(final Locale locale) {
    final URL specific = this.getResource("lang/%s.lang".formatted(locale));

    if(specific != null) {
      return specific;
    }

    final URL langCountry = this.getResource("lang/%s_%s.lang".formatted(locale.getLanguage(), locale.getCountry()));

    if(langCountry != null) {
      return langCountry;
    }

    final URL lang = this.getResource("lang/%s.lang".formatted(locale.getLanguage()));

    if(lang != null) {
      return lang;
    }

    return this.getResource("lang/en.lang");
  }

  public Map<String, String> loadLang(final Locale locale) throws IOException {
    final URL langUrl = this.getLangResource(locale);

    if(langUrl == null) {
      return Map.of();
    }

    final Map<String, String> lang = new HashMap<>();
    final Properties properties = new Properties();
    properties.load(langUrl.openStream());

    for(final Object key : properties.keySet()) {
      lang.put((String)key, properties.getProperty((String)key));
    }

    return lang;
  }

  public static void setActiveMod(@Nullable final ModContainer mod) {
    activeMod = mod;
  }

  public static ModContainer getActiveMod() {
    return activeMod;
  }
}
