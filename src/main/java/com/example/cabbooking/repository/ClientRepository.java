package com.example.cabbooking.repository;

import com.example.cabbooking.model.Client;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public class ClientRepository {

    private HashMap<String, Client> clientHashMap = new HashMap<>();

}
