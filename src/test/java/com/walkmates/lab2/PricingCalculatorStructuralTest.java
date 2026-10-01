package com.walkmates.lab2;

import com.walkmates.model.Booking;
import com.walkmates.model.Listing;
import com.walkmates.model.ListingType;
import com.walkmates.model.Seeker;
import com.walkmates.model.TrustTier;
import com.walkmates.service.PricingCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Lab 2, Part A — structural testing for {@link PricingCalculator} (FR-4.3).
 *
 * <p>Price = hours * baseRate, +20% if duration is strictly > 480 min, then + tier platform fee,
 * rounded to 2 decimals. Expected values below are computed by hand from FR-4.3, not copied
 * from the implementation's output.</p>
 */
class PricingCalculatorStructuralTest {

    private final PricingCalculator pricing = new PricingCalculator();

    private Seeker seeker(TrustTier tier) {
        Seeker s = new Seeker("p@example.com", "Pat", "0701112233");
        s.setTrustTier(tier);
        return s;
    }

    private Listing listing(ListingType type) {
        return new Listing("provider-1", "A listing", "desc", type);
    }

    private Booking booking(int minutes) {
        return new Booking("seeker-1", "listing-1", minutes);
    }

    // ---- Worked example: a short standard walk, no overnight surcharge ----
    @Test
    @DisplayName("60 min DOG_WALK for a VERIFIED seeker = 80 base + 12% fee = 89.60")
    void shortWalkPrice() {
        double price = pricing.priceFor(booking(60), listing(ListingType.DOG_WALK), seeker(TrustTier.VERIFIED));

        assertThat(price).isEqualTo(89.60);
    }

    // ---- Branch: free listing short-circuits to 0 ----
    @Test
    @DisplayName("SHELTER_VOLUNTEER listing is always free, even for a long booking")
    void freeListingCostsZero() {
        double price = pricing.priceFor(booking(600), listing(ListingType.SHELTER_VOLUNTEER), seeker(TrustTier.NEW));

        assertThat(price).isEqualTo(0.00);
    }

    // ---- Branch: overnight surcharge taken ----
    @Test
    @DisplayName("600 min DOG_WALK, VERIFIED: 800 base + 20% = 960, + 12% fee = 1075.20")
    void clearlyOvernightBookingIsSurcharged() {
        double price = pricing.priceFor(booking(600), listing(ListingType.DOG_WALK), seeker(TrustTier.VERIFIED));

        assertThat(price).isEqualTo(1075.20);
    }

    // ---- BOUNDARY: exactly 480 min must NOT be surcharged (FR-4.3: strictly > 480) ----
    @Test
    @DisplayName("Exactly 480 min DOG_WALK, VERIFIED: 640 base, no surcharge, + 12% fee = 716.80")
    void exactly480MinutesIsNotSurcharged() {
        double price = pricing.priceFor(booking(480), listing(ListingType.DOG_WALK), seeker(TrustTier.VERIFIED));

        assertThat(price).isEqualTo(716.80);
    }

    // ---- BOUNDARY: first minute above the threshold IS surcharged ----
    @Test
    @DisplayName("481 min DOG_WALK, VERIFIED: 641.33 base + 20% = 769.60, + 12% fee = 861.95")
    void justOver480MinutesIsSurcharged() {
        double price = pricing.priceFor(booking(481), listing(ListingType.DOG_WALK), seeker(TrustTier.VERIFIED));

        assertThat(price).isEqualTo(861.95);
    }

    // ---- Fee per tier (kills mutants on the fee multiplication) ----
    @Test
    @DisplayName("60 min DOG_WALK, NEW tier: 80 + 15% fee = 92.00")
    void newTierFee() {
        double price = pricing.priceFor(booking(60), listing(ListingType.DOG_WALK), seeker(TrustTier.NEW));

        assertThat(price).isEqualTo(92.00);
    }

    @Test
    @DisplayName("120 min HOUSE_SITTING, PRO_SITTER: 300 + 5% fee = 315.00")
    void proSitterFeeDifferentRate() {
        double price = pricing.priceFor(booking(120), listing(ListingType.HOUSE_SITTING), seeker(TrustTier.PRO_SITTER));

        assertThat(price).isEqualTo(315.00);
    }

    // ---- Branch: null guard (each operand of the || separately) ----
    @Test
    @DisplayName("Null booking is rejected")
    void nullBookingRejected() {
        assertThatThrownBy(() -> pricing.priceFor(null, listing(ListingType.DOG_WALK), seeker(TrustTier.NEW)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Null listing is rejected")
    void nullListingRejected() {
        assertThatThrownBy(() -> pricing.priceFor(booking(60), null, seeker(TrustTier.NEW)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Null seeker is rejected")
    void nullSeekerRejected() {
        assertThatThrownBy(() -> pricing.priceFor(booking(60), listing(ListingType.DOG_WALK), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
