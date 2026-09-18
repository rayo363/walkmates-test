# Lab 1 Analysis

## Activity 1.1 — Quality-attribute analysis

### FR-4.4 — Booking creation rule

**Quality characteristics:** Functional correctness and Reliability.

**Functional correctness** is important because a booking must only be accepted when all five conditions in FR-4.4 are satisfied, including the Seeker's trust-tier booking limit, Provider capacity, valid duration, and sufficient wallet balance.

**Reliability** is important because the same booking rules should be applied consistently every time a booking request is evaluated, so an invalid booking is not accepted under the same conditions.

### Testable quality requirement

For a `NEW` Seeker, the maximum number of concurrent active bookings is 1 (FR-1.2). Therefore, if a `NEW` Seeker already has one active booking, a request to create a second booking must be rejected, even when the other conditions in FR-4.4 are satisfied.

## Activity 1.2 — Bug analysis

### Error - Fault - Failure

**Human error:**  
The developer likely used the wrong comparison operator when implementing FR-4.4 rule 2. The requirement says that a new booking is allowed only when the Seeker's number of active bookings is strictly less than the maximum allowed by the trust tier. However, the implementation only rejects the booking when the number of active bookings is greater than the maximum.

**Fault:**  
The fault is in `BookingService.createBooking()` in the check for FR-4.4 rule 2:

    if (seekerActive > seeker.getMaxConcurrentBookings()) {
        throw new BookingRejectedException(
            "Seeker booking limit reached for tier " + seeker.getTrustTier());
    }

The comparison uses `>` instead of `>=`. This is an off-by-one fault. When the number of active bookings is exactly equal to the allowed maximum, the condition evaluates to false and the system continues creating the new booking.

According to FR-4.4, a booking should only be accepted when:

    active bookings < trust-tier maximum

Therefore, reaching the maximum should already cause the new booking to be rejected.

**Failure:**  
A `NEW` Seeker is allowed a maximum of 1 concurrent active booking according to FR-1.2. If the Seeker already has 1 active booking, the faulty condition checks:

    1 > 1

This is false, so the booking request is allowed to continue. As a result, the `NEW` Seeker can create a second active booking and ends up with 2 active bookings, which violates FR-1.2 and FR-4.4.

### Test level and technique

A **unit test** of `BookingService.createBooking()` should have caught this fault because the incorrect behaviour is caused by the booking-limit condition inside this service.

**Boundary Value Analysis (BVA)** is an appropriate test technique because the fault occurs exactly at the boundary of the maximum number of active bookings. Tests should cover values just below the limit, exactly at the limit, and above the limit.

For example, for a `NEW` Seeker whose maximum is 1 active booking:

- 0 existing active bookings -> a new booking may be accepted if the other FR-4.4 conditions are satisfied.
- 1 existing active booking -> a new booking must be rejected.
- More than 1 active booking -> a new booking must also be rejected.

The case with exactly 1 active booking would expose the fault because the current implementation incorrectly allows another booking.
