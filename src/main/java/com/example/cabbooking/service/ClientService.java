package com.example.cabbooking.service;

import java.util.List;
import java.util.Optional;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

   private final ClientRepository clientRepository;

   @Autowired
   public ClientService(ClientRepository clientRepository) {
      this.clientRepository = clientRepository;
   }

   public List<Client> getAllClients() {
      return clientRepository.findAll();
   }

   public Optional<Client> getClientById(Integer id) {
      return clientRepository.findById(id);
   }

   public boolean addClient(Client client) {
      return clientRepository.newClient(client);
   }

   public boolean updateClient(Client client) {
      return clientRepository.updateClient(client);
   }

   public void deleteClient(Integer id) {
      clientRepository.deleteClient(id);
   }
}
