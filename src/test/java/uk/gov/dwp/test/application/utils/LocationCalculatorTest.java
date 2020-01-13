package uk.gov.dwp.test.application.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import es.blackleg.java.geocalc.DegreeCoordinate;
import org.junit.Test;

public class LocationCalculatorTest {

  // https://www.latlong.net/place/london-uk-14153.html
  private static final DegreeCoordinate LONDON_LAT = new DegreeCoordinate(51.509865);
  private static final DegreeCoordinate LONDON_LNG = new DegreeCoordinate(-0.118092);

  @Test
  public void successStPaulsLondon() {

    // st. pauls cathedral, london (https://latitude.to/)
    DegreeCoordinate stPaulsLat = new DegreeCoordinate(51.513870);
    DegreeCoordinate stPaulsLng = new DegreeCoordinate(-0.098362);
    assertTrue(testOutputs(stPaulsLat, stPaulsLng, 10));
  }

  @Test
  public void failureYorkMinster() {

    // york minster, york (https://latitude.to/)
    DegreeCoordinate yorkMinsterLat = new DegreeCoordinate(53.957162838);
    DegreeCoordinate yorkMinsterLng = new DegreeCoordinate(-1.07583303);
    assertFalse(testOutputs(yorkMinsterLat, yorkMinsterLng, 100));
  }

  private boolean testOutputs(DegreeCoordinate inputLat, DegreeCoordinate inputLng, double allowableRadius) {
    return
        LocationCalculator.distanceWithinAllowableRadius(
            LONDON_LAT.getDecimalDegrees(),
            LONDON_LNG.getDecimalDegrees(),
            inputLat.getDecimalDegrees(),
            inputLng.getDecimalDegrees(),
            allowableRadius);
  }
}
