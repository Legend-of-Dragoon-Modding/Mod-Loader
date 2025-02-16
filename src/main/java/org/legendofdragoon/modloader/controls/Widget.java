package org.legendofdragoon.modloader.controls;

public record Widget<T>(WidgetType type, String name, T currentValue, IWidgetOptions options, ValueWatcher<T> valueWatcher) { }
