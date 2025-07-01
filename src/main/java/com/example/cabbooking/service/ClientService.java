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

   /**
    * Get all clients from the database
    * @return List of all clients (empty list if none found or error occurs
    */
   public List<Client> getAllClients() {
      return clientRepository.findAll();
   }

   /**
    * Get a client by their ID
    * @param id The client ID to search for
    * @return Optional containing the client if found, empty otherwise
    */
   public Optional<Client> getClientById(Integer id) {
      if (id == null) {
         System.out.println("✗ Service Error: Client ID cannot be null");
         return Optional.empty();
      }
      return clientRepository.findById(id);
   }

   /**
    * Add a new client to the database
    * @param client The client to add
    * @return true if client was successfully added, false otherwise
    */
   public boolean addClient(Client client) {
      if (client == null) {
         System.out.println("✗ Service Error: Cannot add null client");
         return false;
      }

      boolean success = clientRepository.newClient(client);
      if (success) {
         System.out.println("✓ Service: Client successfully added through service layer");
      } else {
         System.out.println("✗ Service: Failed to add client through service layer");
      }
      return success;
   }

   /**
    * Update an existing client in the database
    * @param client The client with updated information
    * @return true if client was successfully updated, false otherwise
    */
   public boolean updateClient(Client client) {
      if (client == null) {
         System.out.println("✗ Service Error: Cannot update null client");
         return false;
      }

      if (client.getId() == null || client.getId() <= 0) {
         System.out.println("✗ Service Error: Client must have a valid ID for update");
         return false;
      }

      boolean success = clientRepository.updateClient(client);
      if (success) {
         System.out.println("✓ Service: Client successfully updated through service layer");
      } else {
         System.out.println("✗ Service: Failed to update client through service layer");
      }
      return success;
   }

   /**
    * Delete a client from the database
    * @param id The ID of the client to delete
    * @return true if client was successfully deleted, false otherwise
    */
   public boolean deleteClient(Integer id) {
      if (id == null) {
         System.out.println("✗ Service Error: Client ID cannot be null");
         return false;
      }

      if (id <= 0) {
         System.out.println("✗ Service Error: Invalid client ID: " + id);
         return false;
      }

      boolean success = clientRepository.deleteClient(id);
      if (success) {
         System.out.println("✓ Service: Client successfully deleted through service layer");
      } else {
         System.out.println("✗ Service: Failed to delete client through service layer");
      }
      return success;
   }

   /**
    * Check if a client exists in the database
    * @param id The client ID to check
    * @return true if client exists, false otherwise
    */
   public boolean clientExists(Integer id) {
      if (id == null || id <= 0) {
         return false;
      }
      return getClientById(id).isPresent();
   }

   /**
    * Get the total number of clients in the database
    * @return The count of all clients
    */
   public int getClientCount() {
      List<Client> clients = getAllClients();
      return clients.size();
   }
}