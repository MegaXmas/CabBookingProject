package com.example.cabbooking.controller;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.repository.ClientRepository;
import com.example.cabbooking.service.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientControllerTest {

    @Mock
    private ClientService clientService;
    private ClientRepository clientRepository;

    private ClientController clientController;
    private Client testClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        clientController = new ClientController(clientService, clientRepository);

        testClient = new Client(1, "John Doe", "john@email.com",
                "555-1234", "123 Main St", "4111-1111-1111-1111");
    }

    // ================= GET ALL CLIENTS TESTS =================

    @Test
    public void testGetAllClientsSuccess() {
        // Arrange
        List<Client> expectedClients = Arrays.asList(
                testClient,
                new Client(2, "Jane Smith", "jane@email.com", "555-5678", "456 Oak Ave", "5555-5555-5555-4444")
        );
        when(clientService.getAllClients()).thenReturn(expectedClients);

        // Act
        ResponseEntity<List<Client>> response = clientController.getAllClients();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0).getName());
        assertEquals("Jane Smith", response.getBody().get(1).getName());

        verify(clientService).getAllClients();
    }

    @Test
    public void testGetAllClientsWhenEmpty() {
        // Arrange
        when(clientService.getAllClients()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<Client>> response = clientController.getAllClients();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(clientService).getAllClients();
    }

    @Test
    public void testGetAllClientsThrowsException() {
        // Arrange
        when(clientService.getAllClients()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientController.getAllClients());

        assertTrue(exception.getMessage().contains("Failed to retrieve clients"));
    }

    // ================= GET CLIENT BY ID TESTS =================

    @Test
    public void testGetClientByIdSuccess() {
        // Arrange
        when(clientService.getClientById(1)).thenReturn(Optional.of(testClient));

        // Act
        ResponseEntity<Client> response = clientController.getClientById(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals(Integer.valueOf(1), response.getBody().getId());

        verify(clientService).getClientById(1);
    }

    @Test
    public void testGetClientByIdNotFound() {
        // Arrange
        when(clientService.getClientById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ClientController.ClientNotFoundException exception = assertThrows(
                ClientController.ClientNotFoundException.class,
                () -> clientController.getClientById(999)
        );

        assertEquals("Client with ID 999 not found", exception.getMessage());
        verify(clientService).getClientById(999);
    }

    @Test
    public void testGetClientByIdWithInvalidId() {
        // Act & Assert - Test negative ID
        ClientController.InvalidClientDataException exception = assertThrows(
                ClientController.InvalidClientDataException.class,
                () -> clientController.getClientById(-1)
        );

        assertEquals("Client ID must be positive, got: -1", exception.getMessage());
        verify(clientService, never()).getClientById(anyInt());
    }

    @Test
    public void testGetClientByIdWithZeroId() {
        // Act & Assert - Test zero ID
        ClientController.InvalidClientDataException exception = assertThrows(
                ClientController.InvalidClientDataException.class,
                () -> clientController.getClientById(0)
        );

        assertEquals("Client ID must be positive, got: 0", exception.getMessage());
        verify(clientService, never()).getClientById(anyInt());
    }

    // ================= CREATE CLIENT TESTS =================

    @Test
    public void testCreateClientSuccess() {
        // Arrange
        when(clientService.addClient(testClient)).thenReturn(true);

        // Act
        ResponseEntity<Client> response = clientController.createClient(testClient);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("john@email.com", response.getBody().getEmail());

        verify(clientService).addClient(testClient);
    }

    @Test
    public void testCreateClientFails() {
        // Arrange
        when(clientService.addClient(testClient)).thenReturn(false);

        // Act & Assert
        ClientController.ClientCreationException exception = assertThrows(
                ClientController.ClientCreationException.class,
                () -> clientController.createClient(testClient)
        );

        assertEquals("Failed to create client: John Doe", exception.getMessage());
        verify(clientService).addClient(testClient);
    }

    @Test
    public void testCreateClientWithNullClient() {
        // Act & Assert
        ClientController.InvalidClientDataException exception = assertThrows(
                ClientController.InvalidClientDataException.class,
                () -> clientController.createClient(null)
        );

        assertEquals("Client data cannot be null", exception.getMessage());
        verify(clientService, never()).addClient(any(Client.class));
    }

    @Test
    public void testCreateClientWithNullName() {
        // Arrange
        Client clientWithNullName = new Client(1, null, "john@email.com",
                "555-1234", "123 Main St", "4111-1111");

        // Act & Assert
        ClientController.InvalidClientDataException exception = assertThrows(
                ClientController.InvalidClientDataException.class,
                () -> clientController.createClient(clientWithNullName)
        );

        assertEquals("Client name is required", exception.getMessage());
        verify(clientService, never()).addClient(any(Client.class));
    }

    @Test
    public void testCreateClientWithEmptyName() {
        // Arrange
        Client clientWithEmptyName = new Client(1, "   ", "john@email.com",
                "555-1234", "123 Main St", "4111-1111");

        // Act & Assert
        ClientController.InvalidClientDataException exception = assertThrows(
                ClientController.InvalidClientDataException.class,
                () -> clientController.createClient(clientWithEmptyName)
        );

        assertEquals("Client name is required", exception.getMessage());
        verify(clientService, never()).addClient(any(Client.class));
    }

    @Test
    public void testCreateClientWithNullEmail() {
        // Arrange
        Client clientWithNullEmail = new Client(1, "John Doe", null,
                "555-1234", "123 Main St", "4111-1111");

        // Act & Assert
        ClientController.InvalidClientDataException exception = assertThrows(
                ClientController.InvalidClientDataException.class,
                () -> clientController.createClient(clientWithNullEmail)
        );

        assertEquals("Client email is required", exception.getMessage());
        verify(clientService, never()).addClient(any(Client.class));
    }

    @Test
    public void testCreateClientWithInvalidEmail() {
        // Arrange
        Client clientWithInvalidEmail = new Client(1, "John Doe", "invalid-email",
                "555-1234", "123 Main St", "4111-1111");

        // Act & Assert
        ClientController.InvalidClientDataException exception = assertThrows(
                ClientController.InvalidClientDataException.class,
                () -> clientController.createClient(clientWithInvalidEmail)
        );

        assertEquals("Invalid email format: invalid-email", exception.getMessage());
        verify(clientService, never()).addClient(any(Client.class));
    }

    // ================= UPDATE CLIENT TESTS =================

    @Test
    public void testUpdateClientSuccess() {
        // Arrange
        when(clientService.clientExists(1)).thenReturn(true);
        when(clientService.updateClient(testClient)).thenReturn(true);

        // Act
        ResponseEntity<Client> response = clientController.updateClient(1, testClient);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals(Integer.valueOf(1), response.getBody().getId());

        verify(clientService).clientExists(1);
        verify(clientService).updateClient(testClient);
    }

    @Test
    public void testUpdateClientNotFound() {
        // Arrange
        when(clientService.clientExists(999)).thenReturn(false);

        // Act & Assert
        ClientController.ClientNotFoundException exception = assertThrows(
                ClientController.ClientNotFoundException.class,
                () -> clientController.updateClient(999, testClient)
        );

        assertEquals("Client with ID 999 not found", exception.getMessage());
        verify(clientService).clientExists(999);
        verify(clientService, never()).updateClient(any(Client.class));
    }

    @Test
    public void testUpdateClientFails() {
        // Arrange
        when(clientService.clientExists(1)).thenReturn(true);
        when(clientService.updateClient(testClient)).thenReturn(false);

        // Act & Assert
        ClientController.ClientUpdateException exception = assertThrows(
                ClientController.ClientUpdateException.class,
                () -> clientController.updateClient(1, testClient)
        );

        assertEquals("Failed to update client with ID: 1", exception.getMessage());
        verify(clientService).clientExists(1);
        verify(clientService).updateClient(testClient);
    }

    @Test
    public void testUpdateClientWithInvalidId() {
        // Act & Assert
        ClientController.InvalidClientDataException exception = assertThrows(
                ClientController.InvalidClientDataException.class,
                () -> clientController.updateClient(-1, testClient)
        );

        assertEquals("Client ID must be positive, got: -1", exception.getMessage());
        verify(clientService, never()).clientExists(anyInt());
        verify(clientService, never()).updateClient(any(Client.class));
    }

    @Test
    public void testUpdateClientWithInvalidData() {
        // Arrange
        Client invalidClient = new Client(1, "", "john@email.com",
                "555-1234", "123 Main St", "4111-1111");

        // Act & Assert
        ClientController.InvalidClientDataException exception = assertThrows(
                ClientController.InvalidClientDataException.class,
                () -> clientController.updateClient(1, invalidClient)
        );

        assertEquals("Client name is required", exception.getMessage());
        verify(clientService, never()).clientExists(anyInt());
        verify(clientService, never()).updateClient(any(Client.class));
    }

    @Test
    public void testUpdateClientWithMismatchedIds() {
        // Arrange: Create a client with ID 999 in the JSON body
        Client clientWithWrongId = new Client(999, "John Updated", "johnupdated@email.com",
                "555-9999", "456 New Street", "4111-1111-1111-1111");

        Client clientWithId6 = new Client(6, "john 6", "johnupdated6@email.com",
                "555-99996", "4566 New Street", "6111-1111-1111-1111");

        when(clientService.clientExists(6)).thenReturn(true);

        // This should throw an exception because 999 != 6
        ClientController.InvalidClientDataException exception = assertThrows(
                ClientController.InvalidClientDataException.class,
                () -> clientController.updateClient(6, clientWithWrongId)
        );

        // Verify the exception message is what we expect
        assertEquals("Path ID (6) does not match JSON ID (999)", exception.getMessage());

        // Verify that clientService never checked if client exists or tried to update
        // because the validation should fail early
        verify(clientService, never()).updateClient(any(Client.class));
    }


    // ================= DELETE CLIENT TESTS =================

    @Test
    public void testDeleteClientSuccess() {
        // Arrange
        when(clientService.clientExists(1)).thenReturn(true);
        when(clientService.deleteClient(1)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = clientController.deleteClient(1);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(clientService).clientExists(1);
        verify(clientService).deleteClient(1);
    }

    @Test
    public void testDeleteClientNotFound() {
        // Arrange
        when(clientService.clientExists(999)).thenReturn(false);

        // Act & Assert
        ClientController.ClientNotFoundException exception = assertThrows(
                ClientController.ClientNotFoundException.class,
                () -> clientController.deleteClient(999)
        );

        assertEquals("Client with ID 999 not found", exception.getMessage());
        verify(clientService).clientExists(999);
        verify(clientService, never()).deleteClient(anyInt());
    }

    @Test
    public void testDeleteClientFails() {
        // Arrange
        when(clientService.clientExists(1)).thenReturn(true);
        when(clientService.deleteClient(1)).thenReturn(false);

        // Act & Assert
        ClientController.ClientDeletionException exception = assertThrows(
                ClientController.ClientDeletionException.class,
                () -> clientController.deleteClient(1)
        );

        assertEquals("Failed to delete client with ID: 1", exception.getMessage());
        verify(clientService).clientExists(1);
        verify(clientService).deleteClient(1);
    }

    @Test
    public void testDeleteClientWithInvalidId() {
        // Act & Assert
        ClientController.InvalidClientDataException exception = assertThrows(
                ClientController.InvalidClientDataException.class,
                () -> clientController.deleteClient(0)
        );

        assertEquals("Client ID must be positive, got: 0", exception.getMessage());
        verify(clientService, never()).clientExists(anyInt());
        verify(clientService, never()).deleteClient(anyInt());
    }

    // ================= INTEGRATION-STYLE TESTS =================

    @Test
    public void testCompleteClientWorkflow() {
        // Test a realistic workflow: create, read, update, delete

        // 1. Create a client
        when(clientService.addClient(testClient)).thenReturn(true);
        ResponseEntity<Client> createResponse = clientController.createClient(testClient);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        // 2. Get the client back
        when(clientService.getClientById(1)).thenReturn(Optional.of(testClient));
        ResponseEntity<Client> getResponse = clientController.getClientById(1);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        // 3. Update the client
        testClient.setName("John Updated");
        when(clientService.clientExists(1)).thenReturn(true);
        when(clientService.updateClient(testClient)).thenReturn(true);
        ResponseEntity<Client> updateResponse = clientController.updateClient(1, testClient);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        // 4. Delete the client
        when(clientService.clientExists(1)).thenReturn(true);
        when(clientService.deleteClient(1)).thenReturn(true);
        ResponseEntity<Void> deleteResponse = clientController.deleteClient(1);
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // Verify all calls were made
        verify(clientService).addClient(testClient);
        verify(clientService).getClientById(1);
        verify(clientService, times(2)).clientExists(1); // Called in update and delete
        verify(clientService).updateClient(testClient);
        verify(clientService).deleteClient(1);
    }
}