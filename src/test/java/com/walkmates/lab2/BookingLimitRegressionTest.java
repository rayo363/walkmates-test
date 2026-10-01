package com.walkmates.lab2;

import com.walkmates.model.Booking;
import com.walkmates.model.Listing;
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
import org.junit.jupiter.api.BeforeEach;
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
 * Lab 2, Activity 4.1 — regression test for the booking-limit boundary fault found in Lab 1.
 *
 * <p>FR-4.4 rule 2: a booking is allowed only while the seeker's active bookings are BELOW the
 * trust-tier max. A NEW seeker (max 1) who already has 1 active booking must be rejected.</p>
 */
class BookingLimitRegressionTest {

    private SeekerRepository seekers;
    private ListingRepository listings;
    private ProviderRepository providers;
    private BookingRepository bookings;
    private NotificationService notifications;
    private BookingService service;

    private Seeker seeker;
    private Listing listing;
    private Provider provider;

    @BeforeEach
    void setUp() {
        seekers = mock(SeekerRepository.class);
        listings = mock(ListingRepository.class);
        providers = mock(ProviderRepository.class);
        bookings = mock(BookingRepository.class);
        notifications = mock(NotificationService.class);
        service = new BookingService(seekers, listings, providers, bookings, new PricingCalculator(), notifications);

        seeker = new Seeker("p@example.com", "Pat", "0701112233");
        seeker.setTrustTier(TrustTier.NEW); // max 1 concurrent booking
        seeker.addFunds(1_000.00);

        provider = new Provider("Dog Co", 57.0, 16.0, 10);
        listing = new Listing(provider.getId(), "Walk", "desc", ListingType.DOG_WALK);

        when(seekers.findById(seeker.getId())).thenReturn(Optional.of(seeker));
        when(listings.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(providers.findById(provider.getId())).thenReturn(Optional.of(provider));
        when(listings.findByProviderId(provider.getId())).thenReturn(List.of(listing));
        when(bookings.findByListingId(any())).thenReturn(List.of());
    }

    private List<Booking> activeBookings(int n) {
        List<Booking> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new Booking(seeker.getId(), "other-listing-" + i, 60)); // REQUESTED = active
        }
        return list;
    }

    @Test
    @DisplayName("NEW seeker with 0 active bookings (below max 1) can book")
    void belowLimitIsAccepted() {
        when(bookings.findBySeekerId(seeker.getId())).thenReturn(activeBookings(0));

        Booking booking = service.createBooking(seeker.getId(), listing.getId(), 60);

        assertThat(booking).isNotNull();
    }

    @Test
    @DisplayName("NEW seeker with exactly 1 active booking (= max) is rejected — equality boundary")
    void atLimitIsRejected() {
        when(bookings.findBySeekerId(seeker.getId())).thenReturn(activeBookings(1));

        assertThatThrownBy(() -> service.createBooking(seeker.getId(), listing.getId(), 60))
                .isInstanceOf(BookingRejectedException.class)
                .hasMessageContaining("limit");
        verify(bookings, never()).save(any());
        verify(notifications, never()).sendBookingConfirmed(any(), any());
    }
}
