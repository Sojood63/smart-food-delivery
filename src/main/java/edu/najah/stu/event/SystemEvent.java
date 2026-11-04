package edu.najah.stu.event;

import edu.najah.stu.domain.User;

/**
 * Something that happened in the system and that a person should be told about.
 *
 * <p>Each event knows who to tell and what to say. Listeners such as the
 * email sender handle every event the same way, so a new kind of email only
 * needs a new factory method (Observer pattern, Open/Closed principle).</p>
 */
public interface SystemEvent {

    /**
     * Returns the person to tell.
     *
     * @return the receiver
     */
    User recipient();

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
