package com.walkmates.lab2;

import com.walkmates.model.Seeker;
import com.walkmates.repository.SeekerRepository;
import com.walkmates.service.NotificationService;
import com.walkmates.service.PaymentService;
import com.walkmates.service.SeekerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SeekerServiceTest {

    private SeekerRepository seekers;
    private PaymentService payments;
    private NotificationService notifications;
    private SeekerService seekerService;

    private Seeker seeker;

    @BeforeEach
    void setUp() {
        // Mock external dependencies so SeekerService can be tested in isolation.
        seekers = mock(SeekerRepository.class);
        payments = mock(PaymentService.class);
        notifications = mock(NotificationService.class);

        seekerService = new SeekerService(seekers, payments, notifications);

        // Start each test with a new seeker and an empty wallet.
        seeker = new Seeker(
                "alex@example.com",
                "Alex",
                "0701234567");
    }

    @Test
    void successfulTopUpCreditsWallet() throws Exception {
        when(seekers.findById("seeker-1"))
                .thenReturn(Optional.of(seeker));

        // Simulate a successful payment from the external gateway.
        when(payments.charge("seeker-1", "card-1", 100.00))
                .thenReturn("payment-123");

        when(seekers.save(seeker))
                .thenReturn(seeker);

        Seeker result = seekerService.topUp(
                "seeker-1",
                "card-1",
                100.00);

        // The wallet should be credited after a successful charge.
        assertThat(result.getBalance()).isEqualTo(100.00);

        verify(payments).charge("seeker-1", "card-1", 100.00);
        verify(seekers).save(seeker);
    }

    @Test
    void declinedPaymentDoesNotCreditWallet() throws Exception {
        when(seekers.findById("seeker-1"))
                .thenReturn(Optional.of(seeker));

        // Simulate a declined payment.
        when(payments.charge("seeker-1", "card-1", 100.00))
                .thenThrow(new PaymentService.PaymentException("Payment declined"));

        assertThatThrownBy(() -> seekerService.topUp("seeker-1", "card-1", 100.00))
                .isInstanceOf(PaymentService.PaymentException.class);

        // The wallet must remain unchanged when the payment fails.
        assertThat(seeker.getBalance()).isEqualTo(0.00);

        // The seeker must not be saved after a failed charge.
        verify(seekers, never()).save(any());
    }

    @Test
    void paymentTimeoutDoesNotCreditWallet() throws Exception {
        when(seekers.findById("seeker-1"))
                .thenReturn(Optional.of(seeker));

        // Simulate a timeout from the payment gateway.
        when(payments.charge("seeker-1", "card-1", 100.00))
                .thenThrow(new PaymentService.PaymentTimeoutException(
                        "Payment gateway timeout"));

        assertThatThrownBy(() -> seekerService.topUp("seeker-1", "card-1", 100.00))
                .isInstanceOf(PaymentService.PaymentTimeoutException.class);

        // The wallet must also remain unchanged after a timeout.
        assertThat(seeker.getBalance()).isEqualTo(0.00);

        verify(seekers, never()).save(any());
    }
}