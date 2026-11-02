package edu.najah.stu.domain;

/**
 * Where a restaurant's opening hours came from, from most to least trusted.
 */
public enum HoursSource {

    /** Collected by the team or set by a manager; always wins. */
    MANUAL,

    /** Taken from OpenStreetMap. */
    OSM,

    /** Guessed from the kind of restaurant; shown to customers as estimated. */
    ESTIMATED,

    /** Not known; the restaurant is never treated as open. */
    UNKNOWN
}
