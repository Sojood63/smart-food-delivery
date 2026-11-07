package edu.najah.stu.domain;

/**
 * Whether a driver can take a new order (US3.1).
 */
public enum DriverStatus {

    /** Free to take an order. */
    AVAILABLE,

    /** Delivering an order. */
    BUSY
}
