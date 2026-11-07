package edu.najah.stu.repository;

import edu.najah.stu.domain.Driver;

import java.util.List;

/**
 * Stores the delivery drivers.
 */
public interface DriverRepository {

    /**
     * Saves a driver, replacing any driver with the same id.
     *
     * @param driver the driver; must not be null
     */
    void save(Driver driver);

    /**
     * Returns all drivers.
     *
     * @return an unmodifiable list
     */
    List<Driver> findAll();
}
