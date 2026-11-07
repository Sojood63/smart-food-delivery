package edu.najah.stu.delivery;

import edu.najah.stu.domain.Driver;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.Order;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Chooses the driver who is closest to the restaurant.
 */
public class NearestDriverStrategy implements DriverSelectionStrategy {

    @Override
    public String name() {
        return "Nearest driver";
    }

    @Override
    public Optional<Driver> choose(List<Driver> eligible, Order order) {
        Location restaurant = order.getRestaurant().getLocation().orElseThrow();
        return eligible.stream()
                .min(Comparator.comparingDouble((Driver driver) -> driver.getLocation().distanceKmTo(restaurant))
                        .thenComparing(Driver::getId));
    }
}
