package com.walkmates.lab2;

import com.walkmates.model.Booking;
import com.walkmates.model.BookingStatus;
import com.walkmates.model.Listing;
import com.walkmates.model.ListingType;
import com.walkmates.model.Provider;
import com.walkmates.model.Seeker;
import com.walkmates.repository.BookingRepository;
import com.walkmates.repository.ListingRepository;
import com.walkmates.repository.ProviderRepository;
import com.walkmates.repository.SeekerRepository;
import com.walkmates.service.BookingService;
import com.walkmates.service.BookingService.BookingRejectedException;
import com.walkmates.service.NotificationService;
import com.walkmates.service.PricingCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private SeekerRepository seekers;
    private ListingRepository listings;
    private ProviderRepository providers;
    private BookingRepository bookings;
    private PricingCalculator pricing;
    private NotificationService notifications;

    private BookingService bookingService;

    private Seeker seeker;
    private Provider provider;
    private Listing listing;

    @BeforeEach
    void setUp() {
        // Mock dependencies so BookingService is tested in isolation.
        seekers = mock(SeekerRepository.class);
        listings = mock(ListingRepository.class);
        providers = mock(ProviderRepository.class);
        bookings = mock(BookingRepository.class);
        pricing = mock(PricingCalculator.class);
        notifications = mock(NotificationService.class);

        bookingService = new BookingService(
                seekers,
                listings,
                providers,
                bookings,
                pricing,
                notifications);

        // Create a valid seeker with enough money.
        seeker = new Seeker(
                "alex@example.com",
                "Alex",
                "0701234567");
        seeker.addFunds(1000.00);

        // Create a provider and an available listing.
        provider = new Provider(
                "Dog Co",
                57.0,
                16.0,
                5);

        listing = new Listing(
                provider.getId(),
                "Dog walk",
                "Walk the dog",
                ListingType.DOG_WALK);

        // Common mock setup for a valid booking.
        when(seekers.findById(seeker.getId()))
                .thenReturn(Optional.of(seeker));

        when(listings.findById(listing.getId()))
                .thenReturn(Optional.of(listing));

        when(providers.findById(provider.getId()))
                .thenReturn(Optional.of(provider));

        when(bookings.findBySeekerId(seeker.getId()))
                .thenReturn(List.of());

        when(listings.findByProviderId(provider.getId()))
                .thenReturn(List.of(listing));

        when(bookings.findByListingId(listing.getId()))
                .thenReturn(List.of());
    }

    @Test
    void successfulBookingSendsConfirmationNotification() {
        // Simulate a booking price of 100 SEK.
        when(pricing.priceFor(any(Booking.class), eq(listing), eq(seeker)))
                .thenReturn(100.00);

        Booking booking = bookingService.createBooking(
                seeker.getId(),
                listing.getId(),
                60);

        // The booking should be confirmed.
        assertThat(booking.getStatus())
                .isEqualTo(BookingStatus.CONFIRMED);

        // The wallet should be charged.
        assertThat(seeker.getBalance())
                .isEqualTo(900.00);

        // The booking should be saved and a notification should be sent.
        verify(bookings).save(booking);
        verify(notifications).sendBookingConfirmed(seeker, booking);
    }

    @Test
    void insufficientBalanceRejectsBookingAndDoesNotNotify() {
        // Simulate a price higher than the seeker's balance.
        when(pricing.priceFor(any(Booking.class), eq(listing), eq(seeker)))
                .thenReturn(1500.00);

        assertThatThrownBy(() -> bookingService.createBooking(
                seeker.getId(),
                listing.getId(),
                60))
                .isInstanceOf(BookingRejectedException.class)
                .hasMessageContaining("Insufficient balance");

        // The wallet must remain unchanged.
        assertThat(seeker.getBalance())
                .isEqualTo(1000.00);

        // Rejected bookings must not be saved or notified.
        verify(bookings, never()).save(any());
        verify(notifications, never())
                .sendBookingConfirmed(any(), any());
    }
}