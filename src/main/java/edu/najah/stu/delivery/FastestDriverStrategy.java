package edu.najah.stu.delivery;

import edu.najah.stu.domain.Driver;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.Order;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Chooses the driver who would bring the food to the customer soonest,
 * counting the vehicle's speed, not only the distance.
 */
public class FastestDriverStrategy implements DriverSelectionStrategy {

    @Override
    public String name() {
        return "Fastest driver";
    }

    @Override
    public Optional<Driver> choose(List<Driver> eligible, Order order) {
        Location restaurant = order.getRestaurant().getLocation().orElseThrow();
        Location customer = order.getAddress().location();
        return eligible.stream()
                .min(Comparator.comparingLong((Driver driver) -> driver.minutesToDeliver(restaurant, customer))
                        .thenComparing(Driver::getId));
    }
}
