package org.legendofdragoon.modloader.registries;

public class RegistryEntry {
  private Registry<?> registry;
  private RegistryId id;

  void setRegistry(final Registry<?> registry, final RegistryId id) {
    this.registry = registry;
    this.id = id;
  }

  public RegistryId getRegistryId() {
    return this.id;
  }

  public String getTranslationKey() {
    return this.registry.id.modId() + '.' + this.registry.id.entryId() + '.' + this.id.entryId() + ".name";
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + ' ' + this.id;
  }
}
