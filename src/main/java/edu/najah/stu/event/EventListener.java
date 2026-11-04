package edu.najah.stu.event;

/**
 * Reacts to system events, for example by sending an email (the observer).
 */
public interface EventListener {

    /**
     * Handles one event.
     *
     * @param event the event that happened
     */
    void onEvent(SystemEvent event);
}
