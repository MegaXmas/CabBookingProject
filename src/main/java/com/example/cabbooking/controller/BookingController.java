package com.example.cabbooking.controller;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Route;
import com.example.cabbooking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/booking/")
public class BookingController {

    private final BookingService bookingService;
    private final CalculateFareService calculateFareService;

    @Autowired
    public BookingController(BookingService bookingService, CalculateFareService calculateFareService) {
        this.bookingService = bookingService;
        this.calculateFareService = calculateFareService;
    }

    @PostMapping
    public void bookCab(@RequestBody Client client, Route route) {

        bookingService.bookCab(client, route);
        calculateFareService.calculateFare(route);
    }
}
