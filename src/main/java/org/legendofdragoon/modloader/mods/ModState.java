package org.legendofdragoon.modloader.mods;

public enum ModState {
  INITIALIZED,
  READY,
  ;

  public boolean isReady() {
    return this == READY;
  }
}
