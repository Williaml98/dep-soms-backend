package com.dep.soms.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LocationService {
    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);
    private static final double BASE_RADIUS_METERS = 500.0;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public boolean isWithinAllowedRadius(Double userLat, Double userLon, Double siteLat, Double siteLon, Double gpsAccuracy) {
        if (userLat == null || userLon == null || siteLat == null || siteLon == null) {
            return false;
        }

        double distance = calculateDistance(userLat, userLon, siteLat, siteLon);
        double allowedRadius = calculateDynamicRadius(gpsAccuracy);

        logger.info("Location check - Distance: {:.1f}m, Allowed radius: {:.1f}m, GPS accuracy: {}m",
                distance, allowedRadius, gpsAccuracy);

        return distance <= allowedRadius;
    }

    // Keep old method for backward compatibility
    public boolean isWithinAllowedRadius(Double userLat, Double userLon, Double siteLat, Double siteLon) {
        logger.warn("Location check without GPS accuracy - using fixed {}m radius", BASE_RADIUS_METERS);
        return isWithinAllowedRadius(userLat, userLon, siteLat, siteLon, 50.0);
    }

    private double calculateDynamicRadius(Double gpsAccuracy) {
        if (gpsAccuracy == null || gpsAccuracy <= 0) {
            return BASE_RADIUS_METERS;
        }

        // If GPS accuracy is poor, increase the allowed radius
        if (gpsAccuracy > 100) {
            double dynamicRadius = Math.max(BASE_RADIUS_METERS, gpsAccuracy * 2);
            logger.warn("Poor GPS accuracy ({}m), using expanded radius: {}m", gpsAccuracy, dynamicRadius);
            return dynamicRadius;
        }

        return BASE_RADIUS_METERS;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = EARTH_RADIUS_KM * c;
        return distanceKm * 1000;
    }

    public double getDistanceInMeters(Double userLat, Double userLon, Double siteLat, Double siteLon) {
        if (userLat == null || userLon == null || siteLat == null || siteLon == null) {
            return Double.MAX_VALUE;
        }
        return calculateDistance(userLat, userLon, siteLat, siteLon);
    }
}

