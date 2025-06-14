package com.example.cabbooking.model;

import com.example.cabbooking.model.Client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    public void testDefaultConstructor() {
        Client client = new Client();

        assertEquals(null, client.getId());
        assertNull(client.getName());
        assertNull(client.getEmail());
        assertNull(client.getPhone());
        assertNull(client.getAddress());
        assertNull(client.getCredit_card());
    }

    @Test
    void testFullConstructor() {

        Client client = new Client(4, "mike", "mikemike@test.com",
                "4444444444", "4444 mike street", "0404040404040404");

        assertNotNull(client);
        System.out.println(client);

        //from this test, I found out using the long wrapper for numbers that may start with 0
        //converts them into decimal literals.
        //changing credit_card from int -> long -> string
    }

    @Test
    public void testIdGetterAndSetter() {
        Client client = new Client();

        client.setId(42);

        assertEquals(42, client.getId());
    }

    @Test
    public void testNameGetterAndSetter() {
        Client client = new Client();

        client.setName("Jane Smith");
        assertEquals("Jane Smith", client.getName());
    }

    @Test
    public void testEmailGetterAndSetter() {
        Client client = new Client();

        client.setEmail("jane@test.com");
        assertEquals("jane@test.com", client.getEmail());
    }

    @Test
    public void testPhoneGetterAndSetter() {
        Client client = new Client();

        client.setPhone("555-9876");
        assertEquals("555-9876", client.getPhone());
    }

    @Test
    public void testAddressGetterAndSetter() {
        Client client = new Client();

        client.setAddress("456 Oak Ave");
        assertEquals("456 Oak Ave", client.getAddress());
    }

    @Test
    public void testCreditCardGetterAndSetter() {
        Client client = new Client();

        client.setCredit_card("0404040404040404");
        assertEquals("0404040404040404", client.getCredit_card());
    }

    @Test
    public void testToString() {
        Client client = new Client(1, "John Doe", "john@email.com",
                "555-1234", "123 Main St", "4111-1111-1111-1111");

        String result = client.toString();

        // Check that the string contains all the key information
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("name='John Doe'"));
        assertTrue(result.contains("email='john@email.com'"));
        assertTrue(result.contains("phone='555-1234'"));
        assertTrue(result.contains("address='123 Main St'"));
        assertTrue(result.contains("credit_card=4111-1111-1111-1111"));
    }

    // Test edge case: setting null values doesn't break anything
    @Test
    public void testNullValues() {
        Client client = new Client(1, "Test", "test@email.com",
                "555-0000", "Test Address", "1234-5678");

        // Set fields to null - this should work without errors
        client.setName(null);
        client.setEmail(null);

        assertNull(client.getName());
        assertNull(client.getEmail());
        // Other fields should remain unchanged
        assertEquals(1, client.getId());
        assertEquals("555-0000", client.getPhone());
    }
}