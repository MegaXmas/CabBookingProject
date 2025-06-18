package com.example.cabbooking.service;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private CalculateFareService calculateFareService;

    @Mock
    private BookingService bookingService;

    private PaymentService paymentService;
    private Client validClient;
    private Route validRoute;
    private Location startLocation;
    private Location endLocation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentService(calculateFareService, bookingService);

        // Set up test data
        validClient = new Client(1, "John Doe", "john@email.com", "555-1234", "123 Main St", "4111-1111-1111-1111");
        startLocation = new Location("Central Park", 40.7829, -73.9654);
        endLocation = new Location("Times Square", 40.7580, -73.9855);
        validRoute = new Route(startLocation, endLocation, 2.5);

        // Set up default mock behavior
        when(calculateFareService.calculateFare(validRoute)).thenReturn(10.0);
    }

    // =================== SUCCESS CASE TESTS ===================

    @Test
    void testRequestPaymentSuccess() {
        // Act
        assertDoesNotThrow(() -> {
            paymentService.requestPayment(validClient, validRoute);
        });

        // Verify fare calculation was called
        verify(calculateFareService).calculateFare(validRoute);
    }

    @Test
    void testPaymentConfirmationSuccess() {
        // Act
        assertDoesNotThrow(() -> {
            paymentService.paymentConfirmation(validClient, validRoute, 10.0, "4111-1111-1111-1111");
        });

        // Verify all expected method calls
        verify(calculateFareService).calculateFare(validRoute);
        verify(bookingService).finishBookingCab(validClient, validRoute);
    }

    @Test
    void testPaymentConfirmationWithSpacesInCardNumber() {
        // Test that card numbers with spaces/dashes work
        assertDoesNotThrow(() -> {
            paymentService.paymentConfirmation(validClient, validRoute, 10.0, "4111 1111 1111 1111");
        });

        verify(bookingService).finishBookingCab(validClient, validRoute);
    }

    @Test
    void testPaymentConfirmationWithDashesInCardNumber() {
        // Test that card numbers with dashes work
        assertDoesNotThrow(() -> {
            paymentService.paymentConfirmation(validClient, validRoute, 10.0, "4111-1111-1111-1111");
        });

        verify(bookingService).finishBookingCab(validClient, validRoute);
    }

    @Test
    void testCanProcessPaymentSuccess() {
        // Act
        boolean canProcess = paymentService.canProcessPayment(validClient, validRoute);

        // Assert
        assertTrue(canProcess);
        verify(calculateFareService).calculateFare(validRoute);
    }

    @Test
    void testGetPaymentSummarySuccess() {
        // Act
        String summary = paymentService.getPaymentSummary(validClient, validRoute);

        // Assert
        assertNotNull(summary);
        assertTrue(summary.contains("John Doe"));
        assertTrue(summary.contains("$10.00"));
        assertTrue(summary.contains("****1111")); // Masked card number
        assertTrue(summary.contains("john@email.com"));

        System.out.println("Payment Summary:");
        System.out.println(summary);

        verify(calculateFareService).calculateFare(validRoute);
    }

    // =================== REQUEST PAYMENT EXCEPTION TESTS ===================

    @Test
    void testRequestPaymentWithNullClientShouldThrowException() {
        PaymentService.InvalidPaymentException exception = assertThrows(
                PaymentService.InvalidPaymentException.class,
                () -> paymentService.requestPayment(null, validRoute)
        );

        assertEquals("Client cannot be null", exception.getMessage());

        // Verify fare calculation was never called
        verify(calculateFareService, never()).calculateFare(any());
        System.out.println(exception.getMessage());
    }

    @Test
    void testRequestPaymentWithNullRouteShouldThrowException() {
        PaymentService.InvalidPaymentException exception = assertThrows(
                PaymentService.InvalidPaymentException.class,
                () -> paymentService.requestPayment(validClient, null)
        );

        assertEquals("Route cannot be null", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testRequestPaymentWithInvalidClientNameShouldThrowException() {
        Client invalidClient = new Client(1, "   ", "john@email.com", "555-1234", "123 Main St", "4111-1111-1111-1111");

        PaymentService.InvalidPaymentException exception = assertThrows(
                PaymentService.InvalidPaymentException.class,
                () -> paymentService.requestPayment(invalidClient, validRoute)
        );

        assertEquals("Client must have a valid name", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testRequestPaymentWithInvalidClientEmailShouldThrowException() {
        Client invalidClient = new Client(1, "John Doe", "   ", "555-1234", "123 Main St", "4111-1111-1111-1111");

        PaymentService.InvalidPaymentException exception = assertThrows(
                PaymentService.InvalidPaymentException.class,
                () -> paymentService.requestPayment(invalidClient, validRoute)
        );

        assertEquals("Client must have a valid email for payment notifications", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testRequestPaymentWhenFareCalculationFailsShouldThrowException() {
        when(calculateFareService.calculateFare(validRoute))
                .thenThrow(new CalculateFareService.FareCalculationException("Fare calculation failed"));

        PaymentService.PaymentProcessException exception = assertThrows(
                PaymentService.PaymentProcessException.class,
                () -> paymentService.requestPayment(validClient, validRoute)
        );

        assertTrue(exception.getMessage().contains("Cannot request payment due to fare calculation error"));
        assertTrue(exception.getMessage().contains("Fare calculation failed"));
        System.out.println(exception.getMessage());
    }

    @Test
    void testRequestPaymentWithZeroFareShouldThrowException() {
        when(calculateFareService.calculateFare(validRoute)).thenReturn(0.0);

        PaymentService.PaymentProcessException exception = assertThrows(
                PaymentService.PaymentProcessException.class,
                () -> paymentService.requestPayment(validClient, validRoute)
        );

        assertTrue(exception.getMessage().contains("Invalid fare calculated: 0.0"));
        System.out.println(exception.getMessage());
    }

    @Test
    void testRequestPaymentWithNegativeFareShouldThrowException() {
        when(calculateFareService.calculateFare(validRoute)).thenReturn(-5.0);

        PaymentService.PaymentProcessException exception = assertThrows(
                PaymentService.PaymentProcessException.class,
                () -> paymentService.requestPayment(validClient, validRoute)
        );

        assertTrue(exception.getMessage().contains("Invalid fare calculated: -5.0"));
        System.out.println(exception.getMessage());
    }

    // =================== PAYMENT CONFIRMATION EXCEPTION TESTS ===================

    @Test
    void testPaymentConfirmationWithNullClientShouldThrowException() {
        PaymentService.InvalidPaymentException exception = assertThrows(
                PaymentService.InvalidPaymentException.class,
                () -> paymentService.paymentConfirmation(null, validRoute, 10.0, "4111-1111-1111-1111")
        );

        assertEquals("Client cannot be null", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testPaymentConfirmationWithNullRouteShouldThrowException() {
        PaymentService.InvalidPaymentException exception = assertThrows(
                PaymentService.InvalidPaymentException.class,
                () -> paymentService.paymentConfirmation(validClient, null, 10.0, "4111-1111-1111-1111")
        );

        assertEquals("Route cannot be null", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testPaymentConfirmationWithNegativePaymentAmountShouldThrowException() {
        PaymentService.InvalidPaymentException exception = assertThrows(
                PaymentService.InvalidPaymentException.class,
                () -> paymentService.paymentConfirmation(validClient, validRoute, -5.0, "4111-1111-1111-1111")
        );

        assertTrue(exception.getMessage().contains("Payment amount must be positive"));
        System.out.println(exception.getMessage());
    }

    @Test
    void testPaymentConfirmationWithZeroPaymentAmountShouldThrowException() {
        PaymentService.InvalidPaymentException exception = assertThrows(
                PaymentService.InvalidPaymentException.class,
                () -> paymentService.paymentConfirmation(validClient, validRoute, 0.0, "4111-1111-1111-1111")
        );

        assertTrue(exception.getMessage().contains("Payment amount must be positive"));
        System.out.println(exception.getMessage());
    }

    @Test
    void testPaymentConfirmationWithNaNPaymentAmountShouldThrowException() {
        PaymentService.InvalidPaymentException exception = assertThrows(
                PaymentService.InvalidPaymentException.class,
                () -> paymentService.paymentConfirmation(validClient, validRoute, Double.NaN, "4111-1111-1111-1111")
        );

        assertTrue(exception.getMessage().contains("Payment amount is invalid"));
        System.out.println(exception.getMessage());
    }

    @Test
    void testPaymentConfirmationWithNullCreditCardShouldThrowException() {
        PaymentService.CreditCardException exception = assertThrows(
                PaymentService.CreditCardException.class,
                () -> paymentService.paymentConfirmation(validClient, validRoute, 10.0, null)
        );

        assertEquals("Credit card number cannot be null or empty", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testPaymentConfirmationWithEmptyCreditCardShouldThrowException() {
        PaymentService.CreditCardException exception = assertThrows(
                PaymentService.CreditCardException.class,
                () -> paymentService.paymentConfirmation(validClient, validRoute, 10.0, "   ")
        );

        assertEquals("Credit card number cannot be null or empty", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testPaymentConfirmationWithClientHavingNoCreditCardShouldThrowException() {
        Client clientWithoutCard = new Client(1, "John Doe", "john@email.com", "555-1234", "123 Main St", null);

        PaymentService.CreditCardException exception = assertThrows(
                PaymentService.CreditCardException.class,
                () -> paymentService.paymentConfirmation(clientWithoutCard, validRoute, 10.0, "4111-1111-1111-1111")
        );

        assertEquals("Client does not have a credit card on file", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testPaymentConfirmationWithIncorrectCreditCardShouldThrowException() {
        PaymentService.CreditCardException exception = assertThrows(
                PaymentService.CreditCardException.class,
                () -> paymentService.paymentConfirmation(validClient, validRoute, 10.0, "5555-5555-5555-4444")
        );

        assertEquals("Credit card number does not match card on file", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testPaymentConfirmationWithIncorrectPaymentAmountShouldThrowException() {
        PaymentService.InvalidPaymentException exception = assertThrows(
                PaymentService.InvalidPaymentException.class,
                () -> paymentService.paymentConfirmation(validClient, validRoute, 15.0, "4111-1111-1111-1111") // Expected 10.0
        );

        assertTrue(exception.getMessage().contains("Incorrect payment amount"));
        assertTrue(exception.getMessage().contains("Expected: $10.00"));
        assertTrue(exception.getMessage().contains("Received: $15.00"));
        System.out.println(exception.getMessage());
    }

    @Test
    void testPaymentConfirmationWithSmallPaymentDifferenceShouldSucceed() {
        // Test that small differences (within 1 cent) are allowed for floating point precision
        assertDoesNotThrow(() -> {
            paymentService.paymentConfirmation(validClient, validRoute, 10.005, "4111-1111-1111-1111");
        });

        verify(bookingService).finishBookingCab(validClient, validRoute);
    }

    @Test
    void testPaymentConfirmationWhenBookingFailsShouldThrowException() {
        doThrow(new BookingService.BookingProcessException("Booking failed"))
                .when(bookingService).finishBookingCab(validClient, validRoute);

        PaymentService.PaymentProcessException exception = assertThrows(
                PaymentService.PaymentProcessException.class,
                () -> paymentService.paymentConfirmation(validClient, validRoute, 10.0, "4111-1111-1111-1111")
        );

        assertTrue(exception.getMessage().contains("Payment processed but booking completion failed"));
        assertTrue(exception.getMessage().contains("Booking failed"));
        System.out.println(exception.getMessage());
    }

    // =================== UTILITY METHOD TESTS ===================

    @Test
    void testCanProcessPaymentWithInvalidInputs() {
        // Test with null client
        assertFalse(paymentService.canProcessPayment(null, validRoute));

        // Test with null route
        assertFalse(paymentService.canProcessPayment(validClient, null));

        // Test with client having no credit card
        Client clientWithoutCard = new Client(1, "John", "john@email.com", "555-1234", "123 Main St", "");
        assertFalse(paymentService.canProcessPayment(clientWithoutCard, validRoute));
    }

    @Test
    void testCanProcessPaymentWhenFareCalculationFails() {
        when(calculateFareService.calculateFare(validRoute))
                .thenThrow(new CalculateFareService.FareCalculationException("Calculation error"));

        boolean canProcess = paymentService.canProcessPayment(validClient, validRoute);

        assertFalse(canProcess);
    }

    @Test
    void testGetPaymentSummaryWithInvalidClientShouldThrowException() {
        Client invalidClient = new Client(1, null, "john@email.com", "555-1234", "123 Main St", "4111-1111-1111-1111");

        PaymentService.InvalidPaymentException exception = assertThrows(
                PaymentService.InvalidPaymentException.class,
                () -> paymentService.getPaymentSummary(invalidClient, validRoute)
        );

        assertEquals("Client must have a valid name", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testGetPaymentSummaryWhenFareCalculationFailsShouldThrowException() {
        when(calculateFareService.calculateFare(validRoute))
                .thenThrow(new CalculateFareService.FareCalculationException("Calculation error"));

        PaymentService.PaymentProcessException exception = assertThrows(
                PaymentService.PaymentProcessException.class,
                () -> paymentService.getPaymentSummary(validClient, validRoute)
        );

        assertTrue(exception.getMessage().contains("Failed to generate payment summary"));
        System.out.println(exception.getMessage());
    }

    // =================== EDGE CASE TESTS ===================

    @Test
    void testPaymentConfirmationWithInvalidCardFormatShouldThrowException() {
        Client clientWithBadCard = new Client(1, "John", "john@email.com", "555-1234", "123 Main St", "123"); // Too short

        PaymentService.CreditCardException exception = assertThrows(
                PaymentService.CreditCardException.class,
                () -> paymentService.paymentConfirmation(clientWithBadCard, validRoute, 10.0, "123")
        );

        assertEquals("Invalid credit card number format", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testPaymentConfirmationWithVeryLongCardNumberShouldThrowException() {
        String veryLongCard = "41111111111111111111111"; // Too long
        Client clientWithBadCard = new Client(1, "John", "john@email.com", "555-1234", "123 Main St", veryLongCard);

        PaymentService.CreditCardException exception = assertThrows(
                PaymentService.CreditCardException.class,
                () -> paymentService.paymentConfirmation(clientWithBadCard, validRoute, 10.0, veryLongCard)
        );

        assertEquals("Invalid credit card number format", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testCompletePaymentWorkflow() {
        // Test request payment followed by confirmation
        assertDoesNotThrow(() -> {
            paymentService.requestPayment(validClient, validRoute);
            paymentService.paymentConfirmation(validClient, validRoute, 10.0, "4111-1111-1111-1111");
        });

        // Verify all expected interactions
        verify(calculateFareService, times(2)).calculateFare(validRoute);
        verify(bookingService).finishBookingCab(validClient, validRoute);
    }

    @Test
    void testPaymentWithHighFareAmount() {
        when(calculateFareService.calculateFare(validRoute)).thenReturn(9999.99);

        assertDoesNotThrow(() -> {
            paymentService.paymentConfirmation(validClient, validRoute, 9999.99, "4111-1111-1111-1111");
        });

        verify(bookingService).finishBookingCab(validClient, validRoute);
    }
}