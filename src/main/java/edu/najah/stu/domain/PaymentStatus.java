package edu.najah.stu.domain;

/**
 * Whether an order has been paid (US4.4).
 */
public enum PaymentStatus {

    /** Nothing paid yet; cash is paid on delivery. */
    UNPAID,

    /** Paid in full. */
    PAID,

    /** A payment was tried and rejected. */
    REJECTED
}
