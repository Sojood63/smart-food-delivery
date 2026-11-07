package edu.najah.stu.domain;

import java.time.LocalDateTime;

/**
 * A priority order; it always pays the express delivery fee.
 */
public final class ExpressOrder implements OrderType {

    @Override
    public String name() {
        return "Express";
    }

    @Override
    public void validate(LocalDateTime now) {
        // the extra fee is added by the express pricing strategy
    }

    @Override
    public boolean isExpress() {
        return true;
    }
}
