package com.walkmates.lab2;

import com.walkmates.model.Booking;
import com.walkmates.model.BookingStatus;
import com.walkmates.model.Listing;
import com.walkmates.model.ListingStatus;
import com.walkmates.model.ListingType;
import com.walkmates.model.Provider;
import com.walkmates.model.Seeker;
import com.walkmates.model.TrustTier;
import com.walkmates.repository.BookingRepository;
import com.walkmates.repository.ListingRepository;
import com.walkmates.repository.ProviderRepository;
import com.walkmates.repository.SeekerRepository;
import com.walkmates.service.BookingService;
import com.walkmates.service.BookingService.BookingRejectedException;
import com.walkmates.service.NotificationService;
import com.walkmates.service.PricingCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lab 2, Activity 4.1 — tests written to kill PIT mutants that SURVIVED in
 * {@link BookingService#createBooking} after the first PIT run.
 *
 * <p>Each test names the surviving mutant it targets (source line in BookingService).
 * Price for a 60 min DOG_WALK for a NEW seeker = 80 + 15% fee = 92.00 SEK (FR-4.3).</p>
 */
class BookingServiceMutationTest {

    private static final double PRICE_60_MIN_DOG_WALK_NEW = 92.00;

    private final SeekerRepository seekers = mock(SeekerRepository.class);
    private final ListingRepository listings = mock(ListingRepository.class);
    private final ProviderRepository providers = mock(ProviderRepository.class);
    private final BookingRepository bookings = mock(BookingRepository.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final BookingService service =
            new BookingService(seekers, listings, providers, bookings, new PricingCalculator(), notifications);

    private Seeker seeker;
    private Provider provider;
    private Listing listing;

    /** Wires a NEW-tier seeker with the given balance and a provider with the given capacity. */
    private void scenario(double balance, int providerCapacity, int activeBookingsAtProvider) {
        seeker = new Seeker("p@example.com", "Pat", "0701112233");
        seeker.setTrustTier(TrustTier.NEW); // max 1 concurrent booking
        seeker.addFunds(balance);

        provider = new Provider("Dog Co", 57.0, 16.0, providerCapacity);
        listing = new Listing(provider.getId(), "Walk", "desc", ListingType.DOG_WALK);

        when(seekers.findById(seeker.getId())).thenReturn(Optional.of(seeker));
        when(listings.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(providers.findById(provider.getId())).thenReturn(Optional.of(provider));
        when(listings.findByProviderId(provider.getId())).thenReturn(List.of(listing));
        when(bookings.findBySeekerId(seeker.getId())).thenReturn(List.of());
        when(bookings.findByListingId(listing.getId())).thenReturn(activeBookings(activeBookingsAtProvider));
    }

    private List<Booking> activeBookings(int n) {
        List<Booking> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new Booking("other-seeker-" + i, "listing-x", 60)); // REQUESTED = active
        }
        return list;
    }

    // ---- Line 71: seeker limit boundary (>= vs >), and line 104: seeker active count ----

    @Test
    @DisplayName("NEW seeker with 1 active booking (= tier max 1) is rejected")
    void seekerAtTierLimitIsRejected() {
        scenario(1_000.00, 10, 0);
        when(bookings.findBySeekerId(seeker.getId()))
                .thenReturn(List.of(new Booking(seeker.getId(), "earlier-listing", 60))); // REQUESTED = active

        assertThatThrownBy(() -> service.createBooking(seeker.getId(), listing.getId(), 60))
                .isInstanceOf(BookingRejectedException.class)
                .hasMessageContaining("limit");
        assertThat(seeker.getBalance()).isEqualTo(1_000.00);
        verify(bookings, never()).save(any());
    }

    @Test
    @DisplayName("Cancelled bookings do not count toward the seeker limit")
    void cancelledBookingDoesNotCountTowardLimit() {
        scenario(1_000.00, 10, 0);
        Booking cancelled = new Booking(seeker.getId(), "earlier-listing", 60);
        cancelled.transitionTo(BookingStatus.CANCELLED);
        when(bookings.findBySeekerId(seeker.getId())).thenReturn(List.of(cancelled));

        Booking booking = service.createBooking(seeker.getId(), listing.getId(), 60);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    // ---- Line 79: provider capacity boundary (>= vs >), and lines 110/111: provider count ----

    @Test
    @DisplayName("Provider at capacity (1 active booking, capacity 1) is rejected")
    void providerAtCapacityIsRejected() {
        scenario(1_000.00, 1, 1);

        assertThatThrownBy(() -> service.createBooking(seeker.getId(), listing.getId(), 60))
                .isInstanceOf(BookingRejectedException.class)
                .hasMessageContaining("capacity");
        verify(notifications, never()).sendBookingConfirmed(any(), any());
    }

    @Test
    @DisplayName("Provider one below capacity (1 active booking, capacity 2) is accepted")
    void providerBelowCapacityIsAccepted() {
        scenario(1_000.00, 2, 1);

        Booking booking = service.createBooking(seeker.getId(), listing.getId(), 60);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    // ---- Line 85: balance boundary (> vs >=) ----

    @Test
    @DisplayName("Price exactly equal to balance (92.00 = 92.00) is accepted and empties the wallet")
    void priceEqualToBalanceIsAccepted() {
        scenario(PRICE_60_MIN_DOG_WALK_NEW, 10, 0);

        service.createBooking(seeker.getId(), listing.getId(), 60);

        assertThat(seeker.getBalance()).isEqualTo(0.00);
    }

    @Test
    @DisplayName("Balance one öre short of the price (91.99) is rejected")
    void balanceJustBelowPriceIsRejected() {
        scenario(91.99, 10, 0);

        assertThatThrownBy(() -> service.createBooking(seeker.getId(), listing.getId(), 60))
                .isInstanceOf(BookingRejectedException.class)
                .hasMessageContaining("balance");
        assertThat(seeker.getBalance()).isEqualTo(91.99);
    }

    // ---- Lines 90–93, 99: side effects of a successful booking ----

    @Test
    @DisplayName("Successful booking charges wallet, sets price, confirms booking, books listing, notifies")
    void successfulBookingHasAllSideEffects() {
        scenario(1_000.00, 10, 0);

        Booking booking = service.createBooking(seeker.getId(), listing.getId(), 60);

        assertThat(seeker.getBalance()).isEqualTo(1_000.00 - PRICE_60_MIN_DOG_WALK_NEW); // line 90
        assertThat(booking.getPrice()).isEqualTo(PRICE_60_MIN_DOG_WALK_NEW);              // line 91
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);              // line 92
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.BOOKED);                 // line 93
        verify(bookings).save(booking);
        verify(notifications).sendBookingConfirmed(seeker, booking);                      // line 99
    }

    // ---- Lines 57, 59, 77: NO_COVERAGE lambdas in orElseThrow ----

    @Test
    @DisplayName("Unknown seeker is rejected with BookingRejectedException")
    void unknownSeekerIsRejected() {
        when(seekers.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createBooking("ghost", "any-listing", 60))
                .isInstanceOf(BookingRejectedException.class)
                .hasMessageContaining("Unknown seeker");
    }

    @Test
    @DisplayName("Unknown listing is rejected with BookingRejectedException")
    void unknownListingIsRejected() {
        scenario(1_000.00, 10, 0);
        when(listings.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createBooking(seeker.getId(), "ghost", 60))
                .isInstanceOf(BookingRejectedException.class)
                .hasMessageContaining("Unknown listing");
    }

    @Test
    @DisplayName("Listing whose provider does not exist is rejected with BookingRejectedException")
    void unknownProviderIsRejected() {
        scenario(1_000.00, 10, 0);
        when(providers.findById(provider.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createBooking(seeker.getId(), listing.getId(), 60))
                .isInstanceOf(BookingRejectedException.class)
                .hasMessageContaining("Unknown provider");
    }
}