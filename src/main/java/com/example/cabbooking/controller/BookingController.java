package com.example.cabbooking.controller;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Location;
import com.example.cabbooking.service.BookingService;
import com.example.cabbooking.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking/")
public class BookingController {

    private final BookingService bookingService;
    private final ClientService clientService;

    @Autowired
    public BookingController(BookingService bookingService, ClientService clientService) {
        this.bookingService = bookingService;
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<Location> bookCab(@RequestBody Client client, double latitude, double longitude, String locationName) {
        bookingService.
    }

}
