package com.example.cabbooking.service;

import com.example.cabbooking.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculateFareService {

    private final RouteService routeService;

    //===============Custom exceptions for fare calculation problems============
    public static class InvalidFareParametersException extends RuntimeException {
        public InvalidFareParametersException(String message) {
            super(message);
        }
    }

    public static class FareCalculationException extends RuntimeException {
        public FareCalculationException(String message) {
            super(message);
        }
    }

    @Autowired
    public CalculateFareService(RouteService routeService) {
        this.routeService = routeService;
    }

    public int dollarsPerMile = 3;
    public int initialBookingFee = 3;

    /** method which calculates the fare from a Route objecty */
    public double calculateFare(Route route) {
        //Validate inputs
        validateFareInputs(route);

        try {
            double distance = routeService.getRouteDistance(route);

            //Validate distance result
            if (distance < 0) {
                throw new FareCalculationException("Distance cannot be negative: " + distance);
            }

            //Validate fare parameters
            validateFareParameters();

            double cabFare = initialBookingFee + (distance * dollarsPerMile);

            //Validate final result
            if (cabFare < 0) {
                throw new FareCalculationException("Calculated fare cannot be negative: " + cabFare);
            }

            if (Double.isNaN(cabFare) || Double.isInfinite(cabFare)) {
                throw new FareCalculationException("Calculated fare is invalid: " + cabFare);
            }

            System.out.println("Cab Fare: " + cabFare);
            return cabFare;

        } catch (RouteService.InvalidRouteException e) {
            throw new FareCalculationException("Cannot calculate fare due to invalid route: " + e.getMessage());
        } catch (Exception e) {
            if (e instanceof InvalidFareParametersException || e instanceof FareCalculationException) {
                throw e; // Re-throw our custom exceptions
            }
            throw new FareCalculationException("Failed to calculate fare: " + e.getMessage());
        }
    }

    /** Helper method to validate route input */
    private void validateFareInputs(Route route) {
        if (route == null) {
            throw new InvalidFareParametersException("Route cannot be null");
        }

        if (route.getFrom() == null || route.getTo() == null) {
            throw new InvalidFareParametersException("Route must have valid starting and destination locations");
        }
    }

    /** Helper method to validate fare calculation parameters */
    private void validateFareParameters() {
        if (dollarsPerMile < 0) {
            throw new InvalidFareParametersException("Dollars per mile cannot be negative: " + dollarsPerMile);
        }

        if (initialBookingFee < 0) {
            throw new InvalidFareParametersException("Initial booking fee cannot be negative: " + initialBookingFee);
        }

        if (dollarsPerMile == 0 && initialBookingFee == 0) {
            throw new InvalidFareParametersException("Both rate and booking fee cannot be zero");
        }
    }

    /** method to safely update dollars per mile fare parameter */
    public void setDollarsPerMile(int dollarsPerMile) {
        if (dollarsPerMile < 0) {
            throw new InvalidFareParametersException("Dollars per mile cannot be negative: " + dollarsPerMile);
        }
        this.dollarsPerMile = dollarsPerMile;
    }
    /** method to safely update booking fee fare parameter */
    public void setInitialBookingFee(int initialBookingFee) {
        if (initialBookingFee < 0) {
            throw new InvalidFareParametersException("Initial booking fee cannot be negative: " + initialBookingFee);
        }
        this.initialBookingFee = initialBookingFee;
    }

    //============Getter methods for testing=============
    public int getDollarsPerMile() {return dollarsPerMile;}

    public int getInitialBookingFee() {return initialBookingFee;}
}