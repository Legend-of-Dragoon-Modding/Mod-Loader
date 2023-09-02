package org.legendofdragoon.modloader.registries;

public class RegistryEntry {
  private RegistryId id;

  void setRegistryId(final RegistryId id) {
    this.id = id;
  }

  public RegistryId getRegistryId() {
    return this.id;
  }
}
