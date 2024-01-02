package org.legendofdragoon.modloader.events;

import org.legendofdragoon.modloader.events.listeners.AfterEventListener;
import org.legendofdragoon.modloader.events.listeners.BeforeEventListener;
import org.legendofdragoon.modloader.events.listeners.BeforeResult;
import org.legendofdragoon.modloader.events.listeners.EventListener;

import java.util.ArrayList;
import java.util.List;

public class TestListeners {
    private static abstract class ABefore<T extends TestEvents.ATestEvent> implements BeforeEventListener<T> {
        List<Event> events = new ArrayList<>();
        @Override
        public BeforeResult Before(T event) {
            this.events.add(event);
            event.befored = true;
            return switch (event.value) {
                case 0 -> BeforeResult.Continue;
                case 1 -> BeforeResult.Handled;
                default -> BeforeResult.Return;
            };
        }
    }
    private static abstract class After<T extends TestEvents.ATestEvent> implements AfterEventListener<T> {
        List<T> events = new ArrayList<>();

        @Override
        public void After(T event) {
            this.events.add(event);
            event.aftered = true;
        }
    }

    private static  abstract class Both<T extends TestEvents.ATestEvent> implements BeforeEventListener<T>, AfterEventListener<T> {
        List<T> before = new ArrayList<>();
        List<T> after = new ArrayList<>();

        @Override
        public BeforeResult Before(T event) {
            this.before.add(event);
            event.befored = true;
            return switch (event.value) {
                case 0 -> BeforeResult.Continue;
                case 1 -> BeforeResult.Handled;
                default -> BeforeResult.Return;
            };
        }

        @Override
        public void After(T event) {
            this.after.add(event);
            event.aftered = true;
        }
    }

    @EventListener(event = TestEvents.One.class)
    public static class BeforeOne extends ABefore<TestEvents.One> {}
    @EventListener(event = TestEvents.Two.class)
    public static class BeforeTwo extends ABefore<TestEvents.Two> {}
    @EventListener(event = TestEvents.One.class)
    public static class AfterOne extends After<TestEvents.One> {}
    @EventListener(event = TestEvents.Two.class)
    public static class AfterTwo extends After<TestEvents.Two> {}
    @EventListener(event = TestEvents.One.class)
    public static class BothOne extends Both<TestEvents.One> {}
    @EventListener(event = TestEvents.Two.class)
    public static class BothTwo extends Both<TestEvents.Two> {}
}
