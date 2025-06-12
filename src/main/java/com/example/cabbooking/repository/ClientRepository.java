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

    public List<Client> findAll() {
        return jdbcTemplate.query("SELECT id, name, email, phone, address, credit_card FROM clients", new ClientRowMapper());
    }

    public Optional<Client> findById(int id) {
        List<Client> clients = jdbcTemplate.query(
            "SELECT id, name, email, phone, address, credit_card FROM clients WHERE id = ?", new ClientRowMapper(), id);

            return clients.isEmpty() ? Optional.empty() : Optional.of(clients.get(0));
    }

    public void newClient(Client client) {
        jdbcTemplate.update("INSERT INTO client (name, email, phone, address, credit_card) VALUES (?, ?, ?, ?, ?)",
                client.getName(), client.getEmail(), client.getPhone(), client.getAddress(), client.getCredit_card());

        System.out.println("New client created");
    }

    public boolean updateClient(Client client) {
        // Guard clause - check for null input
        if (client == null) {
            System.out.println("Error: Cannot update null client");
            return false;
        }

        // Execute the SQL update and capture how many rows were affected
        int rowsAffected = jdbcTemplate.update(
                "UPDATE clients SET name =?, email = ?, phone = ?, address = ?, credit_card = ? WHERE id = ?",
                client.getName(), client.getEmail(), client.getPhone(),
                client.getAddress(), client.getCredit_card(), client.getId());

        // Check result and print appropriate message
        if (rowsAffected > 0) {
            System.out.println("Client " + client.getId() + " (" + client.getName() + ") updated successfully");
            return true;
        } else {
            System.out.println("Error: Client with ID " + client.getId() + " not found in database");
            return false;
        }
    }

    public void deleteClient(int id) {
        jdbcTemplate.update("DELETE FROM clients WHERE id = ?", id);

        System.out.println("Client " + id + " deleted");

    }
}
