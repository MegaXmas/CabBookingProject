package com.example.cabbooking.service;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService {

    private final RouteService routeService;
    private final CalculateFareService calculateFareService;
    private final BookingService bookingService;

    @Autowired
    PaymentService(RouteService routeService, CalculateFareService calculateFareService,
                   BookingService bookingService) {
        this.routeService = routeService;
        this.calculateFareService = calculateFareService;
        this.bookingService = bookingService;
    }

    public void requestPayment(Client client, Route route) {
        System.out.println(client.getName() + " please pay " +
                calculateFareService.calculateFare(route) + " to finish booking your cab");
    }

    public void paymentConfirmation(Client client, Route route, double paymentAmount, int creditCardNumber) {

        if (creditCardNumber != client.getCredit_card()) {
            throw new RuntimeException("invalid credit card number");
        } else if (paymentAmount == calculateFareService.calculateFare(route)) {
            System.out.println("payment from " + client.getName() + " confirmed");
            System.out.println(calculateFareService.calculateFare(route) + " charged to card: " + client.getCredit_card());
            bookingService.finishBookingCab(client, route);
        } else {
            throw new IllegalArgumentException("incorrect payment amount");
        }
    }
}
