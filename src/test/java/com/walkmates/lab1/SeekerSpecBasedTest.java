package com.walkmates.lab1;

import com.walkmates.model.Seeker;
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
 * <p>Design your tests on paper first (equivalence partitions, boundary values, decision table)
 * from {@code docs/REQUIREMENTS.md} FR-1.1 / FR-1.3 / FR-1.2, then implement them here. One
 * worked example is provided; the {@code TODO}s are yours.</p>
 */
class SeekerSpecBasedTest {

    // ---- Worked example: boundary value at the maximum single top-up (FR-1.3) ----
    @Test
    @DisplayName("Top-up exactly at the 5000 SEK single-transaction maximum is accepted")
    void topUpAtSingleMaximumIsAccepted() {
        Seeker seeker = new Seeker("sam@example.com", "Sam", "0707654321");

        seeker.addFunds(Seeker.MAX_SINGLE_TOP_UP); // 5000.00, the boundary value

        assertThat(seeker.getBalance()).isEqualTo(Seeker.MAX_SINGLE_TOP_UP);
    }

    // TODO (EP): one valid + one invalid equivalence class for email, name, and phone (FR-1.1).
    // TODO (Decision table): expected fee + max-bookings for each trust tier (FR-1.2).

    @Test
    @DisplayName("TODO: replace me — invalid email is rejected at registration")
    void invalidEmailIsRejected() {
        // Example of the shape; expand into your full EP set.
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker("not-an-email", "Sam", "0707654321"));
    }

    // =================================================================
    // Aktivitet 2.1 + 2.2 — wallet (FR-1.3). Ansvarig: Muntaser.
    // =================================================================
    @Nested
    @DisplayName("Wallet: ekvivalensklasser och gränsvärden (FR-1.3)")
    class WalletBoundaryTests {

        /** Giltig Seeker enligt FR-1.1. Saldo startar på 0.00 (FR-1.2). */
        private Seeker newSeeker() {
            return new Seeker("muntaser@example.se", "Muntaser", "0701234567");
        }

        /**
         * Fyller saldot till exakt 20 000.00 med fyra tillåtna insättningar.
         * Nödvändigt eftersom addFunds kontrollerar maxbelopp per transaktion
         * FÖRE maxsaldo — en enda stor insättning når aldrig maxsaldo-kontrollen.
         */
        private Seeker seekerWithFullWallet() {
            Seeker s = newSeeker();
            for (int i = 0; i < 4; i++) {
                s.addFunds(Seeker.MAX_SINGLE_TOP_UP);
            }
            return s;
        }

        // ---------- Aktivitet 2.1: ekvivalensklasser ----------

        @Test
        @DisplayName("V1: giltigt belopp inom [10, 5000] adderas till saldot")
        void validAmountIncreasesBalance() {
            Seeker s = newSeeker();

            s.addFunds(100.00);

            assertThat(s.getBalance()).isCloseTo(100.00, within(0.001));
        }

        @ParameterizedTest(name = "ogiltigt belopp {0} avvisas och lämnar saldot orört")
        @ValueSource(doubles = {-50.00, 0.00, 5.00, 5_500.00})
        @DisplayName("I1–I4: belopp utanför giltig klass avvisas")
        void invalidAmountIsRejectedAndBalanceUnchanged(double amount) {
            Seeker s = newSeeker();

            assertThatThrownBy(() -> s.addFunds(amount))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(s.getBalance()).isZero();
        }

        @Test
        @DisplayName("I5: insättning som skulle överskrida maxsaldo avvisas")
        void topUpExceedingMaxBalanceIsRejected() {
            Seeker s = seekerWithFullWallet();

            assertThatThrownBy(() -> s.addFunds(Seeker.MIN_TOP_UP))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(s.getBalance()).isCloseTo(Seeker.MAX_BALANCE, within(0.001));
        }

        // ---------- Aktivitet 2.2: gränsvärden, minimum 10.00 ----------

        @Test
        @DisplayName("9.99 (strax under minimum) avvisas")
        void justBelowMinimumIsRejected() {
            Seeker s = newSeeker();

            assertThatThrownBy(() -> s.addFunds(9.99))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(s.getBalance()).isZero();
        }

        @Test
        @DisplayName("10.00 (exakt minimum) accepteras")
        void exactMinimumIsAccepted() {
            Seeker s = newSeeker();

            s.addFunds(Seeker.MIN_TOP_UP);

            assertThat(s.getBalance()).isCloseTo(10.00, within(0.001));
        }

        @Test
        @DisplayName("10.01 (strax över minimum) accepteras")
        void justAboveMinimumIsAccepted() {
            Seeker s = newSeeker();

            s.addFunds(10.01);

            assertThat(s.getBalance()).isCloseTo(10.01, within(0.001));
        }

        // ---------- Aktivitet 2.2: gränsvärden, max per transaktion 5 000.00 ----------

        @Test
        @DisplayName("4 999.99 (strax under maxbelopp) accepteras")
        void justBelowMaxSingleTopUpIsAccepted() {
            Seeker s = newSeeker();

            s.addFunds(4_999.99);

            assertThat(s.getBalance()).isCloseTo(4_999.99, within(0.001));
        }

        @Test
        @DisplayName("5 000.01 (strax över maxbelopp) avvisas")
        void justAboveMaxSingleTopUpIsRejected() {
            Seeker s = newSeeker();

            assertThatThrownBy(() -> s.addFunds(5_000.01))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(s.getBalance()).isZero();
        }

        // ---------- Aktivitet 2.2: gränsvärden, maxsaldo 20 000.00 ----------

        @Test
        @DisplayName("19 999.99 (strax under maxsaldo) accepteras")
        void balanceJustBelowMaxIsAccepted() {
            Seeker s = newSeeker();
            s.addFunds(Seeker.MAX_SINGLE_TOP_UP);
            s.addFunds(Seeker.MAX_SINGLE_TOP_UP);
            s.addFunds(Seeker.MAX_SINGLE_TOP_UP);

            s.addFunds(4_999.99);

            assertThat(s.getBalance()).isCloseTo(19_999.99, within(0.001));
        }

        @Test
        @DisplayName("20 000.00 (exakt maxsaldo) accepteras")
        void balanceMayReachExactMaximum() {
            Seeker s = seekerWithFullWallet();

            assertThat(s.getBalance()).isCloseTo(Seeker.MAX_BALANCE, within(0.001));
        }

        @Test
        @DisplayName("Insättning som skulle ge 20 000.01 avvisas")
        void balanceJustAboveMaxIsRejected() {
            Seeker s = newSeeker();
            s.addFunds(Seeker.MAX_SINGLE_TOP_UP);
            s.addFunds(Seeker.MAX_SINGLE_TOP_UP);
            s.addFunds(Seeker.MAX_SINGLE_TOP_UP);
            s.addFunds(4_999.99);
            // Saldo: 19 999.99. En insättning på 0.02 vore under minimum,
            // så minsta tillåtna insättning (10.00) används i stället.

            assertThatThrownBy(() -> s.addFunds(Seeker.MIN_TOP_UP))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(s.getBalance()).isCloseTo(19_999.99, within(0.001));
        }

        // ---------- Avrundning, half-up (FR-1.3) ----------

        @Test
        @DisplayName("10.005 avrundas half-up till 10.01")
        void amountIsRoundedHalfUpToTwoDecimals() {
            Seeker s = newSeeker();

            s.addFunds(10.005);

            assertThat(s.getBalance()).isCloseTo(10.01, within(0.0001));
        }
    }
}