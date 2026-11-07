package edu.najah.stu.pricing;

import edu.najah.stu.domain.Bill;
import edu.najah.stu.service.TimeProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Map;

/**
 * Works out the delivery fee from the distance (US4.2) and picks the pricing
 * strategy for an order (US4.3).
 *
 * <p>Choice order: express delivery uses express pricing; a valid promo code
 * uses its promotion; lunch (12:00-14:00) and dinner (19:00-21:00) use peak
 * hour pricing; everything else is normal.</p>
 */
public class PricingService {

    /** Fixed part of every delivery fee, in ILS. */
    public static final BigDecimal BASE_FEE = new BigDecimal("5");

    /** Added for every kilometre, in ILS. */
    public static final BigDecimal FEE_PER_KM = new BigDecimal("2");

    private static final Map<String, Integer> PROMO_CODES = Map.of("NAJAH10", 10, "WELCOME20", 20);
    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END = LocalTime.of(14, 0);
    private static final LocalTime DINNER_START = LocalTime.of(19, 0);
    private static final LocalTime DINNER_END = LocalTime.of(21, 0);

    private final TimeProvider timeProvider;

    /**
     * Creates the service.
     *
     * @param timeProvider the clock, used to find peak hours; must not be null
     */
    public PricingService(TimeProvider timeProvider) {
        if (timeProvider == null) {
            throw new IllegalArgumentException("Time provider must not be null");
        }
        this.timeProvider = timeProvider;
    }

    /**
     * Returns the normal delivery fee: a fixed part plus a price per kilometre.
     *
     * @param distanceKm restaurant to customer, zero or more
     * @return the fee, rounded to two decimals
     */
    public BigDecimal deliveryFee(double distanceKm) {
        if (distanceKm < 0 || Double.isNaN(distanceKm)) {
            throw new IllegalArgumentException("Distance must be zero or more");
        }
        return BASE_FEE.add(FEE_PER_KM.multiply(BigDecimal.valueOf(distanceKm))).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Picks the pricing strategy for an order.
     *
     * @param express   {@code true} for express delivery
     * @param promoCode a promo code, or null or empty for none
     * @return the strategy to use
     * @throws IllegalArgumentException if the promo code is not known
     */
    public PricingStrategy choose(boolean express, String promoCode) {
        if (express) {
            return new ExpressDeliveryPricing();
        }
        if (promoCode != null && !promoCode.isBlank()) {
            Integer percent = PROMO_CODES.get(promoCode.trim().toUpperCase(Locale.ROOT));
            if (percent == null) {
                throw new IllegalArgumentException("Unknown promo code: " + promoCode.trim());
            }
            return new PromotionalPricing(percent);
        }
        return isPeakHour(timeProvider.now().toLocalTime()) ? new PeakHourPricing() : new NormalPricing();
    }

    /**
     * Makes the bill for an order.
     *
     * @param subtotal   the food price
     * @param distanceKm restaurant to customer
     * @param express    {@code true} for express delivery
     * @param promoCode  a promo code, or null for none
     * @return the bill with delivery fee and discount
     */
    public Bill bill(BigDecimal subtotal, double distanceKm, boolean express, String promoCode) {
        return choose(express, promoCode).price(subtotal, deliveryFee(distanceKm));
    }

    /**
     * Tells whether a time is at lunch or dinner time.
     *
     * @param time the time of day
     * @return {@code true} from 12:00 to 14:00 and from 19:00 to 21:00
     */
    static boolean isPeakHour(LocalTime time) {
        return within(time, LUNCH_START, LUNCH_END) || within(time, DINNER_START, DINNER_END);
    }

    private static boolean within(LocalTime time, LocalTime start, LocalTime end) {
        return !time.isBefore(start) && time.isBefore(end);
    }
}
