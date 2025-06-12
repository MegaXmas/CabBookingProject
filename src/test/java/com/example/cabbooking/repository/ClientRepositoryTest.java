package com.example.cabbooking.repository;

import com.example.cabbooking.model.Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ClientRepository clientRepository;
    private Client testClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        clientRepository = new ClientRepository(jdbcTemplate);

        // Create a test client for reuse
        testClient = new Client(1, "John Doe", "john@email.com",
                "555-1234", "123 Main St", "4111-1111-1111-1111");
    }

    // Test that findAll returns all clients from database
    @Test
    public void testFindAll() {
        // Arrange: Set up what the mock should return
        List<Client> expectedClients = Arrays.asList(
                testClient,
                new Client(2, "Jane Smith", "jane@email.com", "555-5678", "456 Oak Ave", "5555-5555-5555-4444")
        );

        // Mock the jdbcTemplate.query call to return our test data
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(expectedClients);

        // Act: Call the method we're testing
        List<Client> result = clientRepository.findAll();

        // Assert: Check the results
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());

        // Verify that the correct SQL was called
        verify(jdbcTemplate).query(
                eq("SELECT id, name, email, phone, address, credit_card FROM clients"),
                any(RowMapper.class)
        );
    }

    // Test that findById returns the correct client when found
    @Test
    public void testFindByIdWhenClientExists() {
        // Arrange: Mock returning a single client
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1)))
                .thenReturn(Arrays.asList(testClient));

        // Act
        Optional<Client> result = clientRepository.findById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        assertEquals(1, result.get().getId());

        // Verify correct SQL with parameter
        verify(jdbcTemplate).query(
                eq("SELECT id, name, email, phone, address, credit_card FROM clients WHERE id = ?"),
                any(RowMapper.class),
                eq(1)
        );
    }

    // Test that findById returns empty when client doesn't exist
    @Test
    public void testFindByIdWhenClientNotFound() {
        // Arrange: Mock returning empty list (no client found)
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(999)))
                .thenReturn(new ArrayList<>());

        // Act
        Optional<Client> result = clientRepository.findById(999);

        // Assert
        assertFalse(result.isPresent());

        // Verify the query was still called
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq(999));
    }

    // Test that newClient calls the correct SQL insert
    @Test
    public void testNewClient() {
        // Arrange: Mock the update method to return 1 (meaning 1 row affected)
        when(jdbcTemplate.update(anyString(),
                any(Object.class), any(Object.class), any(Object.class),
                any(Object.class), any(Object.class)))
                .thenReturn(1);

        // Act
        clientRepository.newClient(testClient);

        // Assert: Verify the insert SQL was called with correct parameters
        verify(jdbcTemplate).update(
                eq("INSERT INTO client (name, email, phone, address, credit_card) VALUES (?, ?, ?, ?, ?)"),
                eq(testClient.getId()),
                eq(testClient.getName()),
                eq(testClient.getEmail()),
                eq(testClient.getPhone()),
                eq(testClient.getAddress()),
                eq(testClient.getCredit_card())
        );
    }

    // Test that updateClient calls the correct SQL update
    @Test
    public void testUpdateClient() {
        // Arrange
        when(jdbcTemplate.update(anyString(),
                any(Object.class), any(Object.class), any(Object.class),
                any(Object.class), any(Object.class), any(Object.class)))
                .thenReturn(1);

        // Act
        clientRepository.updateClient(testClient);

        // Assert: Verify the update SQL was called
        verify(jdbcTemplate).update(
                eq("UPDATE clients SET name =?, email = ?, phone = ?, address = ?, credit_card = ? WHERE id = ?"),
                eq(testClient.getId()),
                eq(testClient.getName()),
                eq(testClient.getEmail()),
                eq(testClient.getPhone()),
                eq(testClient.getAddress()),
                eq(testClient.getCredit_card())
        );
    }

    // Test that deleteClient calls the correct SQL delete
    @Test
    public void testDeleteClient() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(Object.class)))
                .thenReturn(1);

        // Act
        clientRepository.deleteClient(1);

        // Assert: Verify the delete SQL was called with correct ID
        verify(jdbcTemplate).update(eq("DELETE FROM clients WHERE id = ?"), eq(1));
    }

    // Test edge case: findAll returns empty list when no clients exist
    @Test
    public void testFindAllWhenNoClients() {
        // Arrange: Mock returning empty list
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<Client> result = clientRepository.findAll();

        // Assert
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    // Test that repository handles null client gracefully
    @Test
    public void testNewClientWithNullValues() {
        Client clientWithNulls = new Client(1, null, null, null, null, null);

        when(jdbcTemplate.update(anyString(),
                any(Object.class), any(Object.class), any(Object.class),
                any(Object.class), any(Object.class)))
                .thenReturn(1);

        // Act
        clientRepository.newClient(clientWithNulls);

        // Assert: Should still call update, even with null values
        verify(jdbcTemplate).update(anyString(),
                any(Object.class), any(Object.class), any(Object.class),
                any(Object.class), any(Object.class));
    }
}