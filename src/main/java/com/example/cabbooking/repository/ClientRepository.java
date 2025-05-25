package com.example.cabbooking.repository;

import com.example.cabbooking.model.Client;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ClientRepository {

    private Map<String, Client> clients = new HashMap<>();

    List<Client> findAll() { return new ArrayList<Client>(clients.values()); }

    Optional<Client> findById(Integer id) { return Optional.of(clients.get(id)); }

    Client save(Client client);


}
