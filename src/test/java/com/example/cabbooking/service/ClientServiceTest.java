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
        assertEquals(Integer.valueOf(1), result.get().getId());

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
        // Act: Service handles null without calling repository
        Optional<Client> result = clientService.getClientById(null);

        // Assert
        assertFalse(result.isPresent());

        // Verify: Repository should NOT be called with null
        verify(clientRepository, never()).findById(anyInt());
    }

    // === ADD CLIENT TESTS ===
    @Test
    public void testAddClientSuccess() {
        // Arrange: Mock repository to return success
        when(clientRepository.newClient(testClient)).thenReturn(true);

        // Act: Service now returns boolean
        boolean result = clientService.addClient(testClient);

        // Assert: Service should return true for success
        assertTrue(result);

        // Verify: Repository method was called
        verify(clientRepository).newClient(testClient);
    }

    @Test
    public void testAddClientWhenRepositoryFails() {
        // Arrange: Mock repository to return failure
        when(clientRepository.newClient(testClient)).thenReturn(false);

        // Act: Service returns false when repository fails
        boolean result = clientService.addClient(testClient);

        // Assert: Service should return false
        assertFalse(result);

        // Verify
        verify(clientRepository).newClient(testClient);
    }

    @Test
    public void testAddClientWithNull() {
        // Act: Service handles null without calling repository
        boolean result = clientService.addClient(null);

        // Assert: Service returns false for null input
        assertFalse(result);

        // Verify: Repository should NOT be called
        verify(clientRepository, never()).newClient(any(Client.class));
    }

    // === UPDATE CLIENT TESTS ===
    @Test
    public void testUpdateClientSuccess() {
        // Arrange: Mock repository to return success
        when(clientRepository.updateClient(testClient)).thenReturn(true);

        // Act: Service now returns boolean
        boolean result = clientService.updateClient(testClient);

        // Assert: Service should return true for success
        assertTrue(result);

        // Verify
        verify(clientRepository).updateClient(testClient);
    }

    @Test
    public void testUpdateClientWhenRepositoryFails() {
        // Arrange: Mock repository to return failure
        when(clientRepository.updateClient(testClient)).thenReturn(false);

        // Act: Service returns false when repository fails
        boolean result = clientService.updateClient(testClient);

        // Assert
        assertFalse(result);

        // Verify
        verify(clientRepository).updateClient(testClient);
    }

    @Test
    public void testUpdateClientWithNull() {
        // Act: Service handles null without calling repository
        boolean result = clientService.updateClient(null);

        // Assert
        assertFalse(result);

        // Verify: Repository should NOT be called
        verify(clientRepository, never()).updateClient(any(Client.class));
    }

    @Test
    public void testUpdateClientWithInvalidId() {
        // Arrange: Create client with invalid ID (0 or negative)
        Client invalidClient = new Client(0, "John", "john@email.com", "555-1234", "123 Main St", "4111-1111");

        // Act: Service should reject invalid ID
        boolean result = clientService.updateClient(invalidClient);

        // Assert
        assertFalse(result);

        // Verify: Repository should NOT be called
        verify(clientRepository, never()).updateClient(any(Client.class));
    }

    // === DELETE CLIENT TESTS ===
    @Test
    public void testDeleteClientSuccess() {
        // Arrange: Mock repository to return success
        when(clientRepository.deleteClient(1)).thenReturn(true);

        // Act: Service now returns boolean
        boolean result = clientService.deleteClient(1);

        // Assert: Service should return true for success
        assertTrue(result);

        // Verify: Ensure repository method was called
        verify(clientRepository).deleteClient(1);
    }

    @Test
    public void testDeleteClientWhenNotFound() {
        // Arrange: Mock repository to return failure
        when(clientRepository.deleteClient(999)).thenReturn(false);

        // Act: Service returns false when repository fails
        boolean result = clientService.deleteClient(999);

        // Assert
        assertFalse(result);

        // Verify: Repository was still called
        verify(clientRepository).deleteClient(999);
    }

    @Test
    public void testDeleteClientWithNullId() {
        // Act: Service handles null without calling repository
        boolean result = clientService.deleteClient(null);

        // Assert
        assertFalse(result);

        // Verify: Repository should NOT be called
        verify(clientRepository, never()).deleteClient(anyInt());
    }

    @Test
    public void testDeleteClientWithInvalidId() {
        // Act: Service should reject invalid ID
        boolean result = clientService.deleteClient(-1);

        // Assert
        assertFalse(result);

        // Verify: Repository should NOT be called
        verify(clientRepository, never()).deleteClient(anyInt());
    }

    // === UTILITY METHOD TESTS ===
    @Test
    public void testClientExistsWhenFound() {
        // Arrange: Mock repository to return client
        when(clientRepository.findById(1)).thenReturn(Optional.of(testClient));

        // Act
        boolean result = clientService.clientExists(1);

        // Assert
        assertTrue(result);

        // Verify
        verify(clientRepository).findById(1);
    }

    @Test
    public void testClientExistsWhenNotFound() {
        // Arrange: Mock repository to return empty
        when(clientRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        boolean result = clientService.clientExists(999);

        // Assert
        assertFalse(result);

        // Verify
        verify(clientRepository).findById(999);
    }

    @Test
    public void testClientExistsWithInvalidId() {
        // Act: Service should handle invalid ID
        boolean result = clientService.clientExists(-1);

        // Assert
        assertFalse(result);

        // Verify: Repository should NOT be called
        verify(clientRepository, never()).findById(anyInt());
    }

    @Test
    public void testGetClientCount() {
        // Arrange: Mock repository to return list of clients
        List<Client> clients = Arrays.asList(testClient,
                new Client(2, "Jane", "jane@email.com", "555-5678", "456 Oak Ave", "5555-5555"));
        when(clientRepository.findAll()).thenReturn(clients);

        // Act
        int count = clientService.getClientCount();

        // Assert
        assertEquals(2, count);

        // Verify
        verify(clientRepository).findAll();
    }

    // === INTEGRATION-STYLE TESTS ===
    @Test
    public void testCompleteClientWorkflow() {
        // Test a realistic workflow: create, read, update, delete

        // 1. Add a client
        when(clientRepository.newClient(testClient)).thenReturn(true);
        boolean addResult = clientService.addClient(testClient);
        assertTrue(addResult);

        // 2. Get the client back
        when(clientRepository.findById(1)).thenReturn(Optional.of(testClient));
        Optional<Client> foundClient = clientService.getClientById(1);
        assertTrue(foundClient.isPresent());

        // 3. Update the client
        testClient.setName("John Updated");
        when(clientRepository.updateClient(testClient)).thenReturn(true);
        boolean updateResult = clientService.updateClient(testClient);
        assertTrue(updateResult);

        // 4. Delete the client
        when(clientRepository.deleteClient(1)).thenReturn(true);
        boolean deleteResult = clientService.deleteClient(1);
        assertTrue(deleteResult);

        // Verify all repository calls were made
        verify(clientRepository).newClient(testClient);
        verify(clientRepository).findById(1);
        verify(clientRepository).updateClient(testClient);
        verify(clientRepository).deleteClient(1);
    }
}