package org.legendofdragoon.modloader.events;

import org.legendofdragoon.modloader.events.listeners.Result;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventManagerTest {
    @Test
    void registerListener_before() {
        // Setup
        final var m = new EventManager();
        final var l = new TestListeners.Before();
        // Test
        m.registerListeners("id", l.getClass(), l);
        // Verify
        var result = m.getListeners(TestEvents.One.class);
        assertNotNull(result);
        assertEquals(1, result.getBeforeListeners().size());
        assertEquals(0, result.getAfterListeners().size());

        result = m.getListeners(TestEvents.Two.class);
        assertNotNull(result);
        assertEquals(1, result.getBeforeListeners().size());
        assertEquals(0, result.getAfterListeners().size());
    }

    @Test
    void registerListener_after() {
        // Setup
        final var m = new EventManager();
        final var l = new TestListeners.After();
        // Test
        m.registerListeners("id", l.getClass(), l);
        // Verify
        var result = m.getListeners(TestEvents.One.class);
        assertNotNull(result);
        assertEquals(0, result.getBeforeListeners().size());
        assertEquals(1, result.getAfterListeners().size());

        result = m.getListeners(TestEvents.Two.class);
        assertNotNull(result);
        assertEquals(0, result.getBeforeListeners().size());
        assertEquals(1, result.getAfterListeners().size());
    }

    @Test
    void registerListener_both() {
        // Setup
        final var m = new EventManager();
        final var l = new TestListeners.Both();
        // Test
        m.registerListeners("id", l.getClass(), l);
        // Verify
        var result = m.getListeners(TestEvents.One.class);
        assertNotNull(result);
        assertEquals(1, result.getBeforeListeners().size());
        assertEquals(1, result.getAfterListeners().size());

        result = m.getListeners(TestEvents.Two.class);
        assertNotNull(result);
        assertEquals(1, result.getBeforeListeners().size());
        assertEquals(1, result.getAfterListeners().size());
    }
    @Test
    void registerListener_same() {
        // Setup
        final var m = new EventManager();
        final var l = new TestListeners.MultipleSameEvent();
        // Test
        m.registerListeners("id", l.getClass(), l);
        // Verify
        var result = m.getListeners(TestEvents.One.class);
        assertNotNull(result);
        assertEquals(2, result.getBeforeListeners().size());
        assertEquals(2, result.getAfterListeners().size());

        result = m.getListeners(TestEvents.Two.class);
        assertNull(result);
    }

    @Test
    void postEvent_continue() {
        // Setup
        final var m = new EventManager();
        final var l1 = new TestListeners.Before();
        final var l2 = new TestListeners.After();
        final var l3 = new TestListeners.Both();
        final var event = new TestEvents.One(Result.CONTINUE.ordinal());
        m.registerListeners("id", l1.getClass(), l1);
        m.registerListeners("id", l2.getClass(), l2);
        m.registerListeners("id", l3.getClass(), l3);

        // Test
        m.postEvent(event, TestListeners::defaultLogic);

        // Verify
        assertTrue(1 <= event.befored);
        assertEquals(1, event.defaulted);
        assertTrue(1 <= event.aftered);
        assertEquals(1, l1.events.size());
        assertEquals(1, l2.events.size());
        assertEquals(1, l3.before.size());
        assertEquals(1, l3.after.size());
    }

    @Test
    void postEvent_handled() {
        // Setup
        final var m = new EventManager();
        final var l1 = new TestListeners.Before();
        final var l2 = new TestListeners.After();
        final var l3 = new TestListeners.Both();
        final var l4 = new TestListeners.MultipleSameEvent();
        final var event = new TestEvents.One(Result.HANDLED.ordinal());
        m.registerListeners("id", l1.getClass(), l1);
        m.registerListeners("id", l2.getClass(), l2);
        m.registerListeners("id", l3.getClass(), l3);
        m.registerListeners("id", l4.getClass(), l4);

        // Test
        m.postEvent(event, TestListeners::defaultLogic);

        // Verify
        assertTrue(1 <= event.befored);
        assertEquals(0, event.defaulted);
        assertTrue(1 <= event.aftered);
        assertEquals(1, l1.events.size());
        assertEquals(1, l2.events.size());
        assertEquals(0, l3.before.size());
        assertEquals(1, l3.after.size());
        assertEquals(0, l4.before.size());
        assertEquals(2, l4.after.size());
    }

    @Test
    void postEvent_return() {
        // Setup
        final var m = new EventManager();
        final var l1 = new TestListeners.Before();
        final var l2 = new TestListeners.After();
        final var l3 = new TestListeners.Both();
        final var l4 = new TestListeners.MultipleSameEvent();
        final var event = new TestEvents.One(Result.CANCEL.ordinal());
        m.registerListeners("id", l1.getClass(), l1);
        m.registerListeners("id", l2.getClass(), l2);
        m.registerListeners("id", l3.getClass(), l3);
        m.registerListeners("id", l4.getClass(), l4);

        // Test
        m.postEvent(event, TestListeners::defaultLogic);

        // Verify
        assertTrue(1 <= event.befored);
        assertEquals(0, event.defaulted);
        assertEquals(0 , event.aftered);
        assertEquals(1, l1.events.size());
        assertEquals(0, l2.events.size());
        assertEquals(0, l3.before.size());
        assertEquals(0, l3.after.size());
        assertEquals(0, l4.before.size());
        assertEquals(0, l4.after.size());
    }

    @Test
    void postEvent_handled_withNoReturnBefore() {
        // Setup
        final var m = new EventManager();
        final var l1 = new TestListeners.Before();
        final var l2 = new TestListeners.NoReturnBefore();
        final var event = new TestEvents.One(Result.HANDLED.ordinal());
        m.registerListeners("id", l1.getClass(), l1);
        m.registerListeners("id", l2.getClass(), l2);

        // Test
        m.postEvent(event, TestListeners::defaultLogic);

        // Verify
        assertTrue(1 <= event.befored);
        assertEquals(0, event.defaulted);
        assertEquals(0, event.aftered);
        assertEquals(1, l1.events.size());
        assertEquals(1, l2.events.size());
    }

    @Test
    void postEvent_exception() {
        // Setup
        final var m = new EventManager();
        final var l1 = new TestListeners.Both();
        final var l2 = new TestListeners.ExceptionListeners();
        final var event1 = new TestEvents.One(Result.CONTINUE.ordinal());
        final var event2 = new TestEvents.One(Result.CONTINUE.ordinal());
        m.registerListeners("id", l1.getClass(), l1);
        m.registerListeners("id", l2.getClass(), l2);

        // Test
        m.postEvent(event1, TestListeners::defaultLogic);
        m.postEvent(event2, TestListeners::defaultLogic);

        // Verify
        assertEquals(2, event1.befored);
        assertEquals(1, event1.defaulted);
        assertEquals(2, event1.aftered);
        assertEquals(1, event2.befored);
        assertEquals(1, event2.defaulted);
        assertEquals(1, event2.aftered);
        assertEquals(2, l1.before.size());
        assertEquals(2, l1.after.size());
    }

    @Test
    void postEvent_deregister() {
        // Setup
        final var m = new EventManager();
        final var l1 = new TestListeners.Both();
        final var l2 = new TestListeners.DeregisterListener();
        final var event1 = new TestEvents.One(Result.CONTINUE.ordinal());
        final var event2 = new TestEvents.One(Result.CONTINUE.ordinal());
        m.registerListeners("id", l1.getClass(), l1);
        m.registerListeners("id", l2.getClass(), l2);

        // Test
        m.postEvent(event1, TestListeners::defaultLogic);
        m.postEvent(event2, TestListeners::defaultLogic);

        // Verify
        assertEquals(2, event1.befored);
        assertEquals(1, event1.defaulted);
        assertEquals(1, event1.aftered);
        assertEquals(1, event2.befored);
        assertEquals(1, event2.defaulted);
        assertEquals(1, event2.aftered);
        assertEquals(2, l1.before.size());
        assertEquals(2, l1.after.size());
    }
}