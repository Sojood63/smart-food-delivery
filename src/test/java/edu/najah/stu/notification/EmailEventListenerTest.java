package edu.najah.stu.notification;

import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.event.AccountEvent;
import edu.najah.stu.event.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * Every event becomes one email. The mail sender is a Mockito mock, so nothing is really sent.
 */
@ExtendWith(MockitoExtension.class)
class EmailEventListenerTest {

    private static final String BODY = "Hi layan_99,\n\n"
            + "Your customer account is ready. Log in to find restaurants that are open now"
            + " near An-Najah, with their distance and walking time."
            + EmailEventListener.SIGNATURE;

    @Mock
    private MailSender mailSender;

    private final User layan = new User("layan_99", "pbkdf2$1$c2FsdA==$aGFzaA==", Role.CUSTOMER, null,
            "layan@example.com");

    @Test
    void rejectsMissingSender() {
        assertThrows(IllegalArgumentException.class, () -> new EmailEventListener(null));
    }

    @Test
    void sendsOneEmailWithGreetingMessageAndSignature() {
        new EmailEventListener(mailSender).onEvent(AccountEvent.registered(layan));

        verify(mailSender).send("layan@example.com", "Welcome to Smart Food Delivery", BODY);
    }

    @Test
    void worksAsAnObserverOfTheEventBus() {
        record Email(String to, String subject, String body) {
        }
        List<Email> sent = new ArrayList<>();
        EventBus bus = new EventBus();
        bus.subscribe(new EmailEventListener((to, subject, body) -> sent.add(new Email(to, subject, body))));

        bus.publish(AccountEvent.registered(layan));

        assertEquals(1, sent.size());
        assertEquals("layan@example.com", sent.get(0).to());
        assertTrue(sent.get(0).body().endsWith("Smart Food Delivery - Nablus"));
    }

    @Test
    void aFailedEmailDoesNotStopTheProgram() {
        EmailEventListener listener = new EmailEventListener((to, subject, body) -> {
            throw new MailException("Could not send email to " + to, new IllegalStateException("offline"));
        });
        AccountEvent event = AccountEvent.registered(layan);

        assertDoesNotThrow(() -> listener.onEvent(event));
    }
}
