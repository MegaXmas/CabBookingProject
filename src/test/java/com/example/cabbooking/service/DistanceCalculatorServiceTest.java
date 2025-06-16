package com.example.cabbooking.service;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static com.example.cabbooking.service.DistanceCalculatorService.calculateDistance;
import static com.example.cabbooking.service.DistanceCalculatorService.kmToMiles;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DistanceCalculatorServiceTest {

    @Test
    void calculateDistanceTest() {
        double distanceTest = calculateDistance(40.7128, -74.0060, 34.0522, -118.2437);
        // NYC and LA

        // Check if distance is between 3,900-4,000 km
        assertThat(distanceTest).isBetween(3900.0, 4000.0);
        System.out.println(distanceTest);
    }

    @Test
    void calculateDistanceTestSameLocationShouldReturnZero() {
        double sameDistance = calculateDistance(40.7128, -74.0060, 40.7128, -74.0060);

        assertEquals(0, sameDistance, 0.01);
        System.out.println(sameDistance);
    }

    @Test
    void kmToMilesTestProperConversion() {
        double miles = kmToMiles(1.0);

        assertEquals(0.621371, miles);
        System.out.println(miles);
    }

    @Test
    void kmToMilesTestRealDistance() {
        double miles = kmToMiles(3950.0);

        assertThat(miles).isBetween(2400.0, 2500.0);
        System.out.println(miles);
    }

    @Test
    void kmToMilesTestExactDistance() {
        double miles = kmToMiles(3950.0);
        assertThat(miles).isEqualTo(3950.0 * 0.621371);
        assertThat(miles).isEqualTo(2454.416, Offset.offset(.001));
        System.out.println(miles);
    }

    @Test
    void kmToMilesTestZeroDistance() {
        double miles = kmToMiles(0.0);

        assertEquals(0.0, miles);
        System.out.println(miles);
    }

    @Test
    void kmToMilesTestVerySmallDistance() {
        double miles = kmToMiles(.0001);

        assertThat(miles).isCloseTo(0.0000621371, Offset.offset(0.000005));
        System.out.println((miles));
    }

    @Test
    void kmToMilesTestVeryLargeDistance() {
        double miles = kmToMiles(50000);

        assertThat(miles).isCloseTo(31068.56, Offset.offset(0.01));
        System.out.println(miles);
    }

    @Test
    void calculateDistanceThenKmToMilesTest() {
        double distanceTest = calculateDistance(40.7128, -74.0060, 34.0522, -118.2437);
        assertThat(distanceTest).isBetween(3900.0, 4000.0);
        System.out.println(distanceTest);

        double miles = kmToMiles(distanceTest);
        assertThat(miles).isBetween(2400.0, 2500.0);
        assertThat(miles).isEqualTo(3935.746254609722 * 0.621371,  Offset.offset(.01));
        System.out.println(miles);
    }

    @Test
    void kmToMilesEqualsMilesToKmTest() {
        double kilometers = calculateDistance(40.7128, -74.0060, 34.0522, -118.2437);
        System.out.println(kilometers); //3935.746254609722
        double miles = kmToMiles(kilometers);
        System.out.println(miles); //2445.5585859730977

        assertThat(miles * 1.60934).isEqualTo(3935.74625409722, Offset.offset(0.1));
        System.out.println(kilometers + " : kilometers should = about 3935.746");
        System.out.println(miles + " : miles should = about 2445.559");
        System.out.println(miles * 1.60934 + " : miles converted to kilometers should = about 3935.746");
        System.out.println(kilometers * 0.621371 + " : kilometers converted to miles should = about 2445.559");
    }
}