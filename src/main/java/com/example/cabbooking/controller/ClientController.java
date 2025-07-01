package com.example.cabbooking.controller;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.repository.ClientRepository;
import com.example.cabbooking.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService, ClientRepository clientRepository) {
        this.clientService = clientService;
    }

    // Custom Exception Classes
    public static class ClientNotFoundException extends RuntimeException {
        public ClientNotFoundException(String message) {
            super(message);
        }
    }

    public static class ClientCreationException extends RuntimeException {
        public ClientCreationException(String message) {
            super(message);
        }
    }

    public static class ClientUpdateException extends RuntimeException {
        public ClientUpdateException(String message) {
            super(message);
        }
    }

    public static class ClientDeletionException extends RuntimeException {
        public ClientDeletionException(String message) {
            super(message);
        }
    }

    public static class InvalidClientDataException extends RuntimeException {
        public InvalidClientDataException(String message) {
            super(message);
        }
    }

    // Exception Handler Methods
    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<String> handleClientNotFound(ClientNotFoundException e) {
        return new ResponseEntity<>("❌ " + e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ClientCreationException.class)
    public ResponseEntity<String> handleClientCreation(ClientCreationException e) {
        return new ResponseEntity<>("❌ " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClientUpdateException.class)
    public ResponseEntity<String> handleClientUpdate(ClientUpdateException e) {
        return new ResponseEntity<>("❌ " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClientDeletionException.class)
    public ResponseEntity<String> handleClientDeletion(ClientDeletionException e) {
        return new ResponseEntity<>("❌ " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidClientDataException.class)
    public ResponseEntity<String> handleInvalidData(InvalidClientDataException e) {
        return new ResponseEntity<>("❌ " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // API Endpoints with Exception Handling

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        try {
            List<Client> clients = clientService.getAllClients();
            return new ResponseEntity<>(clients, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to retrieve clients: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable int id) {
        // Validate ID
        if (id <= 0) {
            throw new InvalidClientDataException("Client ID must be positive, got: " + id);
        }

        Optional<Client> client = clientService.getClientById(id);

        if (client.isPresent()) {
            return new ResponseEntity<>(client.get(), HttpStatus.OK);
        } else {
            throw new ClientNotFoundException("Client with ID " + id + " not found");
        }
    }

    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        // Validate client data
        validateClientData(client);

        boolean success = clientService.addClient(client);

        if (success) {
            return new ResponseEntity<>(client, HttpStatus.CREATED);
        } else {
            throw new ClientCreationException("Failed to create client: " + client.getName());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable int id, @RequestBody Client client) {
        // Validate path ID
        if (id <= 0) {
            throw new InvalidClientDataException("Client ID must be positive, got: " + id);
        }

        // Validate client data
        validateClientData(client);

        if (client.getId() != null && !client.getId().equals(id) && clientService.clientExists(id)) {
            throw new InvalidClientDataException(
                    "Path ID (" + id + ") does not match JSON ID (" + client.getId() + ")"
            );
        }

        // Check if client exists
        if (!clientService.clientExists(id)) {
            throw new ClientNotFoundException("Client with ID " + id + " not found");
        }

        // Set the ID from the path parameter
        client.setId(id);

        boolean success = clientService.updateClient(client);

        if (success) {
            return new ResponseEntity<>(client, HttpStatus.OK);
        } else {
            throw new ClientUpdateException("Failed to update client with ID: " + id);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable int id) {
        // Validate ID
        if (id <= 0) {
            throw new InvalidClientDataException("Client ID must be positive, got: " + id);
        }

        // Check if client exists
        if (!clientService.clientExists(id)) {
            throw new ClientNotFoundException("Client with ID " + id + " not found");
        }

        boolean success = clientService.deleteClient(id);

        if (success) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            throw new ClientDeletionException("Failed to delete client with ID: " + id);
        }
    }

    // Helper method to validate client data
    private void validateClientData(Client client) {
        if (client == null) {
            throw new InvalidClientDataException("Client data cannot be null");
        }

        if (client.getName() == null || client.getName().trim().isEmpty()) {
            throw new InvalidClientDataException("Client name is required");
        }

        if (client.getEmail() == null || client.getEmail().trim().isEmpty()) {
            throw new InvalidClientDataException("Client email is required");
        }

        // Basic email validation
        if (!client.getEmail().contains("@")) {
            throw new InvalidClientDataException("Invalid email format: " + client.getEmail());
        }
    }
}