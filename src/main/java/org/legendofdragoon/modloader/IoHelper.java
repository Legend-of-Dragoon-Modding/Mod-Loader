package org.legendofdragoon.modloader;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class IoHelper {
  private IoHelper() { }

  public static List<String> findJarResources(final Class<?> cls, final String path) {
    final List<String> found = new ArrayList<>();

    final Path jarPath = Path.of(cls.getProtectionDomain().getCodeSource().getLocation().getPath());

    if(Files.isDirectory(jarPath)) {
      try(final DirectoryStream<Path> files = Files.newDirectoryStream(jarPath.getParent().resolve("resources").resolve(path))) {
        for(final Path file : files) {
          found.add(file.getFileName().toString());
        }
      } catch(final IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      try(final JarFile jarFile = new JarFile(jarPath.toString())) {
        for(final Enumeration<JarEntry> em = jarFile.entries(); em.hasMoreElements(); ) {
          final String s = em.nextElement().toString();

          if(s.startsWith(path)) {
            found.add(s.substring(s.lastIndexOf('/') + 1));
          }
        }
      } catch(final IOException e) {
        throw new RuntimeException(e);
      }
    }

    return found;
  }
}
