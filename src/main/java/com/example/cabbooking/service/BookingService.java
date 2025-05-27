package com.example.cabbooking.service;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private ClientRepository clientRepository;

    @Autowired
    public BookingService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void bookCab(Client client, int miles) {
        System.out.println("Booking cab " + miles + " miles");
    }

}
