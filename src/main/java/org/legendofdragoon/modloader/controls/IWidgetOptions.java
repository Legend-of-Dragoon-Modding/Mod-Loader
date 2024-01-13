package org.legendofdragoon.modloader.controls;

public interface IWidgetOptions {
    record DropDown(String[] options) implements IWidgetOptions { }
    record Numeric<T extends Number> (T min, T max, T step) implements IWidgetOptions { }
}
