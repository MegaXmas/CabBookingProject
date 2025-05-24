package com.example.cabbooking.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.cabbooking.model.Client;

public interface ClientService {

   List<Client> getAllClients();
   Optional<Client> getClientById(int id);
   Client addClient(Client client);
}
