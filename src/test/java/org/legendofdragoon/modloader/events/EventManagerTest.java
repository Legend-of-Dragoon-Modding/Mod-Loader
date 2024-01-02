package org.legendofdragoon.modloader.events;

import org.legendofdragoon.modloader.events.listeners.BeforeResult;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventManagerTest {
    @Test
    void registerListener_before() {
        // Setup
        final var m = new EventManager();
        final var l = new TestListeners.BeforeOne();
        // Test
        m.registerListener(l);
        // Verify
        var result = m.getListeners(TestEvents.One.class);
        assertNotNull(result);
        assertEquals(1, result.getBeforeListeners().size());
        assertEquals(0, result.getAfterListeners().size());
    }

    @Test
    void registerListener_after() {
        // Setup
        final var m = new EventManager();
        final var l = new TestListeners.AfterOne();
        // Test
        m.registerListener(l);
        // Verify
        var result = m.getListeners(TestEvents.One.class);
        assertNotNull(result);
        assertEquals(0, result.getBeforeListeners().size());
        assertEquals(1, result.getAfterListeners().size());
    }

    @Test
    void registerListener_both() {
        // Setup
        final var m = new EventManager();
        final var l = new TestListeners.BothOne();
        // Test
        m.registerListener(l);
        // Verify
        var result = m.getListeners(TestEvents.One.class);
        assertNotNull(result);
        assertEquals(1, result.getBeforeListeners().size());
        assertEquals(1, result.getAfterListeners().size());
    }

    @Test
    void registerListener_both_multiple() {
        // Setup
        final var m = new EventManager();
        final var l1 = new TestListeners.BothOne();
        final var l2 = new TestListeners.BothOne();
        // Test
        m.registerListener(l1);
        m.registerListener(l2);
        // Verify
        var result = m.getListeners(TestEvents.One.class);
        assertNotNull(result);
        assertEquals(2, result.getBeforeListeners().size());
        assertEquals(2, result.getAfterListeners().size());
    }

    @Test
    void postEvent_continue() {
        // Setup
        final var m = new EventManager();
        final var l1 = new TestListeners.BothOne();
        final var l2 = new TestListeners.BothOne();
        final var l3 = new TestListeners.BothTwo();
        final var event = new TestEvents.One(BeforeResult.Continue.ordinal());
        final var defaultListener = new TestListeners.AfterOne();
        m.registerListener(l1);
        m.registerListener(l2);
        m.registerListener(l3);

        // Test
        m.postEvent(event, defaultListener);

        // Verify
        assertTrue(event.befored);
        assertTrue(event.aftered);
        assertEquals(1, defaultListener.events.size());
        assertEquals(1, l1.before.size());
        assertEquals(1, l1.after.size());
        assertEquals(1, l2.before.size());
        assertEquals(1, l2.after.size());
        assertEquals(0, l3.before.size());
        assertEquals(0, l3.after.size());
    }

    @Test
    void postEvent_handled() {
        // Setup
        final var m = new EventManager();
        final var l1 = new TestListeners.BothOne();
        final var l2 = new TestListeners.BothOne();
        final var l3 = new TestListeners.BothTwo();
        final var event = new TestEvents.One(BeforeResult.Handled.ordinal());
        final var defaultListener = new TestListeners.AfterOne();
        m.registerListener(l1);
        m.registerListener(l2);
        m.registerListener(l3);

        // Test
        m.postEvent(event, defaultListener);

        // Verify
        assertTrue(event.befored);
        assertTrue(event.aftered);
        assertEquals(0, defaultListener.events.size());
        assertEquals(1, l1.before.size());
        assertEquals(1, l1.after.size());
        assertEquals(0, l2.before.size());
        assertEquals(1, l2.after.size());
        assertEquals(0, l3.before.size());
        assertEquals(0, l3.after.size());
    }

    @Test
    void postEvent_return() {
        // Setup
        final var m = new EventManager();
        final var l1 = new TestListeners.BothOne();
        final var l2 = new TestListeners.BothOne();
        final var l3 = new TestListeners.BothTwo();
        final var event = new TestEvents.One(BeforeResult.Return.ordinal());
        final var defaultListener = new TestListeners.AfterOne();
        m.registerListener(l1);
        m.registerListener(l2);
        m.registerListener(l3);

        // Test
        m.postEvent(event, defaultListener);

        // Verify
        assertTrue(event.befored);
        assertFalse(event.aftered);
        assertEquals(0, defaultListener.events.size());
        assertEquals(1, l1.before.size());
        assertEquals(0, l1.after.size());
        assertEquals(0, l2.before.size());
        assertEquals(0, l2.after.size());
        assertEquals(0, l3.before.size());
        assertEquals(0, l3.after.size());
    }

    @Test
    void clearStaleRefs() {
    }
}