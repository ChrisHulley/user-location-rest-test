package uk.gov.dwp.test.application.utils;

import es.blackleg.java.geocalc.DegreeCoordinate;
import es.blackleg.java.geocalc.EarthCalc;
import es.blackleg.java.geocalc.Point;

public class LocationCalculator {

  private LocationCalculator() {
    // prevent instantiation
  }

  private static double distanceInMetres(
      double startingPointLat,
      double startingPointLong,
      double measurementPointLat,
      double measurementPointLong) {

    Point startingPoint =
        new Point(new DegreeCoordinate(startingPointLat), new DegreeCoordinate(startingPointLong));
    Point measurementPoint =
        new Point(
            new DegreeCoordinate(measurementPointLat), new DegreeCoordinate(measurementPointLong));

    return EarthCalc.getDistance(startingPoint, measurementPoint);
  }

  private static double convertMetresToMiles(double meters) {
    return meters / 1609;
  }

  public static boolean distanceWithinAllowableRadius(
      double startingPointLat,
      double startingPointLong,
      double measurementPointLat,
      double measurementPointLong,
      double allowableRadiusMiles) {

    return convertMetresToMiles(
            distanceInMetres(
                startingPointLat, startingPointLong, measurementPointLat, measurementPointLong))
        <= allowableRadiusMiles;
  }
}
