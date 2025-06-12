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

        testClient = new Client(1, "John Doe", "john@email.com",
                "555-1234", "123 Main St", "4111-1111-1111-1111");
    }

    // === FIND ALL TESTS ===
    @Test
    public void testFindAllSuccess() {
        List<Client> expectedClients = Arrays.asList(testClient);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(expectedClients);

        List<Client> result = clientRepository.findAll();

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    public void testFindAllWhenDatabaseError() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        List<Client> result = clientRepository.findAll();

        assertTrue(result.isEmpty());
    }

    // === FIND BY ID TESTS ===
    @Test
    public void testFindByIdSuccess() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1)))
                .thenReturn(Arrays.asList(testClient));

        Optional<Client> result = clientRepository.findById(1);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    public void testFindByIdNotFound() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(999)))
                .thenReturn(new ArrayList<>());

        Optional<Client> result = clientRepository.findById(999);

        assertFalse(result.isPresent());
    }

    @Test
    public void testFindByIdWithInvalidId() {
        Optional<Client> result = clientRepository.findById(-1);

        assertFalse(result.isPresent());
        verify(jdbcTemplate, never()).query(anyString(), any(RowMapper.class), anyInt());
    }

    @Test
    public void testFindByIdWithDatabaseError() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1)))
                .thenThrow(new RuntimeException("Database error"));

        Optional<Client> result = clientRepository.findById(1);

        assertFalse(result.isPresent());
    }

    // === CREATE CLIENT TESTS ===
    @Test
    public void testNewClientSuccess() {
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        boolean result = clientRepository.newClient(testClient);

        assertTrue(result);
        verify(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any());
    }

    @Test
    public void testNewClientWithNullClient() {
        boolean result = clientRepository.newClient(null);

        assertFalse(result);
        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any(), any(), any());
    }

    @Test
    public void testNewClientWithNullName() {
        Client clientWithNullName = new Client(1, null, "john@email.com",
                "555-1234", "123 Main St", "4111-1111");

        boolean result = clientRepository.newClient(clientWithNullName);

        assertFalse(result);
        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any(), any(), any());
    }

    @Test
    public void testNewClientWithEmptyName() {
        Client clientWithEmptyName = new Client(1, "   ", "john@email.com",
                "555-1234", "123 Main St", "4111-1111");

        boolean result = clientRepository.newClient(clientWithEmptyName);

        assertFalse(result);
        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any(), any(), any());
    }

    @Test
    public void testNewClientWithNullEmail() {
        Client clientWithNullEmail = new Client(1, "John Doe", null,
                "555-1234", "123 Main St", "4111-1111");

        boolean result = clientRepository.newClient(clientWithNullEmail);

        assertFalse(result);
        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any(), any(), any());
    }

    @Test
    public void testNewClientWithDatabaseError() {
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        boolean result = clientRepository.newClient(testClient);

        assertFalse(result);
    }

    @Test
    public void testNewClientWithOptionalNullValues() {
        Client clientWithNulls = new Client(1, "John Doe", "john@email.com",
                null, null, null);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        boolean result = clientRepository.newClient(clientWithNulls);

        assertTrue(result);
    }

    // === UPDATE CLIENT TESTS ===
    @Test
    public void testUpdateClientSuccess() {
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        boolean result = clientRepository.updateClient(testClient);

        assertTrue(result);
    }

    @Test
    public void testUpdateClientWithNullClient() {
        boolean result = clientRepository.updateClient(null);

        assertFalse(result);
        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testUpdateClientWithInvalidId() {
        Client invalidClient = new Client(-1, "John", "john@email.com",
                "555-1234", "123 Main St", "4111-1111");

        boolean result = clientRepository.updateClient(invalidClient);

        assertFalse(result);
        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testUpdateClientNotFound() {
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0);

        boolean result = clientRepository.updateClient(testClient);

        assertFalse(result);
    }

    @Test
    public void testUpdateClientWithNullName() {
        Client clientWithNullName = new Client(1, null, "john@email.com",
                "555-1234", "123 Main St", "4111-1111");

        boolean result = clientRepository.updateClient(clientWithNullName);

        assertFalse(result);
        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any(), any(), any(), any());
    }

    // === DELETE CLIENT TESTS ===
    @Test
    public void testDeleteClientSuccess() {
        when(jdbcTemplate.update(anyString(), any()))
                .thenReturn(1);

        boolean result = clientRepository.deleteClient(1);

        assertTrue(result);
        verify(jdbcTemplate).update(eq("DELETE FROM clients WHERE id = ?"), eq(1));
    }

    @Test
    public void testDeleteClientWithInvalidId() {
        boolean result = clientRepository.deleteClient(-1);

        assertFalse(result);
        verify(jdbcTemplate, never()).update(anyString(), any());
    }

    @Test
    public void testDeleteClientNotFound() {
        when(jdbcTemplate.update(anyString(), any()))
                .thenReturn(0);

        boolean result = clientRepository.deleteClient(999);

        assertFalse(result);
    }

    @Test
    public void testDeleteClientWithDatabaseError() {
        when(jdbcTemplate.update(anyString(), any()))
                .thenThrow(new RuntimeException("Database error"));

        boolean result = clientRepository.deleteClient(1);

        assertFalse(result);
    }
}