package com.example.cabbooking.service;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final CalculateFareService calculateFareService;
    private final BookingService bookingService;

    // ✅ Custom exceptions for payment-related problems
    public static class InvalidPaymentException extends RuntimeException {
        public InvalidPaymentException(String message) {
            super(message);
        }
    }

    public static class PaymentProcessException extends RuntimeException {
        public PaymentProcessException(String message) {
            super(message);
        }
    }

    public static class CreditCardException extends RuntimeException {
        public CreditCardException(String message) {
            super(message);
        }
    }

    @Autowired
    PaymentService(CalculateFareService calculateFareService,
                   BookingService bookingService) {
        this.calculateFareService = calculateFareService;
        this.bookingService = bookingService;
    }

    public void requestPayment(Client client, Route route) {
        // ✅ Validate inputs
        validatePaymentInputs(client, route);

        try {
            double fare = calculateFareService.calculateFare(route);

            // ✅ Validate calculated fare
            if (fare <= 0) {
                throw new PaymentProcessException("Invalid fare calculated: " + fare);
            }

            System.out.println(client.getName() + ", please pay $" + String.format("%.2f", fare) +
                    " to finish booking your cab");
            System.out.println("Payment request sent to " + client.getEmail());

        } catch (CalculateFareService.FareCalculationException e) {
            throw new PaymentProcessException("Cannot request payment due to fare calculation error: " + e.getMessage());
        } catch (Exception e) {
            if (e instanceof InvalidPaymentException || e instanceof PaymentProcessException) {
                throw e; // Re-throw our custom exceptions
            }
            throw new PaymentProcessException("Failed to request payment: " + e.getMessage());
        }
    }

    public void paymentConfirmation(Client client, Route route, double paymentAmount, String creditCardNumber) {
        // ✅ Validate inputs
        validatePaymentConfirmationInputs(client, route, paymentAmount, creditCardNumber);

        try {
            double expectedFare = calculateFareService.calculateFare(route);

            // ✅ Validate credit card
            validateCreditCard(creditCardNumber, client.getCredit_card());

            // ✅ Validate payment amount with tolerance for floating point precision
            if (Math.abs(paymentAmount - expectedFare) > 0.01) {
                throw new InvalidPaymentException("Incorrect payment amount. Expected: $" +
                        String.format("%.2f", expectedFare) + ", Received: $" + String.format("%.2f", paymentAmount));
            }

            // ✅ Process successful payment
            System.out.println("✓ Payment from " + client.getName() + " confirmed");
            System.out.println("✓ $" + String.format("%.2f", expectedFare) +
                    " charged to card ending in " + getMaskedCardNumber(client.getCredit_card()));

            // Finish the booking
            bookingService.finishBookingCab(client, route);

            System.out.println("✓ Payment processing completed successfully");

        } catch (CalculateFareService.FareCalculationException e) {
            throw new PaymentProcessException("Cannot confirm payment due to fare calculation error: " + e.getMessage());
        } catch (BookingService.BookingProcessException e) {
            throw new PaymentProcessException("Payment processed but booking completion failed: " + e.getMessage());
        } catch (Exception e) {
            if (e instanceof InvalidPaymentException || e instanceof PaymentProcessException || e instanceof CreditCardException) {
                throw e; // Re-throw our custom exceptions
            }
            throw new PaymentProcessException("Failed to confirm payment: " + e.getMessage());
        }
    }

    // ✅ Helper method to validate payment inputs
    private void validatePaymentInputs(Client client, Route route) {
        if (client == null) {
            throw new InvalidPaymentException("Client cannot be null");
        }

        if (route == null) {
            throw new InvalidPaymentException("Route cannot be null");
        }

        if (client.getName() == null || client.getName().trim().isEmpty()) {
            throw new InvalidPaymentException("Client must have a valid name");
        }

        if (client.getEmail() == null || client.getEmail().trim().isEmpty()) {
            throw new InvalidPaymentException("Client must have a valid email for payment notifications");
        }
    }

    // ✅ Helper method to validate payment confirmation inputs
    private void validatePaymentConfirmationInputs(Client client, Route route, double paymentAmount, String creditCardNumber) {
        validatePaymentInputs(client, route);

        if (paymentAmount <= 0) {
            throw new InvalidPaymentException("Payment amount must be positive: " + paymentAmount);
        }

        if (Double.isNaN(paymentAmount) || Double.isInfinite(paymentAmount)) {
            throw new InvalidPaymentException("Payment amount is invalid: " + paymentAmount);
        }

        if (creditCardNumber == null || creditCardNumber.trim().isEmpty()) {
            throw new CreditCardException("Credit card number cannot be null or empty");
        }
    }

    // ✅ Helper method to validate credit card
    private void validateCreditCard(String providedCard, String clientCard) {
        if (clientCard == null || clientCard.trim().isEmpty()) {
            throw new CreditCardException("Client does not have a credit card on file");
        }

        // Remove any spaces or dashes for comparison
        String cleanProvidedCard = providedCard.replaceAll("[\\s-]", "");
        String cleanClientCard = clientCard.replaceAll("[\\s-]", "");

        if (!cleanProvidedCard.equals(cleanClientCard)) {
            throw new CreditCardException("Credit card number does not match card on file");
        }

        // Basic credit card validation (length check)
        if (cleanClientCard.length() < 13 || cleanClientCard.length() > 19) {
            throw new CreditCardException("Invalid credit card number format");
        }
    }

    // ✅ Helper method to mask credit card number for security
    private String getMaskedCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        String cleanCard = cardNumber.replaceAll("[\\s-]", "");
        if (cleanCard.length() < 4) {
            return "****";
        }

        return "****" + cleanCard.substring(cleanCard.length() - 4);
    }

    // ✅ Utility method to check if payment can be processed
    public boolean canProcessPayment(Client client, Route route) {
        try {
            validatePaymentInputs(client, route);

            // Check if client has credit card
            if (client.getCredit_card() == null || client.getCredit_card().trim().isEmpty()) {
                return false;
            }

            // Try to calculate fare
            calculateFareService.calculateFare(route);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    // ✅ Method to get payment summary
    public String getPaymentSummary(Client client, Route route) {
        validatePaymentInputs(client, route);

        try {
            double fare = calculateFareService.calculateFare(route);
            String maskedCard = getMaskedCardNumber(client.getCredit_card());

            return String.format("Payment Summary for %s:%nFare: $%.2f%nCard: %s%nEmail: %s",
                    client.getName(), fare, maskedCard, client.getEmail());

        } catch (Exception e) {
            throw new PaymentProcessException("Failed to generate payment summary: " + e.getMessage());
        }
    }
}