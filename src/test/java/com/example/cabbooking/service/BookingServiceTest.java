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

class BookingServiceTest {

    @Mock
    private RouteService routeService;

    @Mock
    private LocationDistanceCalculatorService locationDistanceCalculatorService;

    private BookingService bookingService;
    private Client validClient;
    private Location startLocation;
    private Location endLocation;
    private Route validRoute;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookingService = new BookingService(routeService, locationDistanceCalculatorService);

        // Set up test data
        validClient = new Client(1, "John Doe", "john@email.com", "555-1234", "123 Main St", "4111-1111-1111-1111");
        startLocation = new Location("Central Park", 40.7829, -73.9654);
        endLocation = new Location("Times Square", 40.7580, -73.9855);
        validRoute = new Route(startLocation, endLocation, 2.5);

        // Set up default mock behavior
        when(routeService.getRouteLocationFrom(validRoute)).thenReturn(startLocation);
        when(routeService.getRouteLocationTo(validRoute)).thenReturn(endLocation);
        when(routeService.getRouteDistance(validRoute)).thenReturn(2.5);
        when(locationDistanceCalculatorService.calculateDistanceUsingLocation(startLocation, endLocation)).thenReturn(2.5);
        when(locationDistanceCalculatorService.printDistanceReport(startLocation, endLocation)).thenReturn(1.55); // miles
    }

    // =================== SUCCESS CASE TESTS ===================

    @Test
    void testBookCabSuccess() {
        // Act
        assertDoesNotThrow(() -> {
            bookingService.bookCab(validClient, validRoute);
        });

        // Verify all expected method calls
        verify(routeService).getRouteLocationFrom(validRoute);
        verify(routeService).getRouteLocationTo(validRoute);
        verify(routeService).getRouteDistance(validRoute);
        verify(locationDistanceCalculatorService).calculateDistanceUsingLocation(startLocation, endLocation);
        verify(locationDistanceCalculatorService).printDistanceReport(startLocation, endLocation);
    }

    @Test
    void testFinishBookingCabSuccess() {
        // Act
        assertDoesNotThrow(() -> {
            bookingService.finishBookingCab(validClient, validRoute);
        });

        // Verify expected method calls
        verify(routeService).getRouteLocationFrom(validRoute);
        verify(routeService).getRouteLocationTo(validRoute);
    }

    @Test
    void testGetBookingSummarySuccess() {
        // Act
        String summary = bookingService.getBookingSummary(validClient, validRoute);

        // Assert
        assertNotNull(summary);
        assertTrue(summary.contains("John Doe"));
        assertTrue(summary.contains("Central Park"));
        assertTrue(summary.contains("Times Square"));
        assertTrue(summary.contains("john@email.com"));
        assertTrue(summary.contains("2.5"));

        System.out.println("Booking Summary:");
        System.out.println(summary);
    }

    @Test
    void testIsValidBookingSuccess() {
        // Act
        boolean isValid = bookingService.isValidBooking(validClient, validRoute);

        // Assert
        assertTrue(isValid);
    }

    // =================== BOOK CAB EXCEPTION TESTS ===================

    @Test
    void testBookCabWithNullClientShouldThrowException() {
        BookingService.InvalidBookingException exception = assertThrows(
                BookingService.InvalidBookingException.class,
                () -> bookingService.bookCab(null, validRoute)
        );

        assertEquals("Client cannot be null", exception.getMessage());

        // Verify no service methods were called
        verify(routeService, never()).getRouteLocationFrom(any());
        verify(locationDistanceCalculatorService, never()).calculateDistanceUsingLocation(any(), any());
        System.out.println(exception.getMessage());
    }

    @Test
    void testBookCabWithNullRouteShouldThrowException() {
        BookingService.InvalidBookingException exception = assertThrows(
                BookingService.InvalidBookingException.class,
                () -> bookingService.bookCab(validClient, null)
        );

        assertEquals("Route cannot be null", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testBookCabWithInvalidClientIdShouldThrowException() {
        Client invalidClient = new Client(-1, "John Doe", "john@email.com", "555-1234", "123 Main St", "4111-1111");

        BookingService.InvalidBookingException exception = assertThrows(
                BookingService.InvalidBookingException.class,
                () -> bookingService.bookCab(invalidClient, validRoute)
        );

        assertEquals("Client must have a valid ID", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testBookCabWithInvalidClientNameShouldThrowException() {
        Client invalidClient = new Client(1, "   ", "john@email.com", "555-1234", "123 Main St", "4111-1111");

        BookingService.InvalidBookingException exception = assertThrows(
                BookingService.InvalidBookingException.class,
                () -> bookingService.bookCab(invalidClient, validRoute)
        );

        assertEquals("Client must have a valid name", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testBookCabWithInvalidClientEmailShouldThrowException() {
        Client invalidClient = new Client(1, "John Doe", "invalid-email", "555-1234", "123 Main St", "4111-1111");

        BookingService.InvalidBookingException exception = assertThrows(
                BookingService.InvalidBookingException.class,
                () -> bookingService.bookCab(invalidClient, validRoute)
        );

        assertTrue(exception.getMessage().contains("Client email must be valid"));
        System.out.println(exception.getMessage());
    }

    @Test
    void testBookCabWithNullRouteLocationsShouldThrowException() {
        Route badRoute = new Route(null, null, 5.0);

        BookingService.InvalidBookingException exception = assertThrows(
                BookingService.InvalidBookingException.class,
                () -> bookingService.bookCab(validClient, badRoute)
        );

        assertEquals("Route must have valid starting and destination locations", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testBookCabWithNegativeDistanceShouldThrowException() {
        Route badRoute = new Route(startLocation, endLocation, -5.0);

        BookingService.InvalidBookingException exception = assertThrows(
                BookingService.InvalidBookingException.class,
                () -> bookingService.bookCab(validClient, badRoute)
        );

        assertTrue(exception.getMessage().contains("Route distance cannot be negative"));
        System.out.println(exception.getMessage());
    }

    @Test
    void testBookCabWithSameLocationsShouldThrowException() {
        when(routeService.getRouteLocationFrom(validRoute)).thenReturn(startLocation);
        when(routeService.getRouteLocationTo(validRoute)).thenReturn(startLocation); // Same location

        BookingService.InvalidBookingException exception = assertThrows(
                BookingService.InvalidBookingException.class,
                () -> bookingService.bookCab(validClient, validRoute)
        );

        assertTrue(exception.getMessage().contains("Cannot book cab for same pickup and destination location"));
        System.out.println(exception.getMessage());
    }

    @Test
    void testBookCabWhenRouteServiceThrowsExceptionShouldThrowBookingProcessException() {
        when(routeService.getRouteLocationFrom(validRoute))
                .thenThrow(new RouteService.InvalidRouteException("Route is invalid"));

        BookingService.BookingProcessException exception = assertThrows(
                BookingService.BookingProcessException.class,
                () -> bookingService.bookCab(validClient, validRoute)
        );

        assertTrue(exception.getMessage().contains("Cannot book cab due to invalid route"));
        assertTrue(exception.getMessage().contains("Route is invalid"));
        System.out.println(exception.getMessage());
    }

    @Test
    void testBookCabWhenLocationServiceThrowsExceptionShouldThrowBookingProcessException() {
        when(locationDistanceCalculatorService.calculateDistanceUsingLocation(startLocation, endLocation))
                .thenThrow(new LocationDistanceCalculatorService.InvalidLocationException("Location is invalid"));

        BookingService.BookingProcessException exception = assertThrows(
                BookingService.BookingProcessException.class,
                () -> bookingService.bookCab(validClient, validRoute)
        );

        assertTrue(exception.getMessage().contains("Cannot book cab due to invalid location"));
        assertTrue(exception.getMessage().contains("Location is invalid"));
        System.out.println(exception.getMessage());
    }

    // =================== FINISH BOOKING EXCEPTION TESTS ===================

    @Test
    void testFinishBookingCabWithNullClientShouldThrowException() {
        BookingService.InvalidBookingException exception = assertThrows(
                BookingService.InvalidBookingException.class,
                () -> bookingService.finishBookingCab(null, validRoute)
        );

        assertEquals("Client cannot be null", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testFinishBookingCabWithNullRouteShouldThrowException() {
        BookingService.InvalidBookingException exception = assertThrows(
                BookingService.InvalidBookingException.class,
                () -> bookingService.finishBookingCab(validClient, null)
        );

        assertEquals("Route cannot be null", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testFinishBookingCabWhenRouteServiceThrowsExceptionShouldThrowBookingProcessException() {
        when(routeService.getRouteLocationFrom(validRoute))
                .thenThrow(new RouteService.InvalidRouteException("Route is invalid"));

        BookingService.BookingProcessException exception = assertThrows(
                BookingService.BookingProcessException.class,
                () -> bookingService.finishBookingCab(validClient, validRoute)
        );

        assertTrue(exception.getMessage().contains("Cannot finish booking due to invalid route"));
        System.out.println(exception.getMessage());
    }

    // =================== UTILITY METHOD TESTS ===================

    @Test
    void testIsValidBookingWithInvalidInputs() {
        // Test with null client
        assertFalse(bookingService.isValidBooking(null, validRoute));

        // Test with null route
        assertFalse(bookingService.isValidBooking(validClient, null));

        // Test with invalid client email
        Client invalidClient = new Client(1, "John", "no-at-sign", "555-1234", "123 Main St", "4111-1111");
        assertFalse(bookingService.isValidBooking(invalidClient, validRoute));
    }

    @Test
    void testGetBookingSummaryWithInvalidClientShouldThrowException() {
        Client invalidClient = new Client(1, null, "john@email.com", "555-1234", "123 Main St", "4111-1111");

        BookingService.InvalidBookingException exception = assertThrows(
                BookingService.InvalidBookingException.class,
                () -> bookingService.getBookingSummary(invalidClient, validRoute)
        );

        assertEquals("Client must have a valid name", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    void testGetBookingSummaryWhenRouteServiceThrowsException() {
        when(routeService.getRouteLocationFrom(validRoute))
                .thenThrow(new RouteService.InvalidRouteException("Route error"));

        BookingService.BookingProcessException exception = assertThrows(
                BookingService.BookingProcessException.class,
                () -> bookingService.getBookingSummary(validClient, validRoute)
        );

        assertTrue(exception.getMessage().contains("Failed to generate booking summary"));
        System.out.println(exception.getMessage());
    }

    // =================== EDGE CASE TESTS ===================

    @Test
    void testBookCabWithMinimalValidData() {
        Client minimalClient = new Client(1, "A", "a@b.com", null, null, null);

        assertDoesNotThrow(() -> {
            bookingService.bookCab(minimalClient, validRoute);
        });
    }

    @Test
    void testBookCabWithDistanceMismatchWarning() {
        // Set up distance mismatch (calculated vs route distance)
        when(locationDistanceCalculatorService.calculateDistanceUsingLocation(startLocation, endLocation))
                .thenReturn(10.0); // Very different from route distance (2.5)

        // Should not throw exception, but should print warning
        assertDoesNotThrow(() -> {
            bookingService.bookCab(validClient, validRoute);
        });

        verify(locationDistanceCalculatorService).calculateDistanceUsingLocation(startLocation, endLocation);
    }

    @Test
    void testBookCabWithZeroDistance() {
        Route zeroRoute = new Route(startLocation, endLocation, 0.0);
        when(routeService.getRouteLocationFrom(zeroRoute)).thenReturn(startLocation);
        when(routeService.getRouteLocationTo(zeroRoute)).thenReturn(endLocation);
        when(routeService.getRouteDistance(zeroRoute)).thenReturn(0.0);
        when(locationDistanceCalculatorService.calculateDistanceUsingLocation(startLocation, endLocation)).thenReturn(0.0);

        assertDoesNotThrow(() -> {
            bookingService.bookCab(validClient, zeroRoute);
        });
    }

    @Test
    void testCompleteBookingWorkflow() {
        // Test booking and then finishing
        assertDoesNotThrow(() -> {
            bookingService.bookCab(validClient, validRoute);
            bookingService.finishBookingCab(validClient, validRoute);
        });

        // Verify all expected interactions
        verify(routeService, times(2)).getRouteLocationFrom(validRoute);
        verify(routeService, times(2)).getRouteLocationTo(validRoute);
        verify(routeService).getRouteDistance(validRoute);
        verify(locationDistanceCalculatorService).calculateDistanceUsingLocation(startLocation, endLocation);
        verify(locationDistanceCalculatorService).printDistanceReport(startLocation, endLocation);
    }
}