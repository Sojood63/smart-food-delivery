package edu.najah.stu.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Keeps a list of listeners and passes every published event to each of them
 * (the subject in the Observer pattern).
 *
 * <p>A listener that fails is logged and skipped, so one broken listener
 * never stops the others or the action that caused the event.</p>
 */
public class EventBus implements EventPublisher {

    private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());

    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Adds a listener that will receive every later event.
     *
     * @param listener the listener; must not be null
     */
    public void subscribe(EventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener must not be null");
        }
        listeners.add(listener);
    }

    @Override
    public void publish(SystemEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        for (EventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, e, () -> "A listener failed on '" + event.subject() + "'");
            }
        }
    }
}
