package org.legendofdragoon.modloader.events;

public class TestEvents {
    public static abstract class ATestEvent extends Event {
        public int value;
        public int befored;
        public int aftered;
        public int defaulted;
        public ATestEvent(int ...value) {
            super();
            if (value.length > 0) {
                this.value = value[0];
            }
        }
    }

    public static class One extends ATestEvent {
        public One(int ...value) {
            super(value);
        }
    }

    public static class Two extends ATestEvent {
        public Two(int ...value) {
            super(value);
        }
    }
}