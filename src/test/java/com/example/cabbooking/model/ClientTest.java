package com.example.cabbooking.model;

import com.example.cabbooking.model.Client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    void createNewClient() {

        Client client = new Client(4, "mike", "mikemike@test.com",
                "4444444444", "4444 mike street", "0404040404040404");

        assertNotNull(client);
        System.out.println(client);

        //from this test, I found out using the long wrapper for numbers that may start with 0
        //converts them into decimal literals.
        //changing credit_card from int -> long -> string
    }

    @Test
    void getClientId() {

        Client client = new Client(4, "mike", "mikemike@test.com",
                "4444444444", "4444 mike street", "0404040404040404");

        assertEquals(4, client.getId());
        System.out.println(client);
    }

    @Test
    void getClientName() {

        Client client = new Client(4, "mike", "mikemike@test.com",
                "4444444444", "4444 mike street", "0404040404040404");

        assertEquals("mike", client.getName());
        System.out.println(client.getName());
    }

    @Test
    void getClientEmail() {

        Client client = new Client(4, "mike", "mikemike@test.com",
                "4444444444", "4444 mike street", "0404040404040404");

        assertEquals("mikemike@test.com", client.getEmail());
        System.out.println(client.getEmail());
    }

    @Test
    void getClientPhone() {

        Client client = new Client(4, "mike", "mikemike@test.com",
                "4444444444", "4444 mike street", "0404040404040404");

        assertEquals("4444444444", client.getPhone());
        System.out.println(client.getPhone());
    }

    @Test
    void getClientAddress() {

        Client client = new Client(4, "mike", "mikemike@test.com",
                "4444444444", "4444 mike street", "0404040404040404");

        assertEquals("4444 mike street", client.getAddress());
        System.out.println(client.getAddress());
    }

    @Test
    void getClientCredit_card() {

        Client client = new Client(4, "mike", "mikemike@test.com",
                "4444444444", "4444 mike street", "0404040404040404");

        assertEquals("0404040404040404", client.getCredit_card());
        System.out.println(client.getCredit_card());
    }

}