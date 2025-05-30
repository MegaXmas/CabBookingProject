package com.example.cabbooking.service;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Location;
import com.example.cabbooking.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final CalculateFareService calculateFareService;
    private final LocationDistanceCalculatorService locationDistanceCalculatorService;
    private ClientRepository clientRepository;

    @Autowired
    public BookingService(ClientRepository clientRepository, CalculateFareService calculateFareService,
                          LocationDistanceCalculatorService locationDistanceCalculatorService) {
        this.clientRepository = clientRepository;
        this.calculateFareService = calculateFareService;
        this.locationDistanceCalculatorService = locationDistanceCalculatorService;
    }

    public void bookCab(Client client, Location from, Location to) {
        System.out.println("Booking cab from " + from + " to " + to );
        System.out.println("Booking cab to client " + client );

        LocationDistanceCalculatorService.calculateDistance(from, to);
        LocationDistanceCalculatorService.printDistanceReport(from, to);

        calculateFareService.calculateFare(locationDistanceCalculatorService.miles);
    }

}
