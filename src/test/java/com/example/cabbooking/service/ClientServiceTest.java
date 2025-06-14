package com.example.cabbooking.service;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    private ClientService clientService;
    private Client testClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        clientService = new ClientService(clientRepository);

        testClient = new Client(1, "John Doe", "john@email.com",
                "555-1234", "123 Main St", "4111-1111-1111-1111");
    }

    // === GET ALL CLIENTS TESTS ===
    @Test
    public void testGetAllClientsSuccess() {
        // Arrange: Mock repository to return test data
        List<Client> expectedClients = Arrays.asList(
                testClient,
                new Client(2, "Jane Smith", "jane@email.com", "555-5678", "456 Oak Ave", "5555-5555-5555-4444")
        );
        when(clientRepository.findAll()).thenReturn(expectedClients);

        // Act: Call the service method
        List<Client> result = clientService.getAllClients();

        // Assert: Check the service returns what the repository returned
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());

        // Verify: Ensure the service called the repository
        verify(clientRepository).findAll();
    }

    @Test
    public void testGetAllClientsWhenEmpty() {
        // Arrange: Mock repository to return empty list
        when(clientRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Client> result = clientService.getAllClients();

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());

        // Verify
        verify(clientRepository).findAll();
    }

    // === GET CLIENT BY ID TESTS ===
    @Test
    public void testGetClientByIdWhenFound() {
        // Arrange: Mock repository to return the client
        when(clientRepository.findById(1)).thenReturn(Optional.of(testClient));

        // Act
        Optional<Client> result = clientService.getClientById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        assertEquals(1, result.get().getId());

        // Verify
        verify(clientRepository).findById(1);
    }

    @Test
    public void testGetClientByIdWhenNotFound() {
        // Arrange: Mock repository to return empty
        when(clientRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<Client> result = clientService.getClientById(999);

        // Assert
        assertFalse(result.isPresent());

        // Verify
        verify(clientRepository).findById(999);
    }

    @Test
    public void testGetClientByIdWithNullId() {
        // Arrange: Mock repository behavior for null (if it handles it)
        when(clientRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Optional<Client> result = clientService.getClientById(null);

        // Assert
        assertFalse(result.isPresent());

        // Verify
        verify(clientRepository).findById(null);
    }

    // === ADD CLIENT TESTS ===
    @Test
    public void testAddClientSuccess() {
        // Arrange: Mock repository to return success
        when(clientRepository.newClient(testClient)).thenReturn(true);

        // Act
        Client result = clientService.addClient(testClient);

        // Assert: Service should return the same client
        assertEquals(testClient, result);
        assertEquals("John Doe", result.getName());

        // Verify: Repository method was called
        verify(clientRepository).newClient(testClient);
    }

    @Test
    public void testAddClientWhenRepositoryFails() {
        // Arrange: Mock repository to return failure
        when(clientRepository.newClient(testClient)).thenReturn(false);

        // Act: Even if repository fails, service still returns the client
        Client result = clientService.addClient(testClient);

        // Assert: Service behavior doesn't change based on repository result
        assertEquals(testClient, result);

        // Verify
        verify(clientRepository).newClient(testClient);
    }

    @Test
    public void testAddClientWithNull() {
        // Arrange: Mock repository behavior for null
        when(clientRepository.newClient(null)).thenReturn(false);

        // Act
        Client result = clientService.addClient(null);

        // Assert: Service returns null (the input)
        assertNull(result);

        // Verify
        verify(clientRepository).newClient(null);
    }

    // === UPDATE CLIENT TESTS ===
    @Test
    public void testUpdateClientSuccess() {
        // Arrange: Mock repository to return success
        when(clientRepository.updateClient(testClient)).thenReturn(true);

        // Act
        Client result = clientService.updateClient(testClient);

        // Assert: Service should return the same client
        assertEquals(testClient, result);
        assertEquals("John Doe", result.getName());

        // Verify
        verify(clientRepository).updateClient(testClient);
    }

    @Test
    public void testUpdateClientWhenRepositoryFails() {
        // Arrange: Mock repository to return failure
        when(clientRepository.updateClient(testClient)).thenReturn(false);

        // Act: Service still returns the client even if update failed
        Client result = clientService.updateClient(testClient);

        // Assert
        assertEquals(testClient, result);

        // Verify
        verify(clientRepository).updateClient(testClient);
    }

    @Test
    public void testUpdateClientWithNull() {
        // Arrange
        when(clientRepository.updateClient(null)).thenReturn(false);

        // Act
        Client result = clientService.updateClient(null);

        // Assert
        assertNull(result);

        // Verify
        verify(clientRepository).updateClient(null);
    }

    // === DELETE CLIENT TESTS ===
    @Test
    public void testDeleteClientSuccess() {
        // Arrange: Mock repository to return success
        when(clientRepository.deleteClient(1)).thenReturn(true);

        // Act: Method returns void, so just call it
        clientService.deleteClient(1);

        // Verify: Ensure repository method was called
        verify(clientRepository).deleteClient(1);
    }

    @Test
    public void testDeleteClientWhenNotFound() {
        // Arrange: Mock repository to return failure
        when(clientRepository.deleteClient(999)).thenReturn(false);

        // Act: Service doesn't care about the return value
        clientService.deleteClient(999);

        // Verify: Repository was still called
        verify(clientRepository).deleteClient(999);
    }

    @Test
    public void testDeleteClientWithNullId() {
        // Arrange
        when(clientRepository.deleteClient(null)).thenReturn(false);

        // Act
        clientService.deleteClient(null);

        // Verify
        verify(clientRepository).deleteClient(null);
    }

    // === INTEGRATION-STYLE TESTS ===
    @Test
    public void testCompleteClientWorkflow() {
        // Test a realistic workflow: create, read, update, delete

        // 1. Add a client
        when(clientRepository.newClient(testClient)).thenReturn(true);
        Client addedClient = clientService.addClient(testClient);
        assertEquals(testClient, addedClient);

        // 2. Get the client back
        when(clientRepository.findById(1)).thenReturn(Optional.of(testClient));
        Optional<Client> foundClient = clientService.getClientById(1);
        assertTrue(foundClient.isPresent());

        // 3. Update the client
        testClient.setName("John Updated");
        when(clientRepository.updateClient(testClient)).thenReturn(true);
        Client updatedClient = clientService.updateClient(testClient);
        assertEquals("John Updated", updatedClient.getName());

        // 4. Delete the client
        when(clientRepository.deleteClient(1)).thenReturn(true);
        clientService.deleteClient(1);

        // Verify all repository calls were made
        verify(clientRepository).newClient(testClient);
        verify(clientRepository).findById(1);
        verify(clientRepository).updateClient(testClient);
        verify(clientRepository).deleteClient(1);
    }
}