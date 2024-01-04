package org.legendofdragoon.modloader.mods;

import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

public class ModContainer {
  public final String modId;
  public final Class<?> mod;
  public final Reflections reflections;
  ModState state = ModState.INITIALIZED;
  public final ClassLoader classLoader;
  public final URL url;

  public ModContainer(String modId, Class<?> modClass, Reflections reflections, ClassLoader classLoader, URL url) {
    this.modId = modId;
    this.mod = modClass;
    this.reflections = reflections;
    this.classLoader = classLoader;
    this.url = url;
  }

  public URL getResource(final String path) {
    return this.mod.getClassLoader().getResource(this.modId + '/' + path);
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

  public Object Instance() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    return this.mod.getDeclaredConstructor().newInstance();
  }
}
