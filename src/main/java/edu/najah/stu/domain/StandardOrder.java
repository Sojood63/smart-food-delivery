package edu.najah.stu.domain;

import java.time.LocalDateTime;

/**
 * A normal order, delivered as soon as possible with the normal policies.
 */
public final class StandardOrder implements OrderType {

    @Override
    public String name() {
        return "Standard";
    }

    @Override
    public void validate(LocalDateTime now) {
        // a standard order only follows the normal delivery policies
    }
}
