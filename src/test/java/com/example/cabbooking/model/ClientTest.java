package com.example.cabbooking.model;

import com.example.cabbooking.model.Client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    void createNewClient() {

        Client client = new Client(4, "mike", "mikemike@test.com",
                "4444444444", "4444 mike street", "0404040404040404L");

        assertNotNull(client);
        System.out.println(client);

        //from this test, I found out using the long wrapper for numbers that may start with 0
        //converts them into decimal literals, changing credit_card from int -> long -> string
    }


}