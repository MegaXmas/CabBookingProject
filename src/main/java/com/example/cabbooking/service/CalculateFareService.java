package com.example.cabbooking.service;

import org.springframework.stereotype.Service;

@Service
public class CalculateFareService {

    public int dollarsPerMile = 3;
    public int initialBookingFee = 3;

    public double CalculateFare(double miles) {


        double cabFare = initialBookingFee + (miles * dollarsPerMile);
        return cabFare;
    }


}
