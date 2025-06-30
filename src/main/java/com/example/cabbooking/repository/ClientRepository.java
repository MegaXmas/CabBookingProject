package com.example.cabbooking.repository;

import com.example.cabbooking.model.Client;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class ClientRepository {

    private final JdbcTemplate jdbcTemplate;

    public ClientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static class ClientRowMapper implements RowMapper<Client> {
        @Override
        public Client mapRow(ResultSet rs, int rowNum) throws SQLException {
            Client client = new Client();
            client.setId(rs.getInt("id"));
            client.setName(rs.getString("name"));
            client.setEmail(rs.getString("email"));
            client.setPhone(rs.getString("phone"));
            client.setAddress(rs.getString("address"));
            client.setCredit_card(rs.getString("credit_card"));
            return client;
        }
    }

    /** method which runs a SQL query to find all clients from the database
     * @return ArrayList of clients from the SQL database */
    public List<Client> findAll() {
        try {
            List<Client> clients = jdbcTemplate.query(
                    "SELECT id, name, email, phone, address, credit_card FROM clients",
                    new ClientRowMapper());
            System.out.println("✓ Repository: Successfully retrieved " + clients.size() + " clients");
            return clients;
        } catch (Exception e) {
            System.out.println("✗ Repository: Error retrieving clients: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** method to run a SQL query which find a client by their id number in the database
     * @param id Client id to find by
     * @return Optional of a client object depending on success of the method */
    public Optional<Client> findById(int id) {
        if (id <= 0) {
            System.out.println("✗ Repository: Error: Invalid client ID: " + id);
            return Optional.empty();
        }

        try {
            List<Client> clients = jdbcTemplate.query(
                    "SELECT id, name, email, phone, address, credit_card FROM clients WHERE id = ?",
                    new ClientRowMapper(), id);

            if (clients.isEmpty()) {
                System.out.println("✗ Repository: Client with ID " + id + " not found");
                return Optional.empty();
            } else {
                System.out.println("✓ Repository: Found client: " + clients.get(0).getName());
                return Optional.of(clients.get(0));
            }
        } catch (Exception e) {
            System.out.println("✗ Repository: Error finding client with ID: " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /** method that runs a SQL query to create a new client in the database *
     * @param client Client object to be added to the database
     */
    public boolean newClient(Client client) {
        if (client == null) {
            System.out.println("✗ Repository: Error: Cannot create null client");
            return false;
        }

        if (client.getName() == null || client.getName().trim().isEmpty()) {
            System.out.println("✗ Repository: Error: Client name is required");
            return false;
        }

        if (client.getEmail() == null || client.getEmail().trim().isEmpty()) {
            System.out.println("✗ Repository: Error: Client email is required");
            return false;
        }

        try {
            int rowsAffected = jdbcTemplate.update(
                    "INSERT INTO clients (name, email, phone, address, credit_card) VALUES (?, ?, ?, ?, ?)",
                    client.getName(), client.getEmail(), client.getPhone(),
                    client.getAddress(), client.getCredit_card());

            if (rowsAffected > 0) {
                System.out.println("✓ Repository: New client created: " + client.getName() + " (" + client.getEmail() + ")");
                return true;
            } else {
                System.out.println("✗ Repository: Failed to create client");
                return false;
            }
        } catch (Exception e) {
            System.out.println("✗ Repository: Error creating client: " + e.getMessage());
            return false;
        }
    }

    /** method that runs a SQL query to update client information in the database
     *  @param client Client object to be updated with new data */
    public boolean updateClient(Client client) {
        if (client == null) {
            System.out.println("✗ Repository: Error: Cannot update null client");
            return false;
        }

        if (client.getId() <= 0) {
            System.out.println("✗ Repository: Error: Invalid client ID " + client.getId());
            return false;
        }

        if (client.getName() == null || client.getName().trim().isEmpty()) {
            System.out.println("✗ Repository: Error: Client name is required");
            return false;
        }

        try {
            int rowsAffected = jdbcTemplate.update(
                    "UPDATE clients SET name = ?, email = ?, phone = ?, address = ?, credit_card = ? WHERE id = ?",
                    client.getName(), client.getEmail(), client.getPhone(),
                    client.getAddress(), client.getCredit_card(), client.getId());

            if (rowsAffected > 0) {
                System.out.println("✓ Repository: Client " + client.getId() + " (" + client.getName() + ") updated successfully");
                return true;
            } else {
                System.out.println("✗ Repository: Client with ID " + client.getId() + " not found");
                return false;
            }
        } catch (Exception e) {
            System.out.println("✗ Repository: Error updating client: " + e.getMessage());
            return false;
        }
    }

    /** method that runs a SQL query to delete a client from the database
     * @param id ID of the client which is to be deleted */
    public boolean deleteClient(int id) {
        if (id <= 0) {
            System.out.println("✗ Repository: Error: Invalid client ID: " + id);
            return false;
        }

        try {
            int rowsAffected = jdbcTemplate.update("DELETE FROM clients WHERE id = ?", id);

            if (rowsAffected > 0) {
                System.out.println("✓ Repository: Client " + id + " deleted successfully");
                return true;
            } else {
                System.out.println("✗ Repository: Client with ID " + id + " not found");
                return false;
            }
        } catch (Exception e) {
            System.out.println("✗ Repository: Error deleting client: " + e.getMessage());
            return false;
        }
    }
}