package edu.najah.stu.notification;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Builds real email messages without connecting to the mail server, so no network is needed.
 */
class SmtpMailSenderTest {

    private final SmtpMailSender sender = new SmtpMailSender(new SmtpSettings(MailSenders.DEFAULT_HOST,
            SmtpSettings.STARTTLS_PORT, "9a1b2c001@smtp-brevo.com", "xsmtpsib-key", "smartfood.najah@gmail.com"));

    @Test
    void messageHasSenderReceiverSubjectAndText() throws Exception {
        MimeMessage message = sender.toMessage("layan@example.com", "Welcome to Smart Food Delivery", "Hi layan_99,");

        InternetAddress from = (InternetAddress) message.getFrom()[0];
        assertEquals("smartfood.najah@gmail.com", from.getAddress());
        assertEquals(SmtpMailSender.SENDER_NAME, from.getPersonal());
        assertEquals("layan@example.com",
                ((InternetAddress) message.getRecipients(Message.RecipientType.TO)[0]).getAddress());
        assertEquals("Welcome to Smart Food Delivery", message.getSubject());
        assertEquals("Hi layan_99,", message.getContent());
    }

    @Test
    void arabicTextIsKept() throws Exception {
        MimeMessage message = sender.toMessage("layan@example.com", "أهلًا", "مطعم خميس");

        assertEquals("أهلًا", message.getSubject());
        assertEquals("مطعم خميس", message.getContent());
    }

    @Test
    void badReceiverAddressIsAMailError() {
        MailException error = assertThrows(MailException.class, () -> sender.send("a@@b.com", "Hi", "Text"));

        assertTrue(error.getMessage().contains("a@@b.com"));
    }

    @Test
    void rejectsMissingSettings() {
        assertThrows(IllegalArgumentException.class, () -> new SmtpMailSender(null));
    }
}
