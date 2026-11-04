package edu.najah.stu.event;

/**
 * Announces system events. Services only know this interface, so tests can
 * use a mock and check which events were published.
 */
public interface EventPublisher {

    /**
     * Tells every interested listener that an event happened.
     *
     * @param event the event; must not be null
     */
    void publish(SystemEvent event);
}
