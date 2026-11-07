package edu.najah.stu.repository;

import edu.najah.stu.domain.Driver;
import edu.najah.stu.domain.Location;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps drivers in memory while the program runs.
 */
public class InMemoryDriverRepository implements DriverRepository {

    /** The drivers by id. */
    private final Map<String, Driver> drivers = new LinkedHashMap<>();

    /**
     * Creates a repository with five demo drivers around Nablus.
     *
     * @return the repository
     */
    public static InMemoryDriverRepository withDemoDrivers() {
        InMemoryDriverRepository repository = new InMemoryDriverRepository();
        repository.save(new Driver("D1", "Ahmad", new Location(32.2215, 35.2420), 15));
        repository.save(new Driver("D2", "Khaled", new Location(32.2250, 35.2290), 35));
        repository.save(new Driver("D3", "Omar", new Location(32.2140, 35.2600), 35));
        repository.save(new Driver("D4", "Yousef", new Location(32.2300, 35.2200), 30));
        repository.save(new Driver("D5", "Rami", new Location(32.2200, 35.2450), 30));
        return repository;
    }

    @Override
    public void save(Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver must not be null");
        }
        drivers.put(driver.getId(), driver);
    }

    @Override
    public List<Driver> findAll() {
        return List.copyOf(drivers.values());
    }
}
