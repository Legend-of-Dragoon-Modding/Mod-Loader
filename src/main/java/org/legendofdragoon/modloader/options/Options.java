package org.legendofdragoon.modloader.options;

import org.legendofdragoon.modloader.controls.Widget;

import java.util.List;

public abstract class Options<T> { // Will need to implement Screen
    /**
     * The options that were loaded and will be saved.
     */
    protected T options;

    /**
     * Called after `load` and prior to the option screen is displayed.
     */
    public void setupScreen() {};
    /**
     * Called after the user leaves the option screen but before `save` is called.
     */
    public void teardownScreen() {};
    /**
     * Called each time the display needs to be displayed.
     */
    public abstract List<Widget<?>> display();

    /**
     * Creates a new instance of the options.
     * @return The new instance of the options.
     */
    public abstract T newOptions();

    /**
     * Converts the options to a byte array.
     * @return The byte array representing the options.
     */
    public byte[] save() {
        // TODO
        return null;
    }

    /**
     * Loads the options from a byte array.
     * @param data The byte array representing the options.
     */
    public void load(byte[] data) {
        this.options = this.newOptions();
        // TODO
    }
}
