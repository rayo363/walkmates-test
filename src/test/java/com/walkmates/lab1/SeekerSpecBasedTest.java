package com.walkmates.lab1;

import com.walkmates.model.Seeker;
import com.walkmates.model.TrustTier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Lab 1, Part B — specification-based tests for {@link Seeker}.
 *
 * <p>
 * Design your tests on paper first (equivalence partitions, boundary values,
 * decision table)
 * from {@code docs/REQUIREMENTS.md} FR-1.1 / FR-1.3 / FR-1.2, then implement
 * them here. One
 * worked example is provided; the {@code TODO}s are yours.
 * </p>
 */
class SeekerSpecBasedTest {

    // ---- Worked example: boundary value at the maximum single top-up (FR-1.3)
    // ----
    @Test
    @DisplayName("Top-up exactly at the 5000 SEK single-transaction maximum is accepted")
    void topUpAtSingleMaximumIsAccepted() {
        Seeker seeker = new Seeker("sam@example.com", "Sam", "0707654321");

        seeker.addFunds(Seeker.MAX_SINGLE_TOP_UP); // 5000.00, the boundary value

        assertThat(seeker.getBalance()).isEqualTo(Seeker.MAX_SINGLE_TOP_UP);
    }

    @Test
    @DisplayName("Adding 250 SEK to a new seeker gives a 250.00 balance")
    void addingFundsWorks() {
        Seeker seeker = new Seeker("you@example.com", "You", "0701234567"); // Arrange
        seeker.addFunds(250.00); // Act
        assertThat(seeker.getBalance()).isEqualTo(250.00); // Assert
    }

    // =================================================================
    // Activity 2.1 — equivalence partitions for the identity fields
    // (FR-1.1). Owner: Rasha.
    // =================================================================
    @Nested
    class IdentityPartitionTests {

        @Test
        @DisplayName("Valid email is accepted")
        void validEmailIsAccepted() {
            Seeker seeker = new Seeker(
                    "alex@example.com",
                    "Alex",
                    "0701234567");

            assertThat(seeker).isNotNull();
        }

