package org.legendofdragoon.modloader.controls;

@FunctionalInterface
public interface ValueWatcher<T> {
    void onChange(T value);
}
