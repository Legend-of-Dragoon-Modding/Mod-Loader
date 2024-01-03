package org.legendofdragoon.modloader.events;

import org.legendofdragoon.modloader.events.listeners.*;
import org.legendofdragoon.modloader.events.listeners.Result;

import java.util.ArrayList;
import java.util.List;

public class TestListeners {
    static <T extends TestEvents.ATestEvent> Result before(T event, List events) {
        events.add(event);
        event.befored++;
        return switch (event.value) {
            case 0 -> Result.CONTINUE;
            case 1 -> Result.HANDLED;
            default -> Result.CANCEL;
        };
    }

    static <T extends TestEvents.ATestEvent> void after(T event, List events) {
        events.add(event);
        event.aftered++;
    }

    public static <T extends Event> void defaultLogic(T event) {
        if (event instanceof TestEvents.ATestEvent e) {
            e.defaulted++;
        }
    }

    public static class Before {
        final List<Event> events = new ArrayList<>();

        @EventListener(event = TestEvents.One.class, kind = Kind.BEFORE)
        public Result one(TestEvents.One event) {
            return TestListeners.before(event, this.events);
        }

        @EventListener(event = TestEvents.Two.class, kind = Kind.BEFORE)
        public Result two(TestEvents.Two event) {
            return TestListeners.before(event, this.events);
        }
    }

    public static class After {
        final List<TestEvents.One> events = new ArrayList<>();

        @EventListener(event = TestEvents.One.class, kind = Kind.AFTER)
        public void one(TestEvents.One event) {
            TestListeners.after(event, events);
        }

        @EventListener(event = TestEvents.Two.class, kind = Kind.AFTER)
        public void two(TestEvents.Two event) {
            TestListeners.after(event, events);
        }
    }

    public static class Both {
        List<Event> before = new ArrayList<>();
        List<Event> after = new ArrayList<>();

        @EventListener(event = TestEvents.One.class, kind = Kind.BEFORE)
        public Result beforeOne(TestEvents.One event) {
            return TestListeners.before(event, this.before);
        }

        @EventListener(event = TestEvents.One.class, kind = Kind.AFTER)
        public void afterOne(TestEvents.One event) {
            TestListeners.after(event, this.after);
        }

        @EventListener(event = TestEvents.Two.class, kind = Kind.BEFORE)
        public Result beforeTwo(TestEvents.Two event) {
            return TestListeners.before(event, this.before);
        }

        @EventListener(event = TestEvents.Two.class, kind = Kind.AFTER)
        public void afterTwo(TestEvents.Two event) {
            TestListeners.after(event, this.after);
        }
    }

    public static class SameBoth {
        List<Event> before = new ArrayList<>();
        List<Event> after = new ArrayList<>();

        @EventListener(event = TestEvents.One.class, kind = Kind.BEFORE)
        public Result before1(TestEvents.One event) {
            return TestListeners.before(event, this.before);
        }

        @EventListener(event = TestEvents.One.class, kind = Kind.AFTER)
        public void after1(TestEvents.One event) {
            TestListeners.after(event, this.after);
        }

        @EventListener(event = TestEvents.One.class, kind = Kind.BEFORE)
        public Result before2(TestEvents.One event) {
            return TestListeners.before(event, this.before);
        }

        @EventListener(event = TestEvents.One.class, kind = Kind.AFTER)
        public void after2(TestEvents.One event) {
            TestListeners.after(event, this.after);
        }
    }

    public static class NoReturnBefore {
        final List<Event> events = new ArrayList<>();

        @EventListener(event = TestEvents.One.class, kind = Kind.BEFORE, priority = Priority.First)
        public void voidReturn(TestEvents.One event) {
            TestListeners.before(event, this.events);
        }
    }
}
