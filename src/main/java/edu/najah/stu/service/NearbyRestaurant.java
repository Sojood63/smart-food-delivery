package edu.najah.stu.service;

import edu.najah.stu.domain.Restaurant;

/**
 * One result of a nearby-restaurant search.
 *
 * @param restaurant     the open restaurant that was found
 * @param distanceKm     straight-line distance from the search point, in kilometres
 * @param walkingMinutes estimated walking time in whole minutes, at least 1
 * @param closingSoon    {@code true} if the restaurant closes in under 30 minutes
 */
public record NearbyRestaurant(Restaurant restaurant, double distanceKm,
                               long walkingMinutes, boolean closingSoon) {
}
