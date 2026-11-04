package edu.najah.stu.notification;

/**
 * Sends one plain-text email.
 */
public interface MailSender {

    /**
     * Sends an email.
     *
     * @param to      the receiver's address
     * @param subject the subject line
     * @param body    the text of the email
     * @throws MailException if the email could not be sent
     */
    void send(String to, String subject, String body);
}
