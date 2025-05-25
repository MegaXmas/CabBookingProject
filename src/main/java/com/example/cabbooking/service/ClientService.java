package com.example.cabbooking.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.repository.ClientRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

   List<Client> getAllClients() {
      return ClientRepository.
   }

   ;
   Optional<Client> getClientById(int id);
   Client addClient(Client client);
   Client updateClient(int id, Client client);
}
