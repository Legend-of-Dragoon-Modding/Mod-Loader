package org.legendofdragoon.modloader.controls;

public final class WidgetFactory {
    private WidgetFactory() {}

    public static Widget<String> newLabel(final String text) {
        return new Widget<String>(WidgetType.Label, text, null,null, null);
    }
    public static Widget<Boolean> newButton(final String name, final Boolean current, final ValueWatcher<Boolean> onClick) {
        return new Widget<>(WidgetType.Button, name, current, null, onClick);
    }
    public static Widget<Boolean> newCheckbox(final String name, final Boolean current, final ValueWatcher<Boolean> onClick) {
        return new Widget<>(WidgetType.Checkbox, name, current, null, onClick);
    }
    public static Widget<String> newTextbox(final String name, final String current, final ValueWatcher<String> onChange) {
        return new Widget<>(WidgetType.Textbox, name, current, null, onChange);
    }
    public static Widget<String> newDropdown(final String name, final String[] options, final String current, final ValueWatcher<String> onChange) {
        return new Widget<>(WidgetType.Dropdown, name, current, new IWidgetOptions.DropDown(options), onChange);
    }
    public static Widget<Integer> newInteger(final String name, final int min, final int max, final int step, final int current, final ValueWatcher<Integer> onChange) {
        return new Widget<>(WidgetType.Numeric, name, current, new IWidgetOptions.Numeric<>(min, max, step), onChange);
    }
    public static Widget<Float> newFloat(final String name,final  float min, final float max, final float step, final float current, final ValueWatcher<Float> onChange) {
        return new Widget<>(WidgetType.Numeric, name, current, new IWidgetOptions.Numeric<>(min, max, step), onChange);
    }
}
