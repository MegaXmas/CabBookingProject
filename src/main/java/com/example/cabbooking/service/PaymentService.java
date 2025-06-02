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

    @Autowired
    PaymentService(RouteService routeService, CalculateFareService calculateFareService) {
        this.routeService = routeService;
        this.calculateFareService = calculateFareService;
    }

    public void requestPayment(Client client, Route route) {

        System.out.println(client.getName() + " please pay " +
                calculateFareService.calculateFare(route) + " to finish booking your cab");
    }



}
