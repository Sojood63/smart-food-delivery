package edu.najah.stu.pricing;

import edu.najah.stu.domain.Bill;
import edu.najah.stu.domain.ExpressOrder;
import edu.najah.stu.domain.StandardOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Delivery cost (US4.2) and the pricing strategies (US4.3).
 */
class PricingTest {

    private static final BigDecimal FOOD = new BigDecimal("60");
    private static final BigDecimal FEE = new BigDecimal("8");

    private static PricingService at(int hour, int minute) {
        return new PricingService(() -> LocalDateTime.of(2026, 11, 2, hour, minute));
    }

    @Test
    void deliveryFeeGrowsWithDistance() {
        PricingService pricing = at(16, 0);

        assertEquals(new BigDecimal("5.00"), pricing.deliveryFee(0));
        assertEquals(new BigDecimal("10.00"), pricing.deliveryFee(2.5));
        assertEquals(new BigDecimal("7.47"), pricing.deliveryFee(1.234));
        assertThrows(IllegalArgumentException.class, () -> pricing.deliveryFee(-1));
        assertThrows(IllegalArgumentException.class, () -> pricing.deliveryFee(Double.NaN));
    }

    @Test
    void normalPricingAddsTheFee() {
        Bill bill = new NormalPricing().price(FOOD, FEE);

        assertEquals(new BigDecimal("68.00"), bill.total());
        assertEquals("Normal", bill.pricing());
    }

    @Test
    void peakHourMakesDeliveryHalfAgainMoreExpensive() {
        Bill bill = new PeakHourPricing().price(FOOD, FEE);

        assertEquals(new BigDecimal("12.00"), bill.deliveryFee());
        assertEquals(new BigDecimal("72.00"), bill.total());
    }

    @Test
    void expressAddsTheExpressFee() {
        Bill bill = new ExpressDeliveryPricing().price(FOOD, FEE);

        assertEquals(new BigDecimal("18.00"), bill.deliveryFee());
        assertEquals(new BigDecimal("78.00"), bill.total());
    }

    @Test
    void promotionDiscountsTheFoodOnly() {
        Bill bill = new PromotionalPricing(10).price(FOOD, FEE);

        assertEquals(new BigDecimal("6.00"), bill.discount());
        assertEquals(new BigDecimal("8.00"), bill.deliveryFee());
        assertEquals(new BigDecimal("62.00"), bill.total());
        assertEquals("Promotion 10%", bill.pricing());
        assertThrows(IllegalArgumentException.class, () -> new PromotionalPricing(0));
        assertThrows(IllegalArgumentException.class, () -> new PromotionalPricing(101));
    }

    @Test
    void expressOrdersAlwaysUseExpressPricing() {
        assertInstanceOf(ExpressDeliveryPricing.class, at(12, 30).choose(new ExpressOrder(), "NAJAH10"));
    }

    @Test
    void validPromoCodeIsUsedWhateverTheCase() {
        assertInstanceOf(PromotionalPricing.class, at(12, 30).choose(new StandardOrder(), " najah10 "));
        assertEquals(new BigDecimal("12.00"),
                at(16, 0).bill(FOOD, 0, new StandardOrder(), "WELCOME20").discount());
    }

    @Test
    void unknownPromoCodeIsRejected() {
        PricingService pricing = at(16, 0);
        StandardOrder standard = new StandardOrder();

        assertThrows(IllegalArgumentException.class, () -> pricing.choose(standard, "FREEFOOD"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12:00", "13:59", "19:00", "20:30"})
    void peakHoursUsePeakPricing(String time) {
        LocalTime t = LocalTime.parse(time);

        assertTrue(PricingService.isPeakHour(t));
        assertInstanceOf(PeakHourPricing.class, at(t.getHour(), t.getMinute()).choose(new StandardOrder(), ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"11:59", "14:00", "16:00", "18:59", "21:00", "02:00"})
    void otherTimesUseNormalPricing(String time) {
        LocalTime t = LocalTime.parse(time);

        assertFalse(PricingService.isPeakHour(t));
        assertInstanceOf(NormalPricing.class, at(t.getHour(), t.getMinute()).choose(new StandardOrder(), null));
    }

    @Test
    void needsAClock() {
        assertThrows(IllegalArgumentException.class, () -> new PricingService(null));
    }
}
