package com.walkmates.lab1;

import com.walkmates.model.Seeker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;
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

    // TODO (EP): one valid + one invalid equivalence class for email, name, and
    // phone (FR-1.1).
    // TODO (BVA): just-below / at / just-above the 10.00 minimum top-up (FR-1.3).
    // TODO (BVA): a top-up that would push the balance above 20000.00 is rejected
    // (FR-1.3).
    // TODO (Decision table): expected fee + max-bookings for each trust tier
    // (FR-1.2).

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

    @Test
    @DisplayName("Adding 250 SEK to a new seeker gives a 250.00 balance")
    void addingFundsWorks() {
        Seeker seeker = new Seeker("you@example.com", "You", "0701234567"); // Arrange
        seeker.addFunds(250.00); // Act
        assertThat(seeker.getBalance()).isEqualTo(250.00); // Assert
    }
}
