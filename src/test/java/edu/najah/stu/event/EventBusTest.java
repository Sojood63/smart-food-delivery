package edu.najah.stu.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The subject of the Observer pattern: every listener hears every event.
 */
class EventBusTest {

    private record TestEvent(String subject) implements SystemEvent {

        @Override
        public String recipientEmail() {
            return "a@b.co";
        }

        @Override
        public String recipientName() {
            return "tester";
        }

        @Override
        public String message() {
            return "Something happened.";
        }
    }

    private final EventBus bus = new EventBus();

    @Test
    void everyListenerHearsEveryEventInOrder() {
        List<String> heard = new ArrayList<>();
        bus.subscribe(event -> heard.add("first: " + event.subject()));
        bus.subscribe(event -> heard.add("second: " + event.subject()));

        bus.publish(new TestEvent("A"));
        bus.publish(new TestEvent("B"));

        assertEquals(List.of("first: A", "second: A", "first: B", "second: B"), heard);
    }

    @Test
    void publishingWithNoListenersDoesNothing() {
        TestEvent event = new TestEvent("A");

        assertDoesNotThrow(() -> bus.publish(event));
    }

    @Test
    void aBrokenListenerDoesNotStopTheOthers() {
        List<String> heard = new ArrayList<>();
        bus.subscribe(event -> {
            throw new IllegalStateException("broken");
        });
        bus.subscribe(event -> heard.add(event.subject()));

        bus.publish(new TestEvent("A"));

        assertEquals(List.of("A"), heard);
    }

    @Test
    void rejectsMissingListenerOrEvent() {
        assertThrows(IllegalArgumentException.class, () -> bus.subscribe(null));
        assertThrows(IllegalArgumentException.class, () -> bus.publish(null));
    }
}
