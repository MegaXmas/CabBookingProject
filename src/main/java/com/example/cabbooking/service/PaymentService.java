package com.example.cabbooking.service;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final CalculateFareService calculateFareService;
    private final BookingService bookingService;

    // Custom exceptions for payment-related problems
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

    /**
     * requests a payment from the client calculated based on the route parameter
     * @param client client who is paying the cab fare
     * @param route the distance of this route will be used to calculate the fare
     */
    public void requestPayment(Client client, Route route) {
        // ✅ Validate inputs
        validatePaymentInputs(client, route);

        try {
            double fare = calculateFareService.calculateFare(route);

            // Validate calculated fare
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
                throw e;
            }
            throw new PaymentProcessException("Failed to request payment: " + e.getMessage());
        }
    }

    /**
     * confirming that the fare was successfully paid or if there were errors
     * @param client client who is paying the cab fare
     * @param route the distance of this route will be used to calculate the fare
     * @param paymentAmount the dollar amount the client is to pay to finish booking their cab
     * @param creditCardNumber confirmation that the credit card used to pay the fare matches the credit card
     *                         the client has on file
     */
    public void paymentConfirmation(Client client, Route route, double paymentAmount, String creditCardNumber) {
        // Validate inputs
        validatePaymentConfirmationInputs(client, route, paymentAmount, creditCardNumber);

        try {
            double expectedFare = calculateFareService.calculateFare(route);

            // Validate credit card
            validateCreditCard(creditCardNumber, client.getCredit_card());

            // Validate payment amount with tolerance for floating point precision
            if (Math.abs(paymentAmount - expectedFare) > 0.01) {
                throw new InvalidPaymentException("Incorrect payment amount. Expected: $" +
                        String.format("%.2f", expectedFare) + ", Received: $" + String.format("%.2f", paymentAmount));
            }

            // Process successful payment
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
                throw e;
            }
            throw new PaymentProcessException("Failed to confirm payment: " + e.getMessage());
        }
    }

    /**
     * helper method to validate payment inputs
     * @param client client paying the fare, whose information is to be validated
     * @param route the route that is to be validated as not null
     */
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

    /**
     * helper method to validate payment confirmation inputs
     * @param client client paying the fare
     * @param route route that the fare amount is being calculated from
     * @param paymentAmount confirming that the paymentAmount has is a valid amount
     * @param creditCardNumber to be validated as not null/ not empty
     */
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

    /**
     * helper method which removes any spaces or dashes from the credit card Strings for proper formating
     * and then validates the credit card
     * @param providedCard credit card provided to pay the cab fare which must match the client's card on file
     * @param clientCard credit card the client has on file which must match the provided card
     */
    private void validateCreditCard(String providedCard, String clientCard) {
        if (clientCard == null || clientCard.trim().isEmpty()) {
            throw new CreditCardException("Client does not have a credit card on file");
        }

        // Remove any spaces or dashes for proper formating
        String cleanProvidedCard = providedCard.replaceAll("[\\s-]", "");
        String cleanClientCard = clientCard.replaceAll("[\\s-]", "");

        if (!cleanProvidedCard.equals(cleanClientCard)) {
            throw new CreditCardException("Credit card number does not match card on file");
        }

        // Credit card length validation
        if (cleanClientCard.length() < 13 || cleanClientCard.length() > 19) {
            throw new CreditCardException("Invalid credit card number format");
        }
    }

    /**
     * helper method to mask the credit card number for security purposes
     * @param cardNumber credit card number to be masked
     * @return masked credit card number
     */
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

    /**
     * Utility method to check if payment can be processed
     * @param client client paying the fare
     * @param route route that the fare amount is being calculated from
     * @return true if payment is processed successfully, false if otherwise
     */
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

    /**
     * generates a payment summary for the client who booked the cab
     * @param client client who is paying the cab fare
     * @param route route which the cab fare is calculated from
     * @return generated payment summary
     */
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