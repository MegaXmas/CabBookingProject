package com.example.cabbooking.controller;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Location;
import com.example.cabbooking.service.BookingService;
import com.example.cabbooking.service.ClientService;
import com.example.cabbooking.service.LocationDistanceCalculatorService;
import com.example.cabbooking.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking/")
public class BookingController {

    private final BookingService bookingService;
    private final ClientService clientService;
    private final LocationService locationService;
    private final LocationDistanceCalculatorService locationDistanceCalculatorService;

    @Autowired
    public BookingController(BookingService bookingService, ClientService clientService,
                             LocationService locationService, LocationDistanceCalculatorService locationDistanceCalculatorService) {
        this.bookingService = bookingService;
        this.clientService = clientService;
        this.locationService = locationService;
        this.locationDistanceCalculatorService = locationDistanceCalculatorService;
    }


    @PostMapping
    public ResponseEntity<Location> bookCab(@RequestBody Client client, Location from, Location to) {

        bookingService.bookCab(client, from, to);

    }

}