        @Test
        @DisplayName("Email without @ is rejected")
        void emailWithoutAtIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker(
                            "alexexample.com",
                            "Alex",
                            "0701234567"));
        }

        @Test
        @DisplayName("Email with more than one @ is rejected")
        void emailWithMoreThanOneAtIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker(
                            "alex@@example.com",
                            "Alex",
                            "0701234567"));
        }

        @Test
        @DisplayName("Email with empty local part is rejected")
        void emailWithEmptyLocalPartIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker(
                            "@example.com",
                            "Alex",
                            "0701234567"));
        }

        @Test
        @DisplayName("Email with domain without dot is rejected")
        void emailWithoutDomainDotIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker(
                            "alex@example",
                            "Alex",
                            "0701234567"));
        }

        @Test
        @DisplayName("Email longer than 254 characters is rejected")
        void emailLongerThan254CharactersIsRejected() {
            String email = "a".repeat(243) + "@example.com";

            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker(
                            email,
                            "Alex",
                            "0701234567"));
        }

        @Test
        @DisplayName("Valid display name is accepted")
        void validDisplayNameIsAccepted() {
            Seeker seeker = new Seeker(
                    "alex@example.com",
                    "Anna-Marie",
                    "0701234567");

            assertThat(seeker).isNotNull();
        }

        @Test
        @DisplayName("Display name shorter than 2 characters is rejected")
        void displayNameTooShortIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker(
                            "alex@example.com",
                            "A",
                            "0701234567"));
        }

        @Test
        @DisplayName("Display name longer than 40 characters is rejected")
        void displayNameTooLongIsRejected() {
            String name = "A".repeat(41);

            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker(
                            "alex@example.com",
                            name,
                            "0701234567"));
        }

        @Test
        @DisplayName("Display name with forbidden characters is rejected")
        void displayNameWithForbiddenCharactersIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker(
                            "alex@example.com",
                            "Anna123",
                            "0701234567"));
        }

        @Test
        @DisplayName("Valid Swedish phone number is accepted")
        void validSwedishPhoneNumberIsAccepted() {
            Seeker seeker = new Seeker(
                    "alex@example.com",
                    "Alex",
                    "0701234567");

            assertThat(seeker).isNotNull();
        }

        @Test
        @DisplayName("Valid international phone number is accepted")
        void validInternationalPhoneNumberIsAccepted() {
            Seeker seeker = new Seeker(
                    "alex@example.com",
                    "Alex",
                    "+4671234567");

            assertThat(seeker).isNotNull();
        }

        @Test
        @DisplayName("Phone number with wrong prefix is rejected")
        void phoneNumberWithWrongPrefixIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker(
                            "alex@example.com",
                            "Alex",
                            "0812345678"));
        }

        @Test
        @DisplayName("Phone number with wrong length is rejected")
        void phoneNumberWithWrongLengthIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker(
                            "alex@example.com",
                            "Alex",
                            "070123456"));
        }

    }

    // =================================================================
    // Activity 2.3 — decision table, trust tier to limits (FR-1.2).
    // Owner: Rasha.
    // =================================================================
    @Nested
    class TrustTierDecisionTableTests {

        @Test
        @DisplayName("NEW tier has max 1 booking and 15 percent fee")
        void newTierHasCorrectLimits() {
            Seeker seeker = new Seeker(
                    "alex@example.com",
                    "Alex",
                    "0701234567");

            assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(1);
            assertThat(seeker.getTrustTier().getPlatformFee()).isEqualTo(0.15);
        }

        @Test
        @DisplayName("VERIFIED tier has max 3 bookings and 12 percent fee")
        void verifiedTierHasCorrectLimits() {
            Seeker seeker = new Seeker(
                    "alex@example.com",
                    "Alex",
                    "0701234567");

            seeker.setTrustTier(TrustTier.VERIFIED);

            assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(3);
            assertThat(seeker.getTrustTier().getPlatformFee()).isEqualTo(0.12);
        }

        @Test
        @DisplayName("TRUSTED tier has max 5 bookings and 8 percent fee")
        void trustedTierHasCorrectLimits() {
            Seeker seeker = new Seeker(
                    "alex@example.com",
                    "Alex",
                    "0701234567");

            seeker.setTrustTier(TrustTier.TRUSTED);

            assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(5);
            assertThat(seeker.getTrustTier().getPlatformFee()).isEqualTo(0.08);
        }

        @Test
        @DisplayName("PRO_SITTER tier has max 10 bookings and 5 percent fee")
        void proSitterTierHasCorrectLimits() {
            Seeker seeker = new Seeker(
                    "alex@example.com",
                    "Alex",
                    "0701234567");

            seeker.setTrustTier(TrustTier.PRO_SITTER);

            assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(10);
            assertThat(seeker.getTrustTier().getPlatformFee()).isEqualTo(0.05);
        }
    }

    // =================================================================
    // Activities 2.1 + 2.2 — wallet (FR-1.3). Owner: Muntaser.
    // =================================================================
    @Nested
    @DisplayName("Wallet: equivalence partitions and boundary values (FR-1.3)")
    class WalletBoundaryTests {

        /** A valid Seeker per FR-1.1. Balance starts at 0.00 (FR-1.2). */
        private Seeker newSeeker() {
            return new Seeker("muntaser@example.se", "Muntaser", "0701234567");
        }

        /**
         * Fills the wallet to exactly 20 000.00 using four permitted top-ups.
         * Required because addFunds validates the per-transaction maximum BEFORE
         * the maximum balance — a single large top-up never reaches the
         * maximum-balance check.
         */
        private Seeker seekerWithFullWallet() {
            Seeker s = newSeeker();
            for (int i = 0; i < 4; i++) {
                s.addFunds(Seeker.MAX_SINGLE_TOP_UP);
            }
            return s;
        }

        // ---------- Activity 2.1: equivalence partitions ----------

        @Test
        @DisplayName("V1: a valid amount within [10, 5000] is added to the balance")
        void validAmountIncreasesBalance() {
            Seeker s = newSeeker();

            s.addFunds(100.00);

            assertThat(s.getBalance()).isCloseTo(100.00, within(0.001));
        }

        @ParameterizedTest(name = "invalid amount {0} is rejected and leaves the balance unchanged")
        @ValueSource(doubles = { -50.00, 0.00, 5.00, 5_500.00 })
        @DisplayName("I1-I4: amounts outside the valid partition are rejected")
        void invalidAmountIsRejectedAndBalanceUnchanged(double amount) {
            Seeker s = newSeeker();

            assertThatThrownBy(() -> s.addFunds(amount))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(s.getBalance()).isZero();
        }

        @Test
        @DisplayName("I5: a top-up that would exceed the maximum balance is rejected")
        void topUpExceedingMaxBalanceIsRejected() {
            Seeker s = seekerWithFullWallet();

            assertThatThrownBy(() -> s.addFunds(Seeker.MIN_TOP_UP))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(s.getBalance()).isCloseTo(Seeker.MAX_BALANCE, within(0.001));
        }

        // ---------- Activity 2.2: boundary values, minimum 10.00 ----------

        @Test
        @DisplayName("9.99 (just below the minimum) is rejected")
        void justBelowMinimumIsRejected() {
            Seeker s = newSeeker();

            assertThatThrownBy(() -> s.addFunds(9.99))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(s.getBalance()).isZero();
        }

        @Test
        @DisplayName("10.00 (exactly the minimum) is accepted")
        void exactMinimumIsAccepted() {
            Seeker s = newSeeker();

            s.addFunds(Seeker.MIN_TOP_UP);

            assertThat(s.getBalance()).isCloseTo(10.00, within(0.001));
        }

        @Test
        @DisplayName("10.01 (just above the minimum) is accepted")
        void justAboveMinimumIsAccepted() {
            Seeker s = newSeeker();

            s.addFunds(10.01);

            assertThat(s.getBalance()).isCloseTo(10.01, within(0.001));
        }

        // ---------- Activity 2.2: boundary values, single maximum 5 000.00 ----------

        @Test
        @DisplayName("4999.99 (just below the single maximum) is accepted")
        void justBelowMaxSingleTopUpIsAccepted() {
            Seeker s = newSeeker();

            s.addFunds(4_999.99);

            assertThat(s.getBalance()).isCloseTo(4_999.99, within(0.001));
        }

        @Test
        @DisplayName("5000.01 (just above the single maximum) is rejected")
        void justAboveMaxSingleTopUpIsRejected() {
            Seeker s = newSeeker();

            assertThatThrownBy(() -> s.addFunds(5_000.01))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(s.getBalance()).isZero();
        }

        // ---------- Activity 2.2: boundary values, maximum balance 20 000.00 ----------

        @Test
        @DisplayName("19999.99 (just below the maximum balance) is accepted")
        void balanceJustBelowMaxIsAccepted() {
            Seeker s = newSeeker();
            s.addFunds(Seeker.MAX_SINGLE_TOP_UP);
            s.addFunds(Seeker.MAX_SINGLE_TOP_UP);
            s.addFunds(Seeker.MAX_SINGLE_TOP_UP);

            s.addFunds(4_999.99);

            assertThat(s.getBalance()).isCloseTo(19_999.99, within(0.001));
        }

        @Test
        @DisplayName("20000.00 (exactly the maximum balance) is accepted")
        void balanceMayReachExactMaximum() {
            Seeker s = seekerWithFullWallet();

            assertThat(s.getBalance()).isCloseTo(Seeker.MAX_BALANCE, within(0.001));
        }

        @Test
        @DisplayName("A top-up that would result in 20000.01 is rejected")
        void balanceJustAboveMaxIsRejected() {
            Seeker s = newSeeker();
            s.addFunds(Seeker.MAX_SINGLE_TOP_UP);
            s.addFunds(Seeker.MAX_SINGLE_TOP_UP);
            s.addFunds(Seeker.MAX_SINGLE_TOP_UP);
            s.addFunds(4_999.99);
            // Balance: 19 999.99. A top-up of 0.02 would fall below the minimum,
            // so the smallest permitted top-up (10.00) is used instead.

            assertThatThrownBy(() -> s.addFunds(Seeker.MIN_TOP_UP))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(s.getBalance()).isCloseTo(19_999.99, within(0.001));
        }

        // ---------- Rounding, half-up (FR-1.3) ----------

        @Test
        @DisplayName("10.005 is rounded half-up to 10.01")
        void amountIsRoundedHalfUpToTwoDecimals() {
            Seeker s = newSeeker();

            s.addFunds(10.005);

            assertThat(s.getBalance()).isCloseTo(10.01, within(0.0001));
        }
    }
}