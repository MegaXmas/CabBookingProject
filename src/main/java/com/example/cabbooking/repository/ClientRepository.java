package com.example.cabbooking.repository;

import com.example.cabbooking.model.Client;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;


public interface ClientRepository {


    List<Client> findAll();
    Optional<Client> findById(String id);
    Client save(Client client);


}
