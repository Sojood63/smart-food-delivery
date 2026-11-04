package edu.najah.stu.event;

/**
 * Something that happened in the system and that a person should be told about.
 *
 * <p>Each kind of event is a small record that knows who to tell and what to
 * say. Listeners such as the email sender handle every event the same way, so
 * a new kind of email only needs a new record (Observer pattern,
 * Open/Closed principle).</p>
 */
public interface SystemEvent {

    /**
     * Returns the email address of the person to tell.
     *
     * @return the receiver's email address
     */
    String recipientEmail();

    /**
     * Returns the name used to greet the person.
     *
     * @return the receiver's name
     */
    String recipientName();

    /**
     * Returns a short title for the event, used as the email subject.
     *
     * @return the subject
     */
    String subject();

    /**
     * Returns what happened, in a few plain sentences.
     *
     * @return the message, without greeting or signature
     */
    String message();
}
