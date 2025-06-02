package com.example.cabbooking.model;

import org.springframework.beans.factory.annotation.Autowired;

public class Client {

    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Integer credit_card;

    //-------------Constructors---------------
    public Client() {}


    public Client(Integer id, String name, String email, String phone, String address, Integer credit_card) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.credit_card = credit_card;
    }

    //------------Getters and Setters-------------
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getCredit_card() { return credit_card; }

    public void setCredit_card(Integer credit_card) { this.credit_card = credit_card; }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", credit_card=" + credit_card + '\'' +
                '}';
    }
}

