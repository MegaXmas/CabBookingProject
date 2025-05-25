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
            return client;
        }
    }

    public List<Client> findAll() {
        return jdbcTemplate.query("SELECT id, name, email, phone, address FROM clients", new ClientRowMapper());
    }

    public Optional<Client> findById(Integer id) {
        List<Client> clients = jdbcTemplate.query(
            "SELECT id, name, email, phone, address FROM clients WHERE id = ?", new ClientRowMapper(), id);

            return clients.isEmpty() ? Optional.empty() : Optional.of(clients.get(0));
    }

    public void newClient(Client client) {
        jdbcTemplate.update("INSERT INTO client (name, email, phone, address) VALUES (?, ?, ?, ?)",
                client.getId(), client.getName(), client.getEmail(), client.getPhone(), client.getAddress());

        System.out.println("New client created");
    }

    public void updateClient(Client client) {
        jdbcTemplate.update("UPDATE clients SET name =?, email = ?, phone = ?, address = ? WHERE id = ?",
                client.getId(), client.getName(), client.getEmail(), client.getPhone(), client.getAddress());

        System.out.println("Client " + client.getId() + ", " + client.getName() + "updated");
    }

    public void deleteClient(Client client, Integer id) {
        jdbcTemplate.update("DELETE FROM client WHERE id = ?", client.getId());

        System.out.println("Client " + client.getId() + ", " + client.getName() + "deleted");

    }
}
